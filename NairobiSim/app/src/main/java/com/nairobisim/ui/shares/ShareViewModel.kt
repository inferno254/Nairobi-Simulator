package com.nairobisim.ui.shares

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.ShareRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class ShareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShareRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).playerProfileDao(),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).shareTradeDao()
    )

    // UI State
    val _prices = MutableLiveData<Map<String, Double>>()
    val prices: LiveData<Map<String, Double>> = _prices

    val _companies = MutableLiveData<List<ShareCompany>>(NAIROBI_SHARE_MARKET)
    val companies: LiveData<List<ShareCompany>> = _companies

    val _openPositions = MutableLiveData<List<ShareTrade>>(emptyList())
    val openPositions: LiveData<List<ShareTrade>> = _openPositions

    val _tradeHistory = MutableLiveData<List<ShareTrade>>(emptyList())
    val tradeHistory: LiveData<List<ShareTrade>> = _tradeHistory

    val _balance = MutableLiveData<Double>(0.0)
    val balance: LiveData<Double> = _balance

    val _selectedCompany = MutableLiveData<ShareCompany?>(null)
    val selectedCompany: LiveData<ShareCompany?> = _selectedCompany

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _success = MutableLiveData<String?>(null)
    val success: LiveData<String?> = _success

    init {
        loadData()
        startPriceSimulation()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bal = repository.getPlayerShareBalance("player_1")
                _balance.value = bal
                _prices.value = repository.getAllPrices()
                _openPositions.value = repository.getOpenPositions()
                _tradeHistory.value = repository.getTradeHistory()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCompany(company: ShareCompany) {
        _selectedCompany.value = company
    }

    private fun startPriceSimulation() {
        for (company in NAIROBI_SHARE_MARKET) {
            viewModelScope.launch {
                while (true) {
                    repository.simulatePriceChange(company.symbol)
                    _prices.value = repository.getAllPrices()
                    kotlinx.coroutines.delay(1000L)
                }
            }
        }
    }

    fun buyShares(symbol: String, shares: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val price = repository.getCurrentPrice(symbol)
                repository.buyShares(symbol, shares, price)
                _balance.value = repository.getPlayerShareBalance("player_1")
                _openPositions.value = repository.getOpenPositions()
                _success.value = "Bought $shares shares of $symbol at KES $price each"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sellShares(symbol: String, shares: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.sellShares(symbol, shares)
                _balance.value = repository.getPlayerShareBalance("player_1")
                _openPositions.value = repository.getOpenPositions()
                _success.value = "Sold $shares shares of $symbol"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
    fun clearSuccess() { _success.value = null }
}
