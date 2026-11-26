package com.nairobisim.ui.freelance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentFreelanceBinding
import com.nairobisim.databinding.ItemFreelanceContractBinding
import com.nairobisim.databinding.ItemFreelanceGigBinding
import com.nairobisim.model.FreelanceContract
import com.nairobisim.model.FreelanceGig
import com.nairobisim.model.FreelanceJobStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FreelanceFragment : Fragment() {

    private lateinit var binding: FragmentFreelanceBinding
    private lateinit var viewModel: FreelanceViewModel

    private val gigAdapter = FreelanceGigAdapter { gig, offer ->
        viewModel.haggleForGig(gig, offer)
    }
    private val contractAdapter = FreelanceContractAdapter { contract ->
        when (contract.status) {
            FreelanceJobStatus.ACCEPTED, FreelanceJobStatus.IN_PROGRESS ->
                viewModel.completeContract(contract.id)
            FreelanceJobStatus.DELAYED ->
                viewModel.collectDelayedPayment(contract.id)
            else -> {}
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFreelanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[FreelanceViewModel::class.java]

        binding.recyclerGigs.layoutManager = LinearLayoutManager(context)
        binding.recyclerGigs.adapter = gigAdapter

        binding.recyclerActive.layoutManager = LinearLayoutManager(context)
        binding.recyclerActive.adapter = contractAdapter

        binding.recyclerDelayed.layoutManager = LinearLayoutManager(context)
        binding.recyclerDelayed.adapter = contractAdapter

        binding.btnRefresh.setOnClickListener { viewModel.refresh() }

        viewModel.availableGigs.observe(viewLifecycleOwner) { gigs ->
            gigAdapter.submitList(gigs)
            binding.txtNoGigs.visibility = if (gigs.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.activeContracts.observe(viewLifecycleOwner) { contracts ->
            contractAdapter.submitList(contracts)
            binding.activeContractsSection.visibility = if (contracts.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.delayedContracts.observe(viewLifecycleOwner) { contracts ->
            contractAdapter.submitList(contracts)
            binding.delayedPaymentsSection.visibility = if (contracts.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.txtTotalEarnings.text = "KES ${profile.totalEarnings}"
            binding.txtGigsCompleted.text = profile.gigsCompleted.toString()
            binding.txtReputation.text = profile.reputation.toString()
        }

        viewModel.result.observe(viewLifecycleOwner) { outcome ->
            if (outcome != null) {
                binding.resultContainer.visibility = View.VISIBLE
                binding.resultType.text = if (outcome.success) "Success" else "Failed"
                binding.resultMessage.text = outcome.message
                binding.resultType.setTextColor(
                    requireContext().resources.getColor(
                        if (outcome.success) R.color.nairobi_primary else R.color.nairobi_error,
                        null
                    )
                )
            }
        }

        binding.btnDismissResult.setOnClickListener {
            binding.resultContainer.visibility = View.GONE
            viewModel.refresh()
        }

        viewModel.refresh()
        viewModel.observeContracts()
    }

    private inner class FreelanceGigAdapter(
        private val onHaggle: (FreelanceGig, Long) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<FreelanceGigAdapter.ViewHolder>() {

        private var gigs = emptyList<FreelanceGig>()

        fun submitList(list: List<FreelanceGig>) {
            gigs = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemFreelanceGigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(gigs[position])
        }

        override fun getItemCount(): Int = gigs.size

        inner class ViewHolder(private val b: ItemFreelanceGigBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(gig: FreelanceGig) {
                b.txtGigTitle.text = gig.title
                b.txtGigPay.text = "KES ${gig.basePay}"
                b.txtGigSkill.text = gig.skill.displayName
                b.txtGigLevel.text = gig.requiredLevel.displayName
                b.txtGigClient.text = gig.client.name
                b.txtGigDeadline.text = "${gig.deadlineDays} days"
                b.txtGigDesc.text = gig.description

                val playerLevel = viewModel.levelFor(gig.skill)
                val canAfford = gig.canAffordSkill(playerLevel)
                b.txtGigSkillReq.visibility = if (canAfford) View.GONE else View.VISIBLE
                b.txtGigSkillReq.text = getString(
                    R.string.freelance_skill_required,
                    gig.skill.displayName,
                    gig.requiredLevel.displayName
                )
                b.txtGigSkillReq.setTextColor(
                    requireContext().resources.getColor(
                        if (canAfford) R.color.nairobi_primary else R.color.nairobi_error,
                        null
                    )
                )

                val payNote = if (gig.instantPayment) getString(R.string.freelance_client_pays)
                else getString(R.string.freelance_client_delays)
                b.txtGigPaymentNote.text = payNote

                b.btnHaggle.setOnClickListener {
                    val offerText = b.edtOffer.text.toString().toLongOrNull()
                    if (offerText == null || offerText <= 0) {
                        Toast.makeText(context, "Enter a valid offer", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    onHaggle(gig, offerText)
                }
            }
        }
    }

    private inner class FreelanceContractAdapter(
        private val onAction: (FreelanceContract) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<FreelanceContractAdapter.ViewHolder>() {

        private var contracts = emptyList<FreelanceContract>()

        fun submitList(list: List<FreelanceContract>) {
            contracts = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemFreelanceContractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(contracts[position])
        }

        override fun getItemCount(): Int = contracts.size

        inner class ViewHolder(private val b: ItemFreelanceContractBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(contract: FreelanceContract) {
                b.txtContractTitle.text = contract.gigTitle
                b.txtContractClient.text = contract.clientName
                b.txtContractPay.text = "KES ${contract.agreedPay}"
                b.txtContractSkill.text = contract.skill.displayName
                b.txtContractStatus.text = contract.status.displayName

                val btnText = when (contract.status) {
                    FreelanceJobStatus.ACCEPTED, FreelanceJobStatus.IN_PROGRESS ->
                        getString(R.string.freelance_complete)
                    FreelanceJobStatus.DELAYED ->
                        getString(R.string.freelance_collect)
                    else -> "View"
                }
                b.btnContractAction.text = btnText
                b.btnContractAction.setOnClickListener { onAction(contract) }
            }
        }
    }
}
