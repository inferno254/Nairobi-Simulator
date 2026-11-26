package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class CarRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao
) {

    val availableCars = CAR_MODELS
    val carBrands = CAR_BRANDS

    suspend fun buyCar(carModelId: String, playerId: String): Boolean {
        val carModel = CAR_MODELS.find { it.id == carModelId } ?: return false
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return false

        val balance = profile.kesBalance ?: 0
        if (balance < carModel.priceKES) return false

        val ownedCar = OwnedCar(
            id = "car_${System.currentTimeMillis()}",
            carModelId = carModelId,
            purchasePrice = carModel.priceKES,
            purchaseDate = System.currentTimeMillis(),
            currentValue = carModel.priceKES,
            condition = 100,
            mileageKm = 0,
            color = "Black",
            plateNumber = generatePlateNumber()
        )

        profile.kesBalance = balance - carModel.priceKES
        profile.ownedCars = profile.ownedCars + ownedCar
        profile.totalNetWorth = profile.calculateNetWorth()

        playerProfileDao.updateProfile(profile)
        return true
    }

    suspend fun sellCar(carId: String, playerId: String): Boolean {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return false
        val carIndex = profile.ownedCars.indexOfFirst { it.id == carId }
        if (carIndex < 0) return false

        val car = profile.ownedCars[carIndex]
        profile.kesBalance = (profile.kesBalance ?: 0) + car.currentValue
        profile.ownedCars = profile.ownedCars.filter { it.id != carId }
        profile.totalNetWorth = profile.calculateNetWorth()

        playerProfileDao.updateProfile(profile)
        return true
    }

    private fun generatePlateNumber(): String {
        val prefix = listOf("KAA", "KAB", "KAC", "KAD", "KBK").random()
        val numbers = (1000..9999).random()
        val suffix = listOf("A", "B", "C", "D", "E").random()
        return "$prefix $numbers $suffix"
    }

    fun getCarModel(id: String): CarModel? = CAR_MODELS.find { it.id == id }
    fun getBrand(id: String): CarBrand? = CAR_BRANDS.find { it.id == id }
}
