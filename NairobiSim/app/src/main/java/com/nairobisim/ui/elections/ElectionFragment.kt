package com.nairobisim.ui.elections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import com.nairobisim.R
import com.nairobisim.data.repository.CampaignAction
import com.nairobisim.databinding.FragmentElectionsBinding
import com.nairobisim.model.Candidate
import com.nairobisim.model.Election
import com.nairobisim.model.Office
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectionFragment : Fragment() {

    private var _binding: FragmentElectionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ElectionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentElectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.btnStartElection.setOnClickListener {
            viewModel.startElection(Office.MCCA)
        }

        viewModel.toast.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                viewModel.clearToast()
            }
        }

        viewModel.elections.observe(viewLifecycleOwner) { elections ->
            renderElections(elections)
        }
    }

    private fun renderElections(elections: List<Election>) {
        val c = binding.electionsContainer
        c.removeAllViews()
        if (elections.isEmpty()) {
            c.addView(TextView(requireContext()).apply {
                text = "No elections yet. Start one above."
                setTextColor(resources.getColor(R.color.nairobi_text_secondary, null))
            })
            return
        }
        for (election in elections) {
            c.addView(buildElectionCard(election))
        }
    }

    private fun buildElectionCard(election: Election): View {
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            setCardBackgroundColor(resources.getColor(R.color.nairobi_surface, null))
            radius = 16f
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        container.addView(TextView(requireContext()).apply {
            text = "${election.office.displayName} — ${election.stage.displayName}"
            textSize = 18f
            setTextColor(resources.getColor(R.color.nairobi_primary, null))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        container.addView(TextView(requireContext()).apply {
            text = if (election.stage.name == "CONCLUDED") {
                val w = election.candidates.firstOrNull { it.id == election.winnerId }
                "Winner: ${w?.name ?: "TBD"}"
            } else {
                "Election in ${election.daysUntilElection} days"
            }
            textSize = 14f
            setTextColor(resources.getColor(R.color.nairobi_orange, null))
        })

        for (candidate in election.candidates) {
            container.addView(buildCandidateRow(election, candidate))
        }

        if (election.stage.name != "CONCLUDED") {
            val playerIn = election.candidates.any { it.isPlayer }
            if (!playerIn) {
                container.addView(Button(requireContext()).apply {
                    text = "Declare Candidacy (${election.office.filingFee} KES)"
                    setOnClickListener {
                        viewModel.declare(election.id, "Independent", "Jobs, clean water, lower costs.")
                    }
                })
            } else {
                container.addView(Button(requireContext()).apply {
                    text = "Hold Rally (50K)"
                    setOnClickListener { viewModel.campaign(election.id, CampaignAction.RALLY) }
                })
                container.addView(Button(requireContext()).apply {
                    text = "Buy Ads (100K)"
                    setOnClickListener { viewModel.campaign(election.id, CampaignAction.ADS) }
                })
                container.addView(Button(requireContext()).apply {
                    text = "Seek Endorsement (200K)"
                    setOnClickListener { viewModel.campaign(election.id, CampaignAction.ENDORSEMENT) }
                })
                container.addView(Button(requireContext()).apply {
                    text = "Advance 1 Day"
                    setOnClickListener { viewModel.nextDay(election.id) }
                })
            }
        }

        card.addView(container)
        return card
    }

    private fun buildCandidateRow(election: Election, candidate: Candidate): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 8)
            addView(TextView(requireContext()).apply {
                text = "${candidate.name}${if (candidate.isPlayer) " (YOU)" else ""} — ${candidate.party}"
                textSize = 15f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.nairobi_text_primary, null))
            })
            addView(TextView(requireContext()).apply {
                val pct = (election.projectedVotes(candidate) * 100).toInt()
                text = "Projected: $pct%  •  Rallies ${candidate.ralliesHeld}  Ads ${candidate.adsBought}  Endorsements ${candidate.endorsements}"
                textSize = 12f
                setTextColor(resources.getColor(R.color.nairobi_text_secondary, null))
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}