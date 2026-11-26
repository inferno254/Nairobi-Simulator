package com.nairobisim.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nairobisim.databinding.ItemBusinessCardBinding
import com.nairobisim.model.Business
import com.nairobisim.ui.main.MainViewModel

class BusinessAdapter : ListAdapter<Business, BusinessAdapter.BusinessViewHolder>(BusinessDiffCallback()) {

    var onItemClick: ((Business) -> Unit)? = null
    var viewModel: MainViewModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val binding = ItemBusinessCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BusinessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        val business = getItem(position)
        holder.bind(business)
    }

    inner class BusinessViewHolder(private val binding: ItemBusinessCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(business: Business) {
            binding.business = business
            binding.viewModel = viewModel
            binding.executePendingBindings()

            itemView.setOnClickListener { onItemClick?.invoke(business) }
        }
    }
}

class BusinessDiffCallback : DiffUtil.ItemCallback<Business>() {
    override fun areItemsTheSame(oldItem: Business, newItem: Business): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Business, newItem: Business): Boolean {
        return oldItem == newItem
    }
}