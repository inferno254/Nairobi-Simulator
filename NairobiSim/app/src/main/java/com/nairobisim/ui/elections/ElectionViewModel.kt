package com.nairobisim.ui.elections

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.CampaignAction
import com.nairobisim.data.repository.ElectionRepository
import com.nairobisim.model.Candidate
import com.nairobisim.model.Election
import com.nairobisim.model.Office
import kotlinx.coroutines.launch

class ElectionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ElectionRepository(db)

    val elections: LiveData<List<Election>> = repository.elections.asLiveData()

    private val _toast = MutableLiveData<String?>(null)
    val toast: LiveData<String?> = _toast

    private val _playerName = MutableLiveData<String>("")
    val playerName: LiveData<String> = _playerName

    fun setPlayerName(name: String) { _playerName.value = name }

    fun startElection(office: Office) {
        viewModelScope.launch {
            repository.startElection(office)
            _toast.value = "Election started for ${office.displayName}"
        }
    }

    fun declare(electionId: String, party: String, manifesto: String) {
        viewModelScope.launch {
            val name = _playerName.value?.trim().orEmpty().ifBlank { "You" }
            val result = repository.playerDeclare(electionId, name, party, manifesto)
            _toast.value = if (result == "ok") "You are now a candidate!" else result
        }
    }

    fun campaign(electionId: String, action: CampaignAction) {
        viewModelScope.launch {
            val result = repository.campaign(electionId, action)
            _toast.value = if (result == "ok") "${action.displayName} done!" else result
        }
    }

    fun nextDay(electionId: String) {
        viewModelScope.launch { repository.tickDay(electionId) }
    }

    fun clearToast() { _toast.value = null }
}