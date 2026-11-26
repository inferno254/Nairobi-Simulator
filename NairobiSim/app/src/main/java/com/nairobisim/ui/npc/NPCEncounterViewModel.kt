package com.nairobisim.ui.npc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.NPCEncounterRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class NPCEncounterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NPCEncounterRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).playerProfileDao()
    )

    val _currentEncounter = MutableLiveData<NPCEncounter?>(null)
    val currentEncounter: LiveData<NPCEncounter?> = _currentEncounter

    val _outcome = MutableLiveData<EncounterOutcome?>(null)
    val outcome: LiveData<EncounterOutcome?> = _outcome

    val _allNPCs = MutableLiveData<List<NPCCharacterWithJob>>(repository.getAllNPCs())
    val allNPCs: LiveData<List<NPCCharacterWithJob>> = _allNPCs

    val _npcsByRole = MutableLiveData<Map<NPCJobRole, List<NPCCharacterWithJob>>>(emptyMap())
    val npcsByRole: LiveData<Map<NPCJobRole, List<NPCCharacterWithJob>>> = _npcsByRole

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _totalBribes = MutableLiveData<Long>(0L)
    val totalBribes: LiveData<Long> = _totalBribes

    init {
        loadNPCs()
    }

    private fun loadNPCs() {
        _allNPCs.value = repository.getAllNPCs()
        val byRole = repository.getAllNPCs().groupBy { it.jobRole }
        _npcsByRole.value = byRole
        _totalBribes.value = repository.getTotalBribesInCity()
    }

    fun generateEncounter(role: NPCJobRole?) {
        viewModelScope.launch {
            _isLoading.value = true
            _outcome.value = null
            try {
                val encounter = repository.generateRandomEncounter("player_1", role)
                _currentEncounter.value = encounter
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateRandomCorruptEncounter() {
        generateEncounter(null)
    }

    fun resolveEncounter(pay: Boolean, amount: Long) {
        val encounter = _currentEncounter.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.resolveEncounter(encounter, pay, amount)
                _outcome.value = result
                _currentEncounter.value = null
                loadNPCs()
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

    fun getEncounterDescription(role: NPCJobRole): String = when (role) {
        NPCJobRole.TRAFFIC_POLICE -> "A police officer flags you down at a Nairobi roadblock."
        NPCJobRole.CUSTOMS_OFFICER -> "A customs officer at Mombasa port holds your imported goods."
        NPCJobRole.COUNTY_OFFICER -> "A Nairobi County officer delays your business permit."
        NPCJobRole.KRA_TAX_OFFICER -> "A KRA tax officer wants to 'settle' your tax audit."
        NPCJobRole.HEALTH_INSPECTOR -> "A health inspector found issues at your premises."
        NPCJobRole.BUILDING_INSPECTOR -> "A building inspector cites a violation at your site."
        NPCJobRole.LICENSING_OFFICER -> "A licensing officer can expedite your permit."
        NPCJobRole.IMMIGRATION_OFFICER -> "An immigration officer can process your permit."
        NPCJobRole.POLICE_OFFICER -> "A police officer wants to speak with you."
        NPCJobRole.POLITICIAN -> "A politician wants a 'contribution' for community projects."
    }

    fun getNPCsByRole(role: NPCJobRole): List<NPCCharacterWithJob> {
        return repository.getNPCsByRole(role)
    }
}
