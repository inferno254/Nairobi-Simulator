package com.nairobisim.ui.bribery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.BribeRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class BribeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BribeRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).playerProfileDao()
    )

    val _currentEncounter = MutableLiveData<BribeEncounter?>(null)
    val currentEncounter: LiveData<BribeEncounter?> = _currentEncounter

    val _outcome = MutableLiveData<BribeOutcome?>(null)
    val outcome: LiveData<BribeOutcome?> = _outcome

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun generateEncounter() {
        viewModelScope.launch {
            _isLoading.value = true
            _outcome.value = null
            try {
                val encounter = repository.generateRandomEncounter("player_1")
                _currentEncounter.value = encounter
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resolveBribe(pay: Boolean, amount: Long) {
        val encounter = _currentEncounter.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.resolveBribe(encounter, pay, amount)
                _outcome.value = result
                _currentEncounter.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissOutcome() {
        _outcome.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun getEncounterDescription(type: BribeType): String = repository.getEncounterDescription(type)
}
