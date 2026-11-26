package com.nairobisim.ui.loans

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentLoanApplicationBinding
import com.nairobisim.model.LoanSource
import com.nairobisim.model.LoanType
import com.nairobisim.ui.main.MainViewModel

class LoanApplicationFragment : Fragment() {
    
    private var _binding: FragmentLoanApplicationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private var selectedLoanType: LoanType? = null
    private val loanTypeAdapter = LoanTypeSelectAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanApplicationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
    }
    
    private fun setupRecyclerViews() {
        binding.loanTypesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = loanTypeAdapter
            loanTypeAdapter.submitList(LoanType.values().toList())
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSubmitApplication.setOnClickListener {
            selectedLoanType?.let { type ->
                val amount = binding.etLoanAmount.text.toString().toLongOrNull() ?: 0
                val term = binding.etLoanTerm.text.toString().toIntOrNull() ?: 0
                val purpose = binding.etLoanPurpose.text.toString()
                
                viewModel.takeLoan(LoanSource.BANK, amount, term)
                findNavController().popBackStack()
            }
        }
    }
    
    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSubmitButton()
                updateSummary()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        
        binding.etLoanAmount.addTextChangedListener(watcher)
        binding.etLoanTerm.addTextChangedListener(watcher)
        binding.etLoanPurpose.addTextChangedListener(watcher)
    }
    
    private fun observeViewModel() {
        loanTypeAdapter.onItemClick = { loanType ->
            selectedLoanType = loanType
            loanTypeAdapter.notifyDataSetChanged()
            updateSubmitButton()
            updateSummary()
        }
    }
    
    private fun updateSubmitButton() {
        val amount = binding.etLoanAmount.text.toString().toLongOrNull() ?: 0
        val term = binding.etLoanTerm.text.toString().toIntOrNull() ?: 0
        val purpose = binding.etLoanPurpose.text.toString()
        
        binding.btnSubmitApplication.isEnabled = selectedLoanType != null &&
                amount > 0 &&
                term > 0 &&
                purpose.isNotBlank()
    }
    
    private fun updateSummary() {
        val loanType = selectedLoanType
        val amount = binding.etLoanAmount.text.toString().toLongOrNull() ?: 0
        val term = binding.etLoanTerm.text.toString().toIntOrNull() ?: 0
        
        when {
            loanType != null && amount > 0 && term > 0 -> {
                val monthlyPayment = calculateMonthlyPayment(amount, loanType.interestRate, term)
                val totalInterest = monthlyPayment * term - amount
                binding.tvLoanSummary.text = getString(
                    R.string.loan_summary,
                    loanType.name,
                    viewModel.formatKes(amount),
                    term,
                    viewModel.formatKes(monthlyPayment),
                    viewModel.formatKes(totalInterest)
                )
            }
            loanType != null -> {
                binding.tvLoanSummary.text = getString(R.string.select_loan_details, loanType.name)
            }
            else -> {
                binding.tvLoanSummary.text = getString(R.string.select_loan_type)
            }
        }
    }
    
    private fun calculateMonthlyPayment(principal: Long, annualRate: Double, months: Int): Long {
        if (annualRate == 0.0) return principal / months
        val monthlyRate = annualRate / 12.0
        val payment = principal * monthlyRate * Math.pow(1 + monthlyRate, months.toDouble()) / (Math.pow(1 + monthlyRate, months.toDouble()) - 1)
        return payment.toLong()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LoanTypeSelectAdapter : androidx.recyclerview.widget.ListAdapter<LoanType, LoanTypeSelectAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<LoanType>() {
        override fun areItemsTheSame(oldItem: LoanType, newItem: LoanType): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: LoanType, newItem: LoanType): Boolean = oldItem == newItem
    }
) {
    
    var onItemClick: ((LoanType) -> Unit)? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemLoanTypeSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemLoanTypeSelectBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(loanType: LoanType) {
            binding.loanType = loanType
            itemView.setOnClickListener { onItemClick?.invoke(loanType) }
            binding.executePendingBindings()
        }
    }
}