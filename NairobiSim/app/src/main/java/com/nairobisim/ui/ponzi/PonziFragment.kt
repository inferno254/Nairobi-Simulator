package com.nairobisim.ui.ponzi

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
import com.nairobisim.databinding.FragmentPonziBinding
import com.nairobisim.databinding.ItemPonziInvestmentBinding
import com.nairobisim.databinding.ItemPonziSchemeBinding
import com.nairobisim.model.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PonziFragment : Fragment() {

    private lateinit var binding: FragmentPonziBinding
    private lateinit var viewModel: PonziViewModel

    private val schemeAdapter = SchemeAdapter { scheme, amount ->
        viewModel.invest(scheme.id, amount)
    }
    private val investmentAdapter = InvestmentAdapter { investment ->
        viewModel.collectReturns(investment.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPonziBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[PonziViewModel::class.java]

        binding.recyclerSchemes.layoutManager = LinearLayoutManager(context)
        binding.recyclerSchemes.adapter = schemeAdapter

        binding.recyclerInvestments.layoutManager = LinearLayoutManager(context)
        binding.recyclerInvestments.adapter = investmentAdapter

        binding.btnRefresh.setOnClickListener { viewModel.refresh() }
        binding.btnDismissResult.setOnClickListener { binding.resultContainer.visibility = View.GONE }

        viewModel.schemes.observe(viewLifecycleOwner) { schemes ->
            schemeAdapter.submitList(schemes)
        }

        viewModel.investments.observe(viewLifecycleOwner) { investments ->
            investmentAdapter.submitList(investments)
            val total = investments.sumOf { it.amount }
            val lost = investments.filter { it.status == "LOST" }.sumOf { it.amount }
            binding.txtTotalInvested.text = "KES $total"
            binding.txtLostMoney.text = "KES $lost"
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.resultContainer.visibility = View.VISIBLE
                binding.resultMessage.text = result
            }
        }

        viewModel.investResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.resultContainer.visibility = View.VISIBLE
                binding.resultMessage.text = result
            }
        }

        viewModel.refresh()
    }

    private inner class SchemeAdapter(
        private val onInvest: (PonziScheme, Long) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<SchemeAdapter.ViewHolder>() {

        private var schemes = emptyList<PonziScheme>()

        fun submitList(list: List<PonziScheme>) {
            schemes = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemPonziSchemeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(schemes[position])
        }

        override fun getItemCount(): Int = schemes.size

        inner class ViewHolder(private val b: ItemPonziSchemeBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(scheme: PonziScheme) {
                b.txtSchemeName.text = scheme.name
                b.txtSchemeType.text = scheme.type.displayName
                b.txtPromoter.text = scheme.promoterName
                b.txtLocation.text = scheme.location
                b.txtPromisedReturn.text = "Promised: ${scheme.promisedReturn}% per ${scheme.promisedPeriod} days"
                b.txtMinInvestment.text = "Min: KES ${scheme.minimumInvestment}"
                b.txtSchemeStatus.text = scheme.status.displayName

                val statusColor = when (scheme.status) {
                    PonziSchemeStatus.ACTIVE -> R.color.nairobi_warning
                    PonziSchemeStatus.PAYING -> R.color.nairobi_primary
                    PonziSchemeStatus.COLLAPSED -> R.color.nairobi_error
                    PonziSchemeStatus.INVESTIGATED -> R.color.nairobi_text_secondary
                }
                b.txtSchemeStatus.setTextColor(requireContext().resources.getColor(statusColor, null))

                // Show warning if player knows it's a ponzi
                b.txtSchemeWarning.visibility = if (scheme.isKnownPonzi) View.VISIBLE else View.GONE

                b.btnInvest.visibility = if (scheme.status == PonziSchemeStatus.ACTIVE) View.VISIBLE else View.GONE
                b.btnInvest.setOnClickListener {
                    val amount = b.edtInvestAmount.text.toString().toLongOrNull() ?: 0L
                    if (amount <= 0) { Toast.makeText(context, "Enter amount", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    onInvest(scheme, amount)
                }
            }
        }
    }

    private inner class InvestmentAdapter(
        private val onCollect: (PonziInvestment) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<InvestmentAdapter.ViewHolder>() {

        private var investments = emptyList<PonziInvestment>()

        fun submitList(list: List<PonziInvestment>) {
            investments = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemPonziInvestmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(investments[position])
        }

        override fun getItemCount(): Int = investments.size

        inner class ViewHolder(private val b: ItemPonziInvestmentBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {

            fun bind(inv: PonziInvestment) {
                b.txtInvScheme.text = inv.schemeName
                b.txtInvAmount.text = "Invested: KES ${inv.amount}"
                b.txtInvExpected.text = "Expected: KES ${inv.expectedReturn}"
                b.txtInvStatus.text = inv.status

                val color = when (inv.status) {
                    "ACTIVE" -> R.color.nairobi_secondary
                    "PAID_OUT" -> R.color.nairobi_primary
                    "LOST" -> R.color.nairobi_error
                    else -> R.color.nairobi_text_secondary
                }
                b.txtInvStatus.setTextColor(requireContext().resources.getColor(color, null))

                b.btnCollect.visibility = if (inv.status == "ACTIVE") View.VISIBLE else View.GONE
                b.btnCollect.setOnClickListener { onCollect(inv) }
            }
        }
    }
}