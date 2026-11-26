package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class CarImportRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao
) {

    val foreignMarket = FOREIGN_CAR_MARKET

    suspend fun calculateDuty(listing: ForeignCarListing): ImportDutyBreakdown {
        return calculateImportDuty(listing.cifValue, listing.engineCapacity)
    }

    suspend fun purchaseForeignCar(listing: ForeignCarListing, playerId: String): String? {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return null
        val duty = calculateImportDuty(listing.cifValue, listing.engineCapacity)
        val totalCost = listing.priceKES + listing.sourceCountry.baseShippingCost + listing.auctionFee + duty.totalTaxes + duty.portCharges + duty.clearingAgentFee

        val balance = profile.kesBalance ?: 0
        if (balance < totalCost) return null

        val importId = "import_${System.currentTimeMillis()}"

        // Convert to OwnedCar and add to profile
        val ownedCar = OwnedCar(
            id = importId,
            carModelId = listing.id,
            purchasePrice = totalCost,
            purchaseDate = System.currentTimeMillis(),
            currentValue = listing.priceKES,
            condition = 100,
            mileageKm = 0,
            color = "Black",
            plateNumber = generatePlateNumber()
        )

        profile.kesBalance = balance - totalCost
        profile.ownedCars = profile.ownedCars + ownedCar
        profile.totalNetWorth = profile.calculateNetWorth()

        playerProfileDao.updateProfile(profile)
        return importId
    }

    suspend fun startShipping(carId: String, playerId: String): Boolean {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return false
        val carIndex = profile.ownedCars.indexOfFirst { it.id == carId }
        return carIndex >= 0
    }

    suspend fun arriveAtPort(carId: String, playerId: String): CustomsEncounter? {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return null
        val carIndex = profile.ownedCars.indexOfFirst { it.id == carId }
        if (carIndex < 0) return null

        val scenario = DEFAULT_CUSTOMS_SCENARIOS.random()
        return scenario
    }

    suspend fun clearCustoms(carId: String, playerId: String, payBribe: Boolean, bribeAmount: Long): Boolean {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return false
        val balance = profile.kesBalance ?: 0

        if (payBribe) {
            if (balance < bribeAmount) return false
            profile.kesBalance = balance - bribeAmount
        }

        val carIndex = profile.ownedCars.indexOfFirst { it.id == carId }
        if (carIndex < 0) return false

        return true
    }

    suspend fun getCarImportStatus(carId: String, playerId: String): ImportStatus? {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return null
        return ImportStatus.READY_FOR_PICKUP
    }

    private fun generatePlateNumber(): String {
        val prefix = listOf("KAA", "KAB", "KAC", "KAD", "KBK").random()
        val numbers = (1000..9999).random()
        val suffix = listOf("A", "B", "C", "D", "E").random()
        return "$prefix $numbers $suffix"
    }
}
