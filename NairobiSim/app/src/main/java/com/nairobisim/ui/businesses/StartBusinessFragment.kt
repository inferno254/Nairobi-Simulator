package com.nairobisim.ui.businesses

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
import com.nairobisim.databinding.FragmentStartBusinessBinding
import com.nairobisim.model.BusinessCategory
import com.nairobisim.model.NairobiArea
import com.nairobisim.ui.main.MainViewModel

class StartBusinessFragment : Fragment() {
    
    private var _binding: FragmentStartBusinessBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private var selectedCategory: BusinessCategory? = null
    private var selectedLocation: NairobiArea? = null
    
    private val categoryAdapter = CategorySelectAdapter()
    private val locationAdapter = LocationSelectAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupRecyclerViews() {
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            categoryAdapter.submitList(BusinessCategory.values().toList())
        }
        
        binding.locationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationAdapter
            locationAdapter.submitList(NairobiArea.values().toList())
        }
    }
    
    private fun setupClickListeners() {
        binding.btnStartBusiness.setOnClickListener {
            selectedCategory?.let { category ->
                selectedLocation?.let { location ->
                    val businessName = binding.etBusinessName.text.toString()
                    viewModel.startBusiness(category, location, businessName)
                    findNavController().popBackStack()
                }
            }
        }
    }
    
    private fun observeViewModel() {
        categoryAdapter.onItemClick = { category ->
            selectedCategory = category
            categoryAdapter.notifyDataSetChanged()
            updateStartButton()
            updateSummary()
        }
        
        locationAdapter.onItemClick = { location ->
            selectedLocation = location
            locationAdapter.notifyDataSetChanged()
            updateStartButton()
            updateSummary()
        }
    }
    
    private fun updateStartButton() {
        binding.btnStartBusiness.isEnabled = selectedCategory != null && selectedLocation != null
    }
    
    private fun updateSummary() {
        val category = selectedCategory
        val location = selectedLocation
        
        when {
            category != null && location != null -> {
                val capital = (category.baseCapital * location.rentMultiplier).toLong()
                val estimatedRevenue = (category.baseCapital / 5 * location.demandMultiplier).toLong()
                binding.tvSummary.text = getString(
                    R.string.business_summary,
                    category.displayName,
                    location.displayName,
                    viewModel.formatKes(capital),
                    viewModel.formatKes(estimatedRevenue)
                )
            }
            category != null -> {
                binding.tvSummary.text = getString(R.string.select_location_summary, category.displayName)
            }
            location != null -> {
                binding.tvSummary.text = getString(R.string.select_category_summary, location.displayName)
            }
            else -> {
                binding.tvSummary.text = getString(R.string.select_both_summary)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CategorySelectAdapter : androidx.recyclerview.widget.ListAdapter<BusinessCategory, CategorySelectAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<BusinessCategory>() {
        override fun areItemsTheSame(oldItem: BusinessCategory, newItem: BusinessCategory): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: BusinessCategory, newItem: BusinessCategory): Boolean = oldItem == newItem
    }
) {
    
    var onItemClick: ((BusinessCategory) -> Unit)? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemBusinessCategorySelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemBusinessCategorySelectBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(category: BusinessCategory) {
            binding.category = category
            binding.isSelected = category == (binding.root.tag as BusinessCategory?)
            binding.onClick = { v ->
                v.tag = category
                onItemClick?.invoke(category)
            }
            binding.executePendingBindings()
        }
    }
}

class LocationSelectAdapter : androidx.recyclerview.widget.ListAdapter<NairobiArea, LocationSelectAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<NairobiArea>() {
        override fun areItemsTheSame(oldItem: NairobiArea, newItem: NairobiArea): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: NairobiArea, newItem: NairobiArea): Boolean = oldItem == newItem
    }
) {
    
    var onItemClick: ((NairobiArea) -> Unit)? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemLocationSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemLocationSelectBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(location: NairobiArea) {
            binding.location = location
            binding.isSelected = location == (binding.root.tag as NairobiArea?)
            binding.onClick = { v ->
                v.tag = location
                onItemClick?.invoke(location)
            }
            binding.executePendingBindings()
        }
    }
}