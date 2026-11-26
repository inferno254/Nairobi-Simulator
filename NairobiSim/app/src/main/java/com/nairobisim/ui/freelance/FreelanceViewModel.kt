package com.nairobisim.ui.freelance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.FreelanceRepository
import com.nairobisim.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FreelanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FreelanceRepository(db, db.freelanceContractDao())

    // UI State
    val _availableGigs = MutableLiveData<List<FreelanceGig>>(emptyList())
    val availableGigs: LiveData<List<FreelanceGig>> = _availableGigs

    val _activeContracts = MutableLiveData<List<FreelanceContract>>(emptyList())
    val activeContracts: LiveData<List<FreelanceContract>> = _activeContracts

    val _delayedContracts = MutableLiveData<List<FreelanceContract>>(emptyList())
    val delayedContracts: LiveData<List<FreelanceContract>> = _delayedContracts

    val _profile = MutableLiveData<FreelanceProfile>(FreelanceProfile())
    val profile: LiveData<FreelanceProfile> = _profile

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _result = MutableLiveData<FreelanceOutcome?>(null)
    val result: LiveData<FreelanceOutcome?> = _result

    var playerFreelanceProfile: FreelanceProfile = FreelanceProfile()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val gigs = repository.generateAvailableGigs(playerFreelanceProfile)
            _availableGigs.postValue(gigs)
            _profile.postValue(repository.getProfile())
            _isLoading.value = false
        }
    }

    fun observeContracts() {
        viewModelScope.launch {
            repository.getActiveContracts().first<List<FreelanceContract>>().let { _activeContracts.postValue(it) }
            repository.getDelayedContracts().first<List<FreelanceContract>>().let { _delayedContracts.postValue(it) }
        }
    }

    fun haggleForGig(gig: FreelanceGig, offer: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val outcome = repository.haggle(gig, offer, playerFreelanceProfile.levelFor(gig.skill))
            _result.postValue(outcome)
            if (outcome.success) {
                playerFreelanceProfile = playerFreelanceProfile.copy(
                    reputation = playerFreelanceProfile.reputation + outcome.reputationDelta
                )
                refresh()
            }
            _isLoading.value = false
        }
    }

    fun completeContract(contractId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val outcome = repository.completeContract(contractId)
            _result.postValue(outcome)
            if (outcome.success) {
                playerFreelanceProfile = playerFreelanceProfile.copy(
                    reputation = playerFreelanceProfile.reputation + outcome.reputationDelta
                )
                observeContracts()
                refresh()
            }
            _isLoading.value = false
        }
    }

    fun collectDelayedPayment(contractId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val outcome = repository.collectDelayedPayment(contractId)
            _result.postValue(outcome)
            if (outcome.success) {
                playerFreelanceProfile = playerFreelanceProfile.copy(
                    reputation = playerFreelanceProfile.reputation + outcome.reputationDelta
                )
                observeContracts()
                refresh()
            }
            _isLoading.value = false
        }
    }

    fun levelFor(skill: FreelanceSkill): FreelanceLevel {
        return playerFreelanceProfile.levelFor(skill)
    }

    fun xpFor(skill: FreelanceSkill): Int {
        return playerFreelanceProfile.xpFor(skill)
    }
}
