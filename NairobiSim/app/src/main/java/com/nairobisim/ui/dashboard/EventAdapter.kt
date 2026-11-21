package com.nairobisim.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nairobisim.databinding.ItemEventBinding
import com.nairobisim.model.MarketEvent

class EventAdapter : ListAdapter<MarketEvent, EventAdapter.EventViewHolder>(EventDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
    
    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: MarketEvent) {
            binding.event = event
            binding.executePendingBindings()
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<MarketEvent>() {
    override fun areItemsTheSame(oldItem: MarketEvent, newItem: MarketEvent): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: MarketEvent, newItem: MarketEvent): Boolean {
        return oldItem == newItem
    }
}