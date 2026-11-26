package com.nairobisim.ui.cars

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.CarRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class CarViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val repository = CarRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(appContext),
        com.nairobisim.data.database.AppDatabase.getDatabase(appContext).playerProfileDao()
    )

    // UI State
    val _cars = MutableLiveData<List<CarModel>>(repository.availableCars)
    val cars: LiveData<List<CarModel>> = _cars

    val _brands = MutableLiveData<List<CarBrand>>(repository.carBrands)
    val brands: LiveData<List<CarBrand>> = _brands

    val _ownedCars = MutableLiveData<List<OwnedCar>>(emptyList())
    val ownedCars: LiveData<List<OwnedCar>> = _ownedCars

    val _balance = MutableLiveData<Long>(0)
    val balance: LiveData<Long> = _balance

    val _selectedCar = MutableLiveData<CarModel?>(null)
    val selectedCar: LiveData<CarModel?> = _selectedCar

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _success = MutableLiveData<String?>(null)
    val success: LiveData<String?> = _success

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = com.nairobisim.data.database.AppDatabase.getDatabase(appContext)
                    .playerProfileDao().getProfileOnce("player_1")
                _balance.value = profile?.kesBalance ?: 0
                _ownedCars.value = profile?.ownedCars ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCar(car: CarModel) {
        _selectedCar.value = car
    }

    fun buyCar(carModelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.buyCar(carModelId, "player_1")
                if (result) {
                    val profile = com.nairobisim.data.database.AppDatabase.getDatabase(appContext)
                        .playerProfileDao().getProfileOnce("player_1")
                    _balance.value = profile?.kesBalance ?: 0
                    _ownedCars.value = profile?.ownedCars ?: emptyList()
                    _success.value = "Car purchased successfully!"
                } else {
                    _error.value = "Cannot afford this car or car not found"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sellCar(carId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.sellCar(carId, "player_1")
                if (result) {
                    val profile = com.nairobisim.data.database.AppDatabase.getDatabase(appContext)
                        .playerProfileDao().getProfileOnce("player_1")
                    _balance.value = profile?.kesBalance ?: 0
                    _ownedCars.value = profile?.ownedCars ?: emptyList()
                    _success.value = "Car sold successfully!"
                } else {
                    _error.value = "Car not found"
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
