package com.nairobisim.ui.lifementor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentLifeMentorBinding
import com.nairobisim.databinding.ItemLifeMentorCaseBinding
import com.nairobisim.model.CounselingOutcome
import com.nairobisim.model.LifePath
import com.nairobisim.model.PersuasionApproach
import com.nairobisim.model.SmartPerson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeMentorFragment : Fragment() {

    private lateinit var binding: FragmentLifeMentorBinding
    private lateinit var viewModel: LifeMentorViewModel

    private val caseAdapter = LifeMentorCaseAdapter { person, path ->
        val approach = person.bestApproaches.firstOrNull() ?: PersuasionApproach.EMPATHETIC
        viewModel.counsel(person, path, approach)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLifeMentorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[LifeMentorViewModel::class.java]

        binding.recyclerCases.layoutManager = LinearLayoutManager(context)
        binding.recyclerCases.adapter = caseAdapter

        binding.btnRefresh.setOnClickListener { viewModel.refresh() }

        viewModel.cases.observe(viewLifecycleOwner) { cases ->
            caseAdapter.submitList(cases)
            binding.txtNoCases.visibility = if (cases.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.txtKarma.text = profile.karma.toString()
            binding.txtWisdom.text = profile.wisdom.toString()
            binding.txtCounseled.text = profile.totalPeopleMet.toString()
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.resultContainer.visibility = View.VISIBLE
                binding.resultOutcome.text = result.outcome.displayName
                binding.resultMessage.text = result.message

                val color = when (result.outcome) {
                    CounselingOutcome.CONVINCED -> R.color.nairobi_primary
                    CounselingOutcome.THINKING -> R.color.nairobi_secondary
                    CounselingOutcome.RESISTED -> R.color.nairobi_warning
                    CounselingOutcome.OFFENDED -> R.color.nairobi_error
                    CounselingOutcome.ALREADY_ON_PATH -> R.color.nairobi_tertiary
                    CounselingOutcome.NEEDS_MORE_TIME -> R.color.nairobi_tertiary
                }
                binding.resultOutcome.setTextColor(
                    requireContext().resources.getColor(color, null)
                )

                binding.resultKarmaDelta.text = if (result.karmaDelta >= 0) "+${result.karmaDelta}" else result.karmaDelta.toString()
                binding.resultRepDelta.text = if (result.trustDelta >= 0) "+${result.trustDelta}" else result.trustDelta.toString()
            }
        }

        binding.btnDismissResult.setOnClickListener {
            binding.resultContainer.visibility = View.GONE
        }

        viewModel.refresh()
    }

    private inner class LifeMentorCaseAdapter(
        private val onCounsel: (SmartPerson, LifePath) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<LifeMentorCaseAdapter.ViewHolder>() {

        private var cases = emptyList<SmartPerson>()

        fun submitList(list: List<SmartPerson>) {
            cases = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLifeMentorCaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(cases[position])
        }

        override fun getItemCount(): Int = cases.size

        inner class ViewHolder(private val b: ItemLifeMentorCaseBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(person: SmartPerson) {
                b.txtPersonName.text = person.name
                b.txtPersonAge.text = "Age ${person.age}"
                b.txtPersonLocation.text = person.location
                b.txtPersonCurrentPath.text = person.currentPath
                b.txtPersonOpenness.text = "Trust: ${(person.psychology.trustInPlayer * 100).toInt()}%"
                b.txtPersonStory.text = person.story
                b.txtPersonStruggle.text = "Struggle: ${person.struggle}"
                b.txtPersonDream.text = "Dream: ${person.dream}"

                b.pathButtonsContainer.removeAllViews()
                for (path in person.suggestedPaths) {
                    val btn = Button(requireContext())
                    btn.text = "Suggest: ${path.displayName}"
                    btn.setTextColor(requireContext().resources.getColor(android.R.color.white, null))
                    btn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            requireContext().resources.getColor(R.color.nairobi_primary, null)
                        )
                    )
                    btn.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 6 }
                    btn.setOnClickListener { onCounsel(person, path) }
                    b.pathButtonsContainer.addView(btn)
                }
            }
        }
    }
}