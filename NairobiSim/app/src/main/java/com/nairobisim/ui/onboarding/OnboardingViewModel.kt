package com.nairobisim.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.OnboardingRepository
import com.nairobisim.data.repository.OnboardingBackground
import com.nairobisim.data.repository.OnboardingBusinessChoice
import com.nairobisim.data.repository.OnboardingLocationChoice
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OnboardingRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).playerProfileDao(),
        com.nairobisim.data.repository.SimulationRepository(
            com.nairobisim.data.database.AppDatabase.getDatabase(application)
        )
    )

    // Step state
    private val _currentStep = MutableLiveData<OnboardingStep>(OnboardingStep.WELCOME)
    val currentStep: LiveData<OnboardingStep> = _currentStep

    // User selections
    private val _playerName = MutableLiveData<String>("")
    val playerName: LiveData<String> = _playerName

    private val _selectedBackground = MutableLiveData<OnboardingBackground?>(null)
    val selectedBackground: LiveData<OnboardingBackground?> = _selectedBackground

    private val _selectedLocation = MutableLiveData<OnboardingLocationChoice?>(null)
    val selectedLocation: LiveData<OnboardingLocationChoice?> = _selectedLocation

    private val _selectedBusiness = MutableLiveData<OnboardingBusinessChoice?>(null)
    val selectedBusiness: LiveData<OnboardingBusinessChoice?> = _selectedBusiness

    // UI state
    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _success = MutableLiveData<String?>(null)
    val success: LiveData<String?> = _success

    // Data lists
    val backgrounds = repository.backgroundStories
    val locations = repository.nairobiAreas
    val businesses = repository.startingBusinesses

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            val isComplete = repository.isOnboardingComplete("player_1")
            _currentStep.postValue(if (isComplete) OnboardingStep.COMPLETE else OnboardingStep.WELCOME)
        }
    }

    fun nextStep() {
        val current = _currentStep.value
        when (current) {
            OnboardingStep.WELCOME -> _currentStep.value = OnboardingStep.NAME
            OnboardingStep.NAME -> _currentStep.value = OnboardingStep.BACKGROUND
            OnboardingStep.BACKGROUND -> _currentStep.value = OnboardingStep.LOCATION
            OnboardingStep.LOCATION -> _currentStep.value = OnboardingStep.BUSINESS
            OnboardingStep.BUSINESS -> completeOnboarding()
            OnboardingStep.COMPLETE, null -> { /* already done or not started */ }
        }
    }

    fun previousStep() {
        val current = _currentStep.value
        when (current) {
            OnboardingStep.NAME -> _currentStep.value = OnboardingStep.WELCOME
            OnboardingStep.BACKGROUND -> _currentStep.value = OnboardingStep.NAME
            OnboardingStep.LOCATION -> _currentStep.value = OnboardingStep.BACKGROUND
            OnboardingStep.BUSINESS -> _currentStep.value = OnboardingStep.LOCATION
            OnboardingStep.WELCOME, OnboardingStep.COMPLETE, null -> { /* stay */ }
        }
    }

    fun setPlayerName(name: String) {
        _playerName.value = name.trim()
    }

    fun selectBackground(background: OnboardingBackground?) {
        _selectedBackground.value = background
    }

    fun selectLocation(location: OnboardingLocationChoice?) {
        _selectedLocation.value = location
    }

    fun selectBusiness(business: OnboardingBusinessChoice?) {
        _selectedBusiness.value = business
    }

    fun canProceed(): Boolean {
        val current = _currentStep.value
        return when (current) {
            OnboardingStep.WELCOME -> true
            OnboardingStep.NAME -> _playerName.value?.isNotBlank() ?: false
            OnboardingStep.BACKGROUND -> _selectedBackground.value != null
            OnboardingStep.LOCATION -> _selectedLocation.value != null
            OnboardingStep.BUSINESS -> _selectedBusiness.value != null
            OnboardingStep.COMPLETE, null -> false
        }
    }

    fun getEstimatedStartingBalance(): Long {
        val base = 50000L
        val backgroundBonus = _selectedBackground.value?.bonus ?: 0L
        val businessCost = _selectedBusiness.value?.startingCapital ?: 0L
        return (base + backgroundBonus - businessCost).coerceAtLeast(0)
    }

    private fun completeOnboarding() {
        val name = _playerName.value ?: return
        val background = _selectedBackground.value ?: return
        val location = _selectedLocation.value ?: return
        val business = _selectedBusiness.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.completeOnboarding("player_1", name, background.id, location.id, business.id)
                if (result) {
                    _success.value = "Welcome to Nairobi, $name! Your business journey begins now."
                    _currentStep.value = OnboardingStep.COMPLETE
                } else {
                    _error.value = "Failed to complete onboarding. Please try again."
                }
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

enum class OnboardingStep {
    WELCOME, NAME, BACKGROUND, LOCATION, BUSINESS, COMPLETE
}
