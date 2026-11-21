package com.nairobisim.ui.businesses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nairobisim.databinding.ItemBusinessCardBinding
import com.nairobisim.model.Business
import com.nairobisim.ui.main.MainViewModel

class BusinessCardAdapter : ListAdapter<Business, BusinessCardAdapter.BusinessViewHolder>(BusinessDiffCallback()) {
    
    var onItemClick: ((Business) -> Unit)? = null
    
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
            binding.viewModel = MainViewModel(requireContext().applicationContext as com.nairobisim.NairobiSimApplication)
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

class BusinessCategoryAdapter : ListAdapter<BusinessCategory, BusinessCategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = com.nairobisim.databinding.ItemBusinessCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
    }
    
    inner class CategoryViewHolder(private val binding: com.nairobisim.databinding.ItemBusinessCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: BusinessCategory) {
            binding.category = category
            binding.viewModel = MainViewModel(requireContext().applicationContext as com.nairobisim.NairobiSimApplication)
            binding.executePendingBindings()
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<BusinessCategory>() {
    override fun areItemsTheSame(oldItem: BusinessCategory, newItem: BusinessCategory): Boolean {
        return oldItem == newItem
    }
    
    override fun areContentsTheSame(oldItem: BusinessCategory, newItem: BusinessCategory): Boolean {
        return oldItem == newItem
    }
}