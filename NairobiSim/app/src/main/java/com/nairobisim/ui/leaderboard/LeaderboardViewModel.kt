package com.nairobisim.ui.leaderboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.LeaderboardRepository
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.LeaderboardEntry
import kotlinx.coroutines.launch

enum class LeaderboardTab {
    OVERALL, WEEKLY, YOUR_RANK
}

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LeaderboardRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).playerProfileDao()
    )

    val _leaderboard = MutableLiveData<List<LeaderboardEntry>>(emptyList())
    val leaderboard: LiveData<List<LeaderboardEntry>> = _leaderboard

    val _weeklyLeaderboard = MutableLiveData<List<LeaderboardEntry>>(emptyList())
    val weeklyLeaderboard: LiveData<List<LeaderboardEntry>> = _weeklyLeaderboard

    val _playerRank = MutableLiveData<Int>(0)
    val playerRank: LiveData<Int> = _playerRank

    val _wealthBreakdown = MutableLiveData<LeaderboardRepository.WealthBreakdown?>(null)
    val wealthBreakdown: LiveData<LeaderboardRepository.WealthBreakdown?> = _wealthBreakdown

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _selectedTab = MutableLiveData<LeaderboardTab>(LeaderboardTab.OVERALL)
    val selectedTab: LiveData<LeaderboardTab> = _selectedTab

    init {
        loadLeaderboard()
        loadPlayerRank()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val board = repository.getTopLeaderboard(100)
                _leaderboard.postValue(board)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadWeeklyLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val board = repository.getWeeklyLeaderboard(50)
                _weeklyLeaderboard.postValue(board)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadPlayerRank() {
        viewModelScope.launch {
            try {
                val rank = repository.getPlayerRank("player_1")
                _playerRank.postValue(rank)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    fun loadWealthBreakdown() {
        viewModelScope.launch {
            try {
                val breakdown = repository.getWealthBreakdown("player_1")
                _wealthBreakdown.postValue(breakdown)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    fun setTab(tab: LeaderboardTab) {
        _selectedTab.value = tab
        when (tab) {
            LeaderboardTab.OVERALL -> loadLeaderboard()
            LeaderboardTab.WEEKLY -> loadWeeklyLeaderboard()
            LeaderboardTab.YOUR_RANK -> {
                loadPlayerRank()
                loadWealthBreakdown()
            }
        }
    }

    fun refresh() {
        when (_selectedTab.value ?: LeaderboardTab.OVERALL) {
            LeaderboardTab.OVERALL -> loadLeaderboard()
            LeaderboardTab.WEEKLY -> loadWeeklyLeaderboard()
            LeaderboardTab.YOUR_RANK -> {
                loadPlayerRank()
                loadWealthBreakdown()
            }
        }
    }

    fun clearError() { _error.value = null }
}