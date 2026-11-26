package com.nairobisim.ui.betting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.BettingRepository
import com.nairobisim.data.repository.OddsRepository
import com.nairobisim.data.repository.KenyanBettingOdds
import com.nairobisim.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BettingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BettingRepository(db, db.bettingMatchDao(), db.betSlipDao(), db.bettingBalanceDao())
    private val oddsRepository = OddsRepository()

    val _matches = MutableLiveData<List<BettingMatch>>(emptyList())
    val matches: LiveData<List<BettingMatch>> = _matches

    val _bets = MutableLiveData<List<BetSlip>>(emptyList())
    val bets: LiveData<List<BetSlip>> = _bets

    val _balance = MutableLiveData<BettingBalance>(BettingBalance())
    val balance: LiveData<BettingBalance> = _balance

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _result = MutableLiveData<String?>(null)
    val result: LiveData<String?> = _result

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _matches.postValue(repository.getAvailableMatches().first())
            _bets.postValue(repository.getAllBets().first())
            _balance.postValue(repository.getBalance())
            _isLoading.value = false
        }
    }

    fun placeBet(match: BettingMatch, betType: BetType, selection: String, stake: Long, odds: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val slip = repository.placeBet(match.id, betType, selection, stake, odds)
                _result.postValue("Bet placed! KES $stake on $selection at ${odds}x odds")
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Failed to place bet")
            }
            _isLoading.value = false
        }
    }

    fun settleBet(slipId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val slip = repository.settleBet(slipId)
                if (slip.status == BetStatus.WON) {
                    _result.postValue("YOU WON! KES ${slip.actualWin}")
                } else {
                    _result.postValue("You lost KES ${slip.stake}")
                }
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Failed to settle bet")
            }
            _isLoading.value = false
        }
    }

    fun simulateResults() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.simulateMatchResults()
            _result.postValue("Match results simulated! Check your bets.")
            refresh()
            _isLoading.value = false
        }
    }

    fun addMoney(amount: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addToBettingWallet(amount)
            refresh()
            _isLoading.value = false
        }
    }

    fun clearResult() { _result.value = null }
}