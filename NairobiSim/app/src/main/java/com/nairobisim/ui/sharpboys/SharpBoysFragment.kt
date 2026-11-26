package com.nairobisim.ui.sharpboys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentSharpBoysBinding
import com.nairobisim.model.SharpBoyEncounter
import com.nairobisim.model.StreetEncounterOutcome
import com.nairobisim.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharpBoysFragment : Fragment() {

    private lateinit var binding: FragmentSharpBoysBinding
    private lateinit var viewModel: SharpBoysViewModel

    private val historyAdapter = EncounterHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSharpBoysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SharpBoysViewModel::class.java]

        // Setup RecyclerView
        binding.recyclerHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerHistory.adapter = historyAdapter

        // Observe encounter history
        viewModel.encounterHistory.observe(viewLifecycleOwner) { encounters ->
            historyAdapter.submitList(encounters)
        }

        // Observe current encounter and update UI
        viewModel.currentEncounter.observe(viewLifecycleOwner) { encounter ->
            if (encounter != null) {
                binding.encounterContainer.visibility = View.VISIBLE
                binding.encounterType.text = encounter.boyType.displayName
                binding.encounterLocation.text = "${encounter.location} • ${encounter.timeOfDay}"
                binding.encounterDetails.text = encounter.details
                binding.encounterDemand.text = "Demand: KES ${encounter.demandedAmount}"
            }
        }

        // Observe encounter result
        viewModel.encounterResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.resultContainer.visibility = View.VISIBLE
                binding.resultType.text = result.outcome.displayName
                binding.resultType.setTextColor(
                    requireContext().resources.getColor(colorForOutcome(result.outcome), null)
                )
                binding.resultMessage.text = result.message

                val impact = buildImpactString(result)
                binding.resultImpact.text = impact
            }
        }

        // Observe street cred and reputation
        viewModel.streetCred.observe(viewLifecycleOwner) { cred ->
            binding.txtStreetCred.text = cred.toString()
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Button click listeners
        binding.btnGenerate.setOnClickListener {
            viewModel.generateRandomEncounter("Nairobi", "Day")
        }

        binding.btnPay.setOnClickListener {
            viewModel.resolveEncounter(StreetEncounterOutcome.PAID)
        }

        binding.btnRefuse.setOnClickListener {
            viewModel.resolveEncounter(StreetEncounterOutcome.REFUSED)
        }

        binding.btnNegotiate.setOnClickListener {
            viewModel.resolveEncounter(StreetEncounterOutcome.NEGOTIATED)
        }

        binding.btnFight.setOnClickListener {
            viewModel.resolveEncounter(StreetEncounterOutcome.FOUGHT)
        }

        binding.btnEscape.setOnClickListener {
            viewModel.resolveEncounter(StreetEncounterOutcome.ESCAPED)
        }

        binding.btnDismiss.setOnClickListener {
            viewModel.dismissResult()
        }

        // Inject player state from parent activity's MainViewModel
        val mainViewModel: MainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel.playerProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                viewModel.setPlayerState(
                    reputation = profile.reputation,
                    streetCred = profile.reputation,
                    kesBalance = profile.kesBalance ?: 0,
                    hasPhone = true
                )
                binding.txtReputation.text = profile.reputation.toString()
            }
        }
    }

    private fun colorForOutcome(outcome: StreetEncounterOutcome): Int {
        return when (outcome) {
            StreetEncounterOutcome.PAID -> R.color.nairobi_error
            StreetEncounterOutcome.REFUSED -> R.color.nairobi_primary
            StreetEncounterOutcome.NEGOTIATED -> R.color.nairobi_warning
            StreetEncounterOutcome.FOUGHT -> R.color.nairobi_tertiary
            StreetEncounterOutcome.ESCAPED -> R.color.nairobi_teal
            StreetEncounterOutcome.PHONE_STOLEN -> R.color.nairobi_tertiary
            StreetEncounterOutcome.INJURED -> R.color.nairobi_error
            StreetEncounterOutcome.ARRESTED -> R.color.nairobi_primary_dark
        }
    }

    private fun buildImpactString(result: com.nairobisim.model.SharpBoyOutcome): String {
        val sb = StringBuilder()
        if (result.amountPaid > 0) sb.append("- KES ${result.amountPaid} paid\n")
        if (result.phoneStolen) sb.append("Phone stolen\n")
        if (result.injured) sb.append("Injured - Medical: KES ${result.medicalCost}\n")
        sb.append("Street cred: ")
        if (result.reputationChange >= 0) sb.append("+")
        sb.append(result.reputationChange)
        return sb.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = FragmentSharpBoysBinding.bind(View(requireContext()))
    }
}

// ==================== ADAPTER ====================

class EncounterHistoryAdapter :
    androidx.recyclerview.widget.ListAdapter<SharpBoyEncounter, EncounterHistoryAdapter.ViewHolder>(
        EncounterHistoryDiffCallback()
    ) {

    class ViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val typeView: TextView = itemView.findViewById(R.id.itemType)
        val locationView: TextView = itemView.findViewById(R.id.itemLocation)
        val amountView: TextView = itemView.findViewById(R.id.itemAmount)
        val outcomeView: TextView = itemView.findViewById(R.id.itemOutcome)
        val dateView: TextView = itemView.findViewById(R.id.itemDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_encounter_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val encounter = getItem(position)
        holder.typeView.text = encounter.boyType.displayName
        holder.locationView.text = "${encounter.location} • ${encounter.timeOfDay}"
        holder.amountView.text = "Demand: KES ${encounter.demandedAmount}"
        holder.outcomeView.text = encounter.outcome?.displayName ?: "Pending"
        holder.outcomeView.setTextColor(
            encounter.outcome?.let { colorRes(it) }
                ?: holder.itemView.context.resources.getColor(R.color.nairobi_text_secondary, null)
        )
        val date = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(encounter.timestamp))
        holder.dateView.text = date
    }

    private fun colorRes(outcome: StreetEncounterOutcome): Int {
        return when (outcome) {
            StreetEncounterOutcome.PAID -> R.color.nairobi_error
            StreetEncounterOutcome.REFUSED -> R.color.nairobi_primary
            StreetEncounterOutcome.NEGOTIATED -> R.color.nairobi_warning
            StreetEncounterOutcome.FOUGHT -> R.color.nairobi_tertiary
            StreetEncounterOutcome.ESCAPED -> R.color.nairobi_teal
            StreetEncounterOutcome.PHONE_STOLEN -> R.color.nairobi_tertiary
            StreetEncounterOutcome.INJURED -> R.color.nairobi_error
            StreetEncounterOutcome.ARRESTED -> R.color.nairobi_primary_dark
        }
    }
}

class EncounterHistoryDiffCallback :
    androidx.recyclerview.widget.DiffUtil.ItemCallback<SharpBoyEncounter>() {
    override fun areItemsTheSame(oldItem: SharpBoyEncounter, newItem: SharpBoyEncounter): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SharpBoyEncounter, newItem: SharpBoyEncounter): Boolean =
        oldItem == newItem
}
