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
import com.nairobisim.databinding.FragmentBusinessesBinding
import com.nairobisim.model.Business
import com.nairobisim.model.BusinessCategory
import com.nairobisim.ui.main.MainViewModel

class BusinessesFragment : Fragment() {
    
    private var _binding: FragmentBusinessesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private val activeAdapter = BusinessCardAdapter()
    private val categoryAdapter = BusinessCategoryAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeData()
        setupClickListeners()
    }
    
    private fun setupRecyclerViews() {
        binding.activeBusinessesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeAdapter
        }
        
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            categoryAdapter.submitList(BusinessCategory.values().toList())
        }
    }
    
    private fun observeData() {
        viewModel.activeBusinesses.observe(viewLifecycleOwner) { businesses ->
            activeAdapter.submitList(businesses)
            binding.activeBusinessesRecyclerView.visibility = if (businesses.isEmpty()) View.GONE else View.VISIBLE
        }
    }
    
    private fun setupClickListeners() {
        binding.btnStartNewBusiness.setOnClickListener {
            findNavController().navigate(R.id.startBusinessFragment)
        }
        
        activeAdapter.onItemClick = { business ->
            viewModel.selectBusiness(business)
            val bundle = Bundle()
            bundle.putString("businessId", business.id)
            findNavController().navigate(R.id.businessDetailFragment, bundle)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}