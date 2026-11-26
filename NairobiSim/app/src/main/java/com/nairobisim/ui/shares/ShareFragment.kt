package com.nairobisim.ui.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentSharesBinding
import com.nairobisim.model.ShareCompany
import com.nairobisim.model.NAIROBI_SHARE_MARKET
import com.nairobisim.ui.shares.adapter.*
import androidx.core.content.ContextCompat

class ShareFragment : Fragment() {

    private var _binding: FragmentSharesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ShareViewModel by viewModels()

    private val sharesAdapter = ShareCompanyAdapter()
    private val portfolioAdapter = PortfolioAdapter()
    private val historyAdapter = ShareHistoryAdapter()

    private var selectedCompany: ShareCompany? = null
    private var selectedAction: String = "BUY"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
        setupCompanySelector()
    }

    private fun setupRecyclerViews() {
        binding.sharesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sharesAdapter
        }
        binding.portfolioRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = portfolioAdapter
        }
        binding.shareHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.companies.observe(viewLifecycleOwner) { companies ->
            sharesAdapter.submitList(companies)
        }

        viewModel.prices.observe(viewLifecycleOwner) { prices ->
            sharesAdapter.notifyDataSetChanged()
            updateCurrentPriceDisplay()
        }

        viewModel.openPositions.observe(viewLifecycleOwner) { positions ->
            portfolioAdapter.submitList(positions)
        }

        viewModel.tradeHistory.observe(viewLifecycleOwner) { history ->
            historyAdapter.submitList(history)
        }

        viewModel.balance.observe(viewLifecycleOwner) {
            binding.executePendingBindings()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }

        viewModel.selectedCompany.observe(viewLifecycleOwner) { company ->
            selectedCompany = company
            if (company != null) {
                binding.companySelector.setText(company.name, false)
                updateCurrentPriceDisplay()
            }
        }
    }

    private fun updateCurrentPriceDisplay() {
        val company = selectedCompany ?: return
        val price = viewModel.prices.value?.get(company.symbol) ?: company.currentPrice
        binding.tvCurrentPrice.text = "Current Price: KES ${String.format("%.2f", price)}"
    }

    private fun setupCompanySelector() {
        val companyNames: List<String> = NAIROBI_SHARE_MARKET.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, companyNames)
        binding.companySelector.setAdapter(adapter)
        binding.companySelector.setOnItemClickListener { _, _, position, _ ->
            selectedCompany = NAIROBI_SHARE_MARKET[position]
            viewModel.selectCompany(selectedCompany!!)
        }

        // Auto-select first
        if (selectedCompany == null && NAIROBI_SHARE_MARKET.isNotEmpty()) {
            selectedCompany = NAIROBI_SHARE_MARKET.first()
            viewModel.selectCompany(selectedCompany!!)
            binding.companySelector.setText(selectedCompany!!.name, false)
        }
    }

    private fun setupClickListeners() {
        binding.btnBuyShares.setOnClickListener {
            selectedAction = "BUY"
            binding.btnBuyShares.isChecked = true
            binding.btnSellShares.isChecked = false
        }

        binding.btnSellShares.setOnClickListener {
            selectedAction = "SELL"
            binding.btnSellShares.isChecked = true
            binding.btnBuyShares.isChecked = false
        }

        binding.btnExecuteShareTrade.setOnClickListener {
            executeTrade()
        }
    }

    private fun executeTrade() {
        val company = selectedCompany ?: return
        val sharesText = binding.etShares.text.toString()
        val shares = sharesText.toIntOrNull() ?: 0

        if (shares <= 0) {
            Toast.makeText(requireContext(), "Enter valid number of shares", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedAction == "BUY") {
            viewModel.buyShares(company.symbol, shares)
        } else {
            viewModel.sellShares(company.symbol, shares)
        }

        binding.etShares.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
