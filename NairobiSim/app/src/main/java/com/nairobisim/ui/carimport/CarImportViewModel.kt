package com.nairobisim.ui.carimport

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.CarImportRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class CarImportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CarImportRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).playerProfileDao()
    )

    val _marketListings = MutableLiveData<List<ForeignCarListing>>(repository.foreignMarket)
    val marketListings: LiveData<List<ForeignCarListing>> = _marketListings

    val _dutyBreakdown = MutableLiveData<ImportDutyBreakdown?>(null)
    val dutyBreakdown: LiveData<ImportDutyBreakdown?> = _dutyBreakdown

    val _customsEncounter = MutableLiveData<CustomsEncounter?>(null)
    val customsEncounter: LiveData<CustomsEncounter?> = _customsEncounter

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _success = MutableLiveData<String?>(null)
    val success: LiveData<String?> = _success

    fun calculateDutyForListing(listing: ForeignCarListing) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val breakdown = repository.calculateDuty(listing)
                _dutyBreakdown.value = breakdown
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importCar(listing: ForeignCarListing) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val carId = repository.purchaseForeignCar(listing, "player_1")
                if (carId != null) {
                    _success.value = "Car purchased! Shipping to Mombasa port..."
                    // Auto-arrive at port after shipping
                    val encounter = repository.arriveAtPort(carId, "player_1")
                    _customsEncounter.value = encounter
                } else {
                    _error.value = "Insufficient funds to import this car"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resolveCustoms(payBribe: Boolean, bribeAmount: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.clearCustoms("import_pending", "player_1", payBribe, bribeAmount)
                if (result) {
                    _success.value = if (payBribe) "Customs cleared. Car ready for pickup!" else "Customs cleared without bribe. Car ready for pickup!"
                } else {
                    _error.value = "Customs clearance failed"
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

    fun formatDutyLabel(label: String, amount: Long): String = "$label: KES ${String.format("%,d", amount)}"
}
