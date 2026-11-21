package com.nairobisim.ui.finances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentFinancesBinding
import com.nairobisim.model.Business
import com.nairobisim.ui.main.MainViewModel

class FinancesFragment : Fragment() {
    
    private var _binding: FragmentFinancesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private val incomeAdapter = IncomeAdapter()
    private val expenseAdapter = ExpenseAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinancesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeData()
    }
    
    private fun setupRecyclerViews() {
        binding.incomeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = incomeAdapter
        }
        
        binding.expenseRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
        }
    }
    
    private fun observeData() {
        viewModel.activeBusinesses.observe(viewLifecycleOwner) { businesses ->
            incomeAdapter.submitList(businesses.filter { it.isOperational })
            
            val expenses = listOf(
                "Employee Salaries" to businesses.sumOf { it.employees.filter { it.isActive }.sumOf { it.salary } },
                "Rent & Utilities" to businesses.sumOf { (it.capitalInvested * 0.02).toLong() },
                "Asset Maintenance" to businesses.sumOf { it.assets.sumOf { it.maintenanceCost } },
                "Loan Payments" to viewModel.playerProfile.value?.loans?.sumOf { it.monthlyPayment } ?: 0L,
                "Chama Contributions" to viewModel.playerProfile.value?.chamas?.sumOf { it.monthlyContribution } ?: 0L
            )
            expenseAdapter.submitList(expenses)
        }
        
        viewModel.playerProfile.observe(viewLifecycleOwner) { profile ->
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class IncomeAdapter : androidx.recyclerview.widget.ListAdapter<Business, IncomeAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Business>() {
        override fun areItemsTheSame(oldItem: Business, newItem: Business): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Business, newItem: Business): Boolean = oldItem == newItem
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemIncomeBreakdownBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemIncomeBreakdownBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(business: Business) {
            binding.business = business
            binding.viewModel = (requireContext().applicationContext as com.nairobisim.NairobiSimApplication).let {
                androidx.lifecycle.ViewModelProvider(it)[com.nairobisim.ui.main.MainViewModel::class.java]
            }
            binding.executePendingBindings()
        }
    }
}

class ExpenseAdapter : androidx.recyclerview.widget.ListAdapter<Pair<String, Long>, ExpenseAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Pair<String, Long>>() {
        override fun areItemsTheSame(oldItem: Pair<String, Long>, newItem: Pair<String, Long>): Boolean = oldItem.first == newItem.first
        override fun areContentsTheSame(oldItem: Pair<String, Long>, newItem: Pair<String, Long>): Boolean = oldItem == newItem
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemExpenseBreakdownBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, amount) = getItem(position)
        holder.bind(category, amount)
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemExpenseBreakdownBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, amount: Long) {
            binding.category = category
            binding.amount = amount
            binding.viewModel = (requireContext().applicationContext as com.nairobisim.NairobiSimApplication).let {
                androidx.lifecycle.ViewModelProvider(it)[com.nairobisim.ui.main.MainViewModel::class.java]
            }
            binding.executePendingBindings()
        }
    }
}