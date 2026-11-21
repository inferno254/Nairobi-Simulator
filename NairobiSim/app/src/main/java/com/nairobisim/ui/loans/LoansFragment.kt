package com.nairobisim.ui.loans

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
import com.nairobisim.databinding.FragmentLoansBinding
import com.nairobisim.model.Loan
import com.nairobisim.model.LoanType
import com.nairobisim.ui.main.MainViewModel

class LoansFragment : Fragment() {
    
    private var _binding: FragmentLoansBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private val activeLoanAdapter = ActiveLoanAdapter()
    private val availableLoanAdapter = AvailableLoanAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoansBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeData()
        setupClickListeners()
    }
    
    private fun setupRecyclerViews() {
        binding.activeLoansRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeLoanAdapter
        }
        
        binding.availableLoansRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = availableLoanAdapter
            availableLoanAdapter.submitList(LoanType.values().toList())
        }
    }
    
    private fun observeData() {
        viewModel.playerProfile.observe(viewLifecycleOwner) { profile ->
            profile?.loans?.let { loans ->
                activeLoanAdapter.submitList(loans.filter { it.isActive })
            }
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnApplyLoan.setOnClickListener {
            findNavController().navigate(R.id.loanApplicationFragment)
        }
        
        availableLoanAdapter.onItemClick = { loanType ->
            viewModel.selectLoanType(loanType)
            findNavController().navigate(R.id.loanApplicationFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ActiveLoanAdapter : androidx.recyclerview.widget.ListAdapter<Loan, ActiveLoanAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Loan>() {
        override fun areItemsTheSame(oldItem: Loan, newItem: Loan): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Loan, newItem: Loan): Boolean = oldItem == newItem
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemLoanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemLoanBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(loan: Loan) {
            binding.loan = loan
            binding.viewModel = (requireContext().applicationContext as com.nairobisim.NairobiSimApplication).let {
                androidx.lifecycle.ViewModelProvider(it)[com.nairobisim.ui.main.MainViewModel::class.java]
            }
            binding.executePendingBindings()
        }
    }
}

class AvailableLoanAdapter : androidx.recyclerview.widget.ListAdapter<LoanType, AvailableLoanAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<LoanType>() {
        override fun areItemsTheSame(oldItem: LoanType, newItem: LoanType): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: LoanType, newItem: LoanType): Boolean = oldItem == newItem
    }
) {
    
    var onItemClick: ((LoanType) -> Unit)? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemLoanAvailableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemLoanAvailableBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(loanType: LoanType) {
            binding.loanType = loanType
            binding.viewModel = (requireContext().applicationContext as com.nairobisim.NairobiSimApplication).let {
                androidx.lifecycle.ViewModelProvider(it)[com.nairobisim.ui.main.MainViewModel::class.java]
            }
            binding.onClick = { onItemClick?.invoke(loanType) }
            binding.executePendingBindings()
        }
    }
}