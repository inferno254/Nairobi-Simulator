package com.nairobisim.ui.chama

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
import com.nairobisim.databinding.FragmentChamaBinding
import com.nairobisim.model.Chama
import com.nairobisim.ui.main.MainViewModel

class ChamaFragment : Fragment() {
    
    private var _binding: FragmentChamaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    
    private val myChamaAdapter = MyChamaAdapter()
    private val availableChamaAdapter = AvailableChamaAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChamaBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeData()
        setupClickListeners()
    }
    
    private fun setupRecyclerViews() {
        binding.myChamaRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myChamaAdapter
        }
        
        binding.availableChamasRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = availableChamaAdapter
        }
    }
    
    private fun observeData() {
        viewModel.playerProfile.observe(viewLifecycleOwner) { profile ->
            profile?.chamas?.let { chamas ->
                myChamaAdapter.submitList(chamas.filter { it.isActive })
            }
            
            // Generate sample available chamas
            val available = listOf(
                Chama(
                    id = "chama_1",
                    name = "Nairobi Traders Chama",
                    description = "Traders pooling capital for bulk imports",
                    members = listOf(),
                    monthlyContribution = 10_000,
                    cycleDuration = 12,
                    currentCycle = 3,
                    totalPool = 1_200_000,
                    memberShares = emptyMap(),
                    maxMembers = 20,
                    isActive = true,
                    nextPayoutDate = "Jan 15, 2025"
                ),
                Chama(
                    id = "chama_2",
                    name = "Boda Boda Operators Union",
                    description = "Motorcycle taxi operators saving for fleet expansion",
                    members = listOf(),
                    monthlyContribution = 15_000,
                    cycleDuration = 10,
                    currentCycle = 1,
                    totalPool = 800_000,
                    memberShares = emptyMap(),
                    maxMembers = 15,
                    isActive = true,
                    nextPayoutDate = "Feb 1, 2025"
                ),
                Chama(
                    id = "chama_3",
                    name = "Tech Startups Investment Group",
                    description = "Early-stage tech founders investing in each other",
                    members = listOf(),
                    monthlyContribution = 50_000,
                    cycleDuration = 24,
                    currentCycle = 6,
                    totalPool = 5_000_000,
                    memberShares = emptyMap(),
                    maxMembers = 10,
                    isActive = true,
                    nextPayoutDate = "Mar 1, 2025"
                )
            )
            availableChamaAdapter.submitList(available)
            
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCreateChama.setOnClickListener {
            // TODO: Navigate to create chama screen
        }
        
        availableChamaAdapter.onItemClick = { chama ->
            // TODO: Implement join chama
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class MyChamaAdapter : androidx.recyclerview.widget.ListAdapter<Chama, MyChamaAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Chama>() {
        override fun areItemsTheSame(oldItem: Chama, newItem: Chama): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Chama, newItem: Chama): Boolean = oldItem == newItem
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemChamaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemChamaBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(chama: Chama) {
            binding.chama = chama
            binding.executePendingBindings()
        }
    }
}

class AvailableChamaAdapter : androidx.recyclerview.widget.ListAdapter<Chama, AvailableChamaAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Chama>() {
        override fun areItemsTheSame(oldItem: Chama, newItem: Chama): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Chama, newItem: Chama): Boolean = oldItem == newItem
    }
) {
    
    var onItemClick: ((Chama) -> Unit)? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.nairobisim.databinding.ItemChamaAvailableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: com.nairobisim.databinding.ItemChamaAvailableBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(chama: Chama) {
            binding.chama = chama
            binding.executePendingBindings()
        }
    }
}