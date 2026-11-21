package com.nairobisim.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nairobisim.databinding.ItemTransactionBinding
import com.nairobisim.model.DailyTransaction
import com.nairobisim.ui.main.MainViewModel

class TransactionAdapter : ListAdapter<DailyTransaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }
    
    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: DailyTransaction) {
            binding.transaction = transaction
            binding.viewModel = MainViewModel(requireContext().applicationContext as com.nairobisim.NairobiSimApplication)
            binding.executePendingBindings()
        }
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<DailyTransaction>() {
    override fun areItemsTheSame(oldItem: DailyTransaction, newItem: DailyTransaction): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: DailyTransaction, newItem: DailyTransaction): Boolean {
        return oldItem == newItem
    }
}