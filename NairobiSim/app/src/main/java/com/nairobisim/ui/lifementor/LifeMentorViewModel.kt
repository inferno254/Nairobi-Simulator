package com.nairobisim.ui.lifementor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.LifeMentorRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class LifeMentorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = LifeMentorRepository(
        db,
        db.smartPersonDao(),
        db.counselingSessionDao()
    )

    private val _cases = MutableLiveData<List<SmartPerson>>(emptyList())
    val cases: LiveData<List<SmartPerson>> = _cases

    private val _profile = MutableLiveData<MentorProfile>(MentorProfile())
    val profile: LiveData<MentorProfile> = _profile

    private val _result = MutableLiveData<CounselingResult?>(null)
    val result: LiveData<CounselingResult?> = _result

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val prof = repository.getProfile()
            _profile.postValue(prof)
            _cases.postValue(repository.generateCases(prof))
            _isLoading.value = false
        }
    }

    fun counsel(person: SmartPerson, path: LifePath, approach: PersuasionApproach) {
        viewModelScope.launch {
            _isLoading.value = true
            val prof = _profile.value ?: MentorProfile()
            val outcome = repository.convince(person, path, approach, prof)
            _result.postValue(outcome)
            refresh()
            _isLoading.value = false
        }
    }
}