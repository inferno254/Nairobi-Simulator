package com.nairobisim.ui.sharpboys

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.SharpBoysRepository
import com.nairobisim.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SharpBoysViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SharpBoysRepository(db, db.sharpBoyEncounterDao())

    // UI State
    val _currentEncounter = MutableLiveData<SharpBoyEncounter?>(null)
    val currentEncounter: LiveData<SharpBoyEncounter?> = _currentEncounter

    val _encounterResult = MutableLiveData<SharpBoyOutcome?>(null)
    val encounterResult: LiveData<SharpBoyOutcome?> = _encounterResult

    val _encounterHistory = MutableLiveData<List<SharpBoyEncounter>>(emptyList())
    val encounterHistory: LiveData<List<SharpBoyEncounter>> = _encounterHistory

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _streetCred = MutableLiveData<Int>(0)
    val streetCred: LiveData<Int> = _streetCred

    val _showEncounter = MutableLiveData<Boolean>(false)
    val showEncounter: LiveData<Boolean> = _showEncounter

    // Player state (injected from MainViewModel via constructor or set directly)
    var playerReputation: Int = 0
    var playerStreetCred: Int = 0
    var playerKesBalance: Long = 0
    var playerHasPhone: Boolean = true

    init {
        loadEncounterHistory()
    }

    fun setPlayerState(reputation: Int, streetCred: Int, kesBalance: Long, hasPhone: Boolean) {
        playerReputation = reputation
        playerStreetCred = streetCred
        playerKesBalance = kesBalance
        playerHasPhone = hasPhone
        _streetCred.postValue(streetCred)
    }

    fun generateRandomEncounter(location: String, timeOfDay: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val encounter = repository.generateEncounter(location, timeOfDay)
                if (encounter != null) {
                    _currentEncounter.postValue(encounter)
                    _showEncounter.postValue(true)
                    repository.saveEncounter(encounter)
                } else {
                    _error.postValue("The streets are quiet today. No encounters.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to generate encounter")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun resolveEncounter(choice: StreetEncounterOutcome) {
        val encounter = _currentEncounter.value ?: return
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.resolveEncounter(
                    encounter,
                    playerReputation,
                    playerStreetCred,
                    playerKesBalance,
                    choice,
                    playerHasPhone
                )
                _encounterResult.postValue(result)

                // Update player state based on outcome
                playerKesBalance -= result.amountPaid
                playerStreetCred += result.reputationChange
                playerReputation += result.reputationChange
                if (result.phoneStolen) playerHasPhone = false
                _streetCred.postValue(playerStreetCred)

                // Mark encounter as resolved
                val resolved = encounter.copy(isResolved = true, outcome = choice, amountPaid = result.amountPaid, phoneStolen = result.phoneStolen, injured = result.injured, medicalCost = result.medicalCost)
                repository.saveEncounter(resolved)

                loadEncounterHistory()
                _showEncounter.postValue(false)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to resolve encounter")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadEncounterHistory() {
        viewModelScope.launch {
            try {
                val history = repository.encounterHistory.first()
                _encounterHistory.postValue(history)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to load history")
            }
        }
    }

    fun dismissResult() {
        _encounterResult.postValue(null)
    }

    fun clearError() { _error.value = null }
}
