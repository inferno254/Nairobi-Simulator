package com.nairobisim.ui.forex

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.nairobisim.databinding.FragmentForexBinding
import com.nairobisim.model.*
import com.nairobisim.ui.forex.adapter.*
import androidx.core.content.ContextCompat

class ForexFragment : Fragment() {

    private var _binding: FragmentForexBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForexViewModel by viewModels()

    private val pairAdapter = CurrencyPairAdapter()
    private val positionAdapter = OpenPositionAdapter()
    private val historyAdapter = TradeHistoryAdapter()

    private var selectedPair: CurrencyPair? = null
    private var selectedTradeType: TradeType = TradeType.BUY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
        setupPairSelector()
    }

    private fun setupRecyclerViews() {
        binding.pairsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pairAdapter
        }
        binding.openPositionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = positionAdapter
        }
        binding.tradeHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.rates.observe(viewLifecycleOwner) { rates ->
            pairAdapter.submitList(DEFAULT_FOREX_PAIRS.map { pair ->
                val rate = rates[pair.symbol]
                pair.copy() // return pair, rate shown in adapter
            })
        }

        viewModel.openTrades.observe(viewLifecycleOwner) { trades ->
            positionAdapter.submitList(trades)
        }

        viewModel.tradeHistory.observe(viewLifecycleOwner) { trades ->
            historyAdapter.submitList(trades)
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.executePendingBindings()
        }

        viewModel.equity.observe(viewLifecycleOwner) { equity ->
            binding.executePendingBindings()
        }

        viewModel.usedMargin.observe(viewLifecycleOwner) {
            binding.executePendingBindings()
        }

        viewModel.freeMargin.observe(viewLifecycleOwner) {
            binding.executePendingBindings()
        }

        viewModel.marginLevel.observe(viewLifecycleOwner) {
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

        viewModel.selectedPair.observe(viewLifecycleOwner) { pair ->
            selectedPair = pair
            binding.pairSelector.setText(pair?.displayName ?: "", false)
        }
    }

    private fun setupPairSelector() {
        val pairNames = DEFAULT_FOREX_PAIRS.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, pairNames)
        binding.pairSelector.setAdapter(adapter)
        binding.pairSelector.setOnItemClickListener { _, _, position, _ ->
            selectedPair = DEFAULT_FOREX_PAIRS[position]
            viewModel.selectPair(selectedPair!!)
        }

        // Auto-select first pair
        if (selectedPair == null && DEFAULT_FOREX_PAIRS.isNotEmpty()) {
            selectedPair = DEFAULT_FOREX_PAIRS.first()
            viewModel.selectPair(selectedPair!!)
            binding.pairSelector.setText(selectedPair!!.displayName, false)
        }
    }

    private fun setupClickListeners() {
        // Buy button
        binding.btnBuy.setOnClickListener {
            selectedTradeType = TradeType.BUY
            binding.btnBuy.isChecked = true
            binding.btnSell.isChecked = false
        }

        // Sell button
        binding.btnSell.setOnClickListener {
            selectedTradeType = TradeType.SELL
            binding.btnSell.isChecked = true
            binding.btnBuy.isChecked = false
        }

        // Execute trade
        binding.btnExecuteTrade.setOnClickListener {
            executeTrade()
        }

        // Position click - close position
        positionAdapter.onPositionClick = { trade ->
            viewModel.closeTrade(trade.id)
        }
    }

    private fun executeTrade() {
        val pair = selectedPair ?: return
        val lotSizeText = binding.etLotSize.text.toString()
        val lotSize = lotSizeText.toDoubleOrNull() ?: 0.0

        if (lotSize <= 0) {
            Toast.makeText(requireContext(), "Enter valid lot size", Toast.LENGTH_SHORT).show()
            return
        }

        val slText = binding.etStopLoss.text.toString()
        val tpText = binding.etTakeProfit.text.toString()
        val stopLoss = slText.toDoubleOrNull() ?: 0.0
        val takeProfit = tpText.toDoubleOrNull() ?: 0.0

        viewModel.openTrade(pair.symbol, selectedTradeType, lotSize, stopLoss, takeProfit)

        // Clear inputs
        binding.etLotSize.text?.clear()
        binding.etStopLoss.text?.clear()
        binding.etTakeProfit.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
