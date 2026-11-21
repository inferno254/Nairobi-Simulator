package com.nairobisim.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentDashboardBinding
import com.nairobisim.model.Business
import com.nairobisim.model.DailyTransaction
import com.nairobisim.model.MarketEvent
import com.nairobisim.ui.main.MainViewModel
import com.nairobisim.ui.main.MainViewModel.MainTab

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private val businessAdapter = BusinessAdapter()
    private val transactionAdapter = TransactionAdapter()
    private val eventAdapter = EventAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeData()
        setupClickListeners()
    }
    
    private fun setupRecyclerViews() {
        binding.businessesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = businessAdapter
        }
        
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
        
        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }
    
    private fun observeData() {
        viewModel.activeBusinesses.observe(viewLifecycleOwner) { businesses ->
            businessAdapter.submitList(businesses)
            binding.businessesRecyclerView.visibility = if (businesses.isEmpty()) View.GONE else View.VISIBLE
        }
        
        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions.take(10))
        }
        
        viewModel.currentEvents.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
            binding.eventsRecyclerView.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
        }
        
        viewModel.playerProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.kesBalance.text = viewModel.formatKes(it.kesBalance)
                binding.monthlyIncome.text = viewModel.formatKes(calculateMonthlyIncome())
                binding.monthlyExpenses.text = viewModel.formatKes(calculateMonthlyExpenses())
                binding.netWorth.text = viewModel.formatKes(calculateNetWorth(it))
            }
        }
    }
    
    private fun calculateMonthlyIncome(): Long {
        return viewModel.activeBusinesses.value?.sumOf { it.netMonthlyIncome } ?: 0
    }
    
    private fun calculateMonthlyExpenses(): Long {
        return viewModel.activeBusinesses.value?.sumOf { it.monthlyExpenses } ?: 0
    }
    
    private fun calculateNetWorth(profile: com.nairobisim.model.PlayerProfile): Long {
        val businessValue = viewModel.activeBusinesses.value?.sumOf { it.capitalInvested } ?: 0
        return profile.kesBalance + profile.mpesaBalance + profile.bankBalance + profile.cryptoWallet + businessValue
    }
    
    private fun setupClickListeners() {
        binding.btnStartBusiness.setOnClickListener {
            viewModel.setTab(MainTab.BUSINESSES)
            findNavController().navigate(R.id.startBusinessFragment)
        }
        
        businessAdapter.onItemClick = { business ->
            viewModel.selectBusiness(business)
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToBusinessDetailFragment(business.id)
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}