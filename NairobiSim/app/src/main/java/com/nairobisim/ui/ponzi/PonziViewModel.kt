package com.nairobisim.ui.ponzi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.PonziRepository
import com.nairobisim.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PonziViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PonziRepository(db, db.ponziSchemeDao(), db.ponziInvestmentDao())

    val _schemes = MutableLiveData<List<PonziScheme>>(emptyList())
    val schemes: LiveData<List<PonziScheme>> = _schemes

    val _investments = MutableLiveData<List<PonziInvestment>>(emptyList())
    val investments: LiveData<List<PonziInvestment>> = _investments

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _result = MutableLiveData<String?>(null)
    val result: LiveData<String?> = _result

    val _investResult = MutableLiveData<String?>(null)
    val investResult: LiveData<String?> = _investResult

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.checkSchemeCollapses()
            _schemes.postValue(repository.getAllSchemes().first())
            _investments.postValue(repository.getAllInvestments().first())
            _isLoading.value = false
        }
    }

    fun invest(schemeId: String, amount: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val investment = repository.invest(schemeId, amount)
                _investResult.postValue("Invested KES $amount in ${investment.schemeName}. Expected return: KES ${investment.expectedReturn}")
                refresh()
            } catch (e: Exception) {
                _investResult.postValue(e.message ?: "Investment failed")
            }
            _isLoading.value = false
        }
    }

    fun collectReturns(investmentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.collectReturns(investmentId)
                _result.postValue(result)
                refresh()
            } catch (e: Exception) {
                _result.postValue(e.message ?: "Failed to collect returns")
            }
            _isLoading.value = false
        }
    }

    fun clearResult() { _result.value = null }
    fun clearInvestResult() { _investResult.value = null }
}