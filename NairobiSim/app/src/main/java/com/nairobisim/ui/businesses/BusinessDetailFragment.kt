package com.nairobisim.ui.businesses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentBusinessDetailBinding
import com.nairobisim.model.Business
import com.nairobisim.model.Employee
import com.nairobisim.model.BusinessAsset
import com.nairobisim.ui.main.MainViewModel

class BusinessDetailFragment : Fragment() {
    
    private var _binding: FragmentBusinessDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private val args: BusinessDetailFragmentArgs by navArgs()
    
    private val employeeAdapter = EmployeeAdapter()
    private val assetAdapter = AssetAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeBusiness()
    }
    
    private fun setupRecyclerViews() {
        binding.employeesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = employeeAdapter
        }
        
        binding.assetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = assetAdapter
        }
    }
    
    private fun observeBusiness() {
        viewModel.activeBusinesses.observe(viewLifecycleOwner) { businesses ->
            val business = businesses.firstOrNull { it.id == args.businessId }
            business?.let {
                binding.business = it
                binding.viewModel = viewModel
                binding.executePendingBindings()
                
                employeeAdapter.submitList(it.employees)
                assetAdapter.submitList(it.assets)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class EmployeeAdapter : androidx.recyclerview.widget.ListAdapter<Employee, EmployeeAdapter.EmployeeViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Employee>() {
        override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean = oldItem == newItem
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = com.nairobisim.databinding.ItemEmployeeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return EmployeeViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class EmployeeViewHolder(private val binding: com.nairobisim.databinding.ItemEmployeeBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(employee: Employee) {
            binding.employee = employee
            binding.viewModel = (requireContext().applicationContext as com.nairobisim.NairobiSimApplication).let {
                androidx.lifecycle.ViewModelProvider(it)[com.nairobisim.ui.main.MainViewModel::class.java]
            }
            binding.executePendingBindings()
        }
    }
}

class AssetAdapter : androidx.recyclerview.widget.ListAdapter<BusinessAsset, AssetAdapter.AssetViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<BusinessAsset>() {
        override fun areItemsTheSame(oldItem: BusinessAsset, newItem: BusinessAsset): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BusinessAsset, newItem: BusinessAsset): Boolean = oldItem == newItem
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = com.nairobisim.databinding.ItemAssetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return AssetViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class AssetViewHolder(private val binding: com.nairobisim.databinding.ItemAssetBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(asset: BusinessAsset) {
            binding.asset = asset
            binding.viewModel = (requireContext().applicationContext as com.nairobisim.NairobiSimApplication).let {
                androidx.lifecycle.ViewModelProvider(it)[com.nairobisim.ui.main.MainViewModel::class.java]
            }
            binding.executePendingBindings()
        }
    }
}