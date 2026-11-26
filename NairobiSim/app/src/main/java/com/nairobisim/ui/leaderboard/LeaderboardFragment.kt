package com.nairobisim.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nairobisim.R
import com.nairobisim.databinding.FragmentLeaderboardBinding
import com.nairobisim.data.repository.LeaderboardRepository
import com.nairobisim.model.LeaderboardEntry
import com.nairobisim.ui.leaderboard.LeaderboardTab

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LeaderboardViewModel by viewModels()

    private val adapter = LeaderboardAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupTabButtons()
    }

    private fun setupRecyclerView() {
        binding.leaderboardRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapter
        }
    }

    private fun observeViewModel() {
        viewModel.leaderboard.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
        }

        viewModel.weeklyLeaderboard.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
        }

        viewModel.playerRank.observe(viewLifecycleOwner) { rank ->
            binding.yourRankText.text = "Your Rank: #$rank"
        }

        viewModel.wealthBreakdown.observe(viewLifecycleOwner) { breakdown ->
            if (breakdown != null) {
                showWealthBreakdown(breakdown)
            }
        }

        viewModel.selectedTab.observe(viewLifecycleOwner) { tab ->
            updateTabUI(tab)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.leaderboardRecycler.visibility = if (loading) View.GONE else View.VISIBLE
        }
    }

    private fun setupTabButtons() {
        binding.tabOverall.setOnClickListener { viewModel.setTab(LeaderboardTab.OVERALL) }
        binding.tabWeekly.setOnClickListener { viewModel.setTab(LeaderboardTab.WEEKLY) }
        binding.tabYourRank.setOnClickListener { viewModel.setTab(LeaderboardTab.YOUR_RANK) }
    }

    private fun updateTabUI(tab: LeaderboardTab) {
        binding.tabOverall.setBackgroundResource(if (tab == LeaderboardTab.OVERALL) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected)
        binding.tabWeekly.setBackgroundResource(if (tab == LeaderboardTab.WEEKLY) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected)
        binding.tabYourRank.setBackgroundResource(if (tab == LeaderboardTab.YOUR_RANK) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected)

        binding.leaderboardRecycler.visibility = if (tab == LeaderboardTab.YOUR_RANK) View.GONE else View.VISIBLE
        binding.wealthBreakdownCard.visibility = if (tab == LeaderboardTab.YOUR_RANK) View.VISIBLE else View.GONE
    }

    private fun showWealthBreakdown(breakdown: LeaderboardRepository.WealthBreakdown) {
        binding.wealthTotal.text = "Total Net Worth: KES ${String.format("%,d", breakdown.totalNetWorth)}"
        binding.wealthCash.text = "Cash & Bank: KES ${String.format("%,d", breakdown.cash)}"
        binding.wealthBusinesses.text = "Businesses: KES ${String.format("%,d", breakdown.businesses)}"
        binding.wealthVehicles.text = "Vehicles: KES ${String.format("%,d", breakdown.vehicles)}"
        binding.wealthStocks.text = "Stocks: KES ${String.format("%,d", breakdown.stocks)}"
        binding.wealthForex.text = "Forex: KES ${String.format("%,d", breakdown.forex)}"
        binding.wealthOther.text = "Crypto & Other: KES ${String.format("%,d", breakdown.other)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LeaderboardAdapter : androidx.recyclerview.widget.ListAdapter<com.nairobisim.model.LeaderboardEntry, LeaderboardAdapter.ViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<com.nairobisim.model.LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: com.nairobisim.model.LeaderboardEntry, newItem: com.nairobisim.model.LeaderboardEntry): Boolean = oldItem.playerId == newItem.playerId
        override fun areContentsTheSame(oldItem: com.nairobisim.model.LeaderboardEntry, newItem: com.nairobisim.model.LeaderboardEntry): Boolean = oldItem == newItem
    }
) {

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.rank_text)
        val nameText: TextView = view.findViewById(R.id.name_text)
        val badgeText: TextView = view.findViewById(R.id.badge_text)
        val netWorthText: TextView = view.findViewById(R.id.net_worth_text)
        val detailsText: TextView = view.findViewById(R.id.details_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        holder.rankText.text = "#${entry.rank}"
        holder.nameText.text = entry.playerName
        holder.badgeText.text = entry.badge
        holder.netWorthText.text = "KES ${String.format("%,d", entry.totalNetWorth)}"
        holder.detailsText.text = "Level ${entry.level} • ${entry.businessCount} businesses • KES ${String.format("%,d", entry.kesBalance)} cash"
    }
}