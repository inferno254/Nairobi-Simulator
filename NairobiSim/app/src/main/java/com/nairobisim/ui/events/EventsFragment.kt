package com.nairobisim.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentEventsBinding
import com.nairobisim.model.MarketEvent
import com.nairobisim.ui.main.MainViewModel

class EventsFragment : Fragment() {
    
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private val activeEventAdapter = EventAdapter()
    private val historyEventAdapter = EventAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeData()
    }
    
    private fun setupRecyclerViews() {
        binding.activeEventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeEventAdapter
        }
        
        binding.eventHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyEventAdapter
        }
    }
    
    private fun observeData() {
        viewModel.currentEvents.observe(viewLifecycleOwner) { events ->
            activeEventAdapter.submitList(events)
            historyEventAdapter.submitList(events.sortedByDescending { it.startDate })
            
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class EventAdapter : androidx.recyclerview.widget.ListAdapter<MarketEvent, EventAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<MarketEvent>() {
        override fun areItemsTheSame(oldItem: MarketEvent, newItem: MarketEvent): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MarketEvent, newItem: MarketEvent): Boolean = oldItem == newItem
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemEventBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(event: MarketEvent) {
            binding.event = event
            binding.executePendingBindings()
        }
    }
}