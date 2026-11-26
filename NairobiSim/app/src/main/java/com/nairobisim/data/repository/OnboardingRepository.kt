package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.*
import kotlinx.coroutines.launch

class OnboardingRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao,
    private val simulationRepository: SimulationRepository
) {

    val startingBusinesses = listOf(
        OnboardingBusinessChoice(
            id = "kiosk",
            title = "Kiosk (Duuka)",
            description = "Small neighborhood shop. Low capital, steady income.",
            category = BusinessCategory.KIOSK,
            startingCapital = 50000,
            location = NairobiArea.KIBERA,
            icon = "@drawable/ic_business"
        ),
        OnboardingBusinessChoice(
            id = "matatu",
            title = "Matatu Sacco",
            description = "Public transport on Ngong Road. Higher risk, higher reward.",
            category = BusinessCategory.MATATU_SACCO,
            startingCapital = 500000,
            location = NairobiArea.NGONG_ROAD,
            icon = "@drawable/ic_business"
        ),
        OnboardingBusinessChoice(
            id = "cyber",
            title = "Cyber Cafe + M-Pesa",
            description = "Printing, browsing, and mobile money in Eastleigh.",
            category = BusinessCategory.CYBER_CAFE,
            startingCapital = 150000,
            location = NairobiArea.EASTLEIGH,
            icon = "@drawable/ic_business"
        ),
        OnboardingBusinessChoice(
            id = "choma",
            title = "Nyama Choma Joint",
            description = "Popular meat roasting spot in Kilimani.",
            category = BusinessCategory.RESTAURANT,
            startingCapital = 400000,
            location = NairobiArea.KILIMANI,
            icon = "@drawable/ic_business"
        ),
        OnboardingBusinessChoice(
            id = "boda",
            title = "Bodaboda Fleet",
            description = "Motorcycle taxi fleet in Dandora.",
            category = BusinessCategory.BODABODA_FLEET,
            startingCapital = 250000,
            location = NairobiArea.DANDORA,
            icon = "@drawable/ic_business"
        ),
        OnboardingBusinessChoice(
            id = "agrovet",
            title = "Agrovet Store",
            description = "Farm supplies and veterinary products in Limuru.",
            category = BusinessCategory.AGROVET,
            startingCapital = 200000,
            location = NairobiArea.LIMURU,
            icon = "@drawable/ic_business"
        )
    )

    val nairobiAreas = listOf(
        OnboardingLocationChoice(
            id = "kibera",
            name = "Kibera",
            description = "Africa's largest informal settlement. Massive market, low rent.",
            area = NairobiArea.KIBERA,
            rentMultiplier = 0.5,
            trafficMultiplier = 1.6,
            vibe = "Hustler's paradise"
        ),
        OnboardingLocationChoice(
            id = "cbd",
            name = "CBD",
            description = "Heart of Nairobi. High traffic, high rent, fierce competition.",
            area = NairobiArea.CBD,
            rentMultiplier = 3.0,
            trafficMultiplier = 1.5,
            vibe = "Big city energy"
        ),
        OnboardingLocationChoice(
            id = "westlands",
            name = "Westlands",
            description = "Expat hub with serious spending power.",
            area = NairobiArea.WESTLANDS,
            rentMultiplier = 2.5,
            trafficMultiplier = 1.4,
            vibe = "Upscale nightlife"
        ),
        OnboardingLocationChoice(
            id = "kilimani",
            name = "Kilimani",
            description = "Young professionals, apartments, and offices.",
            area = NairobiArea.KILIMANI,
            rentMultiplier = 2.0,
            trafficMultiplier = 1.4,
            vibe = "Modern Nairobi"
        ),
        OnboardingLocationChoice(
            id = "karen",
            name = "Karen",
            description = "Wealthy suburbs. High-end clients, expensive rent.",
            area = NairobiArea.KAREN,
            rentMultiplier = 3.5,
            trafficMultiplier = 1.2,
            vibe = "Old money"
        )
    )

    val backgroundStories = listOf(
        OnboardingBackground(
            id = "hustler",
            title = "The Hustler",
            description = "Started from nothing. Street smart and resilient.",
            bonus = 10000,
            reputationBonus = 5
        ),
        OnboardingBackground(
            id = "graduate",
            title = "University Graduate",
            description = "Fresh from campus with a business degree.",
            bonus = 20000,
            reputationBonus = 0
        ),
        OnboardingBackground(
            id = "diaspora",
            title = "Returning Diaspora",
            description = "Came back from abroad with savings and connections.",
            bonus = 100000,
            reputationBonus = 10
        ),
        OnboardingBackground(
            id = "family_business",
            title = "Family Business",
            description = "Inherited a small shop from your parents.",
            bonus = 50000,
            reputationBonus = 15
        )
    )

    suspend fun getPlayerProfile(playerId: String): PlayerProfile? = playerProfileDao.getProfileOnce(playerId)

    suspend fun completeOnboarding(
        playerId: String,
        playerName: String,
        backgroundId: String,
        locationId: String,
        businessId: String
    ): Boolean {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return false

        val background = backgroundStories.firstOrNull { it.id == backgroundId } ?: backgroundStories.first()
        val location = nairobiAreas.firstOrNull { it.id == locationId } ?: nairobiAreas.first()
        val business = startingBusinesses.firstOrNull { it.id == businessId } ?: startingBusinesses.first()

        val startingKes = 50000 + background.bonus
        val afterBusinessCapital = (startingKes - business.startingCapital).coerceAtLeast(0)

        val updatedProfile = profile.copy(
            playerName = playerName,
            kesBalance = afterBusinessCapital,
            reputation = background.reputationBonus,
            lastLogin = java.util.Date(),
            isProfileComplete = true
        )

        playerProfileDao.updateProfile(updatedProfile)

        // Start the chosen business via SimulationRepository
        val newBusiness = Business(
            id = "biz_${business.category.name.lowercase()}_${System.currentTimeMillis()}",
            name = "${business.title} (${location.name})",
            category = business.category,
            location = location.area,
            capitalInvested = business.startingCapital,
            unlockRequirements = UnlockRequirements()
        )
        newBusiness.monthlyRevenue = newBusiness.calculateRevenue()

        simulationRepository.startBusiness(newBusiness)

        return true
    }

    suspend fun isOnboardingComplete(playerId: String): Boolean {
        val profile = playerProfileDao.getProfileOnce(playerId)
        return profile?.isProfileComplete ?: false
    }
}

data class OnboardingBusinessChoice(
    val id: String,
    val title: String,
    val description: String,
    val category: BusinessCategory,
    val startingCapital: Long,
    val location: NairobiArea,
    val icon: String
)

data class OnboardingLocationChoice(
    val id: String,
    val name: String,
    val description: String,
    val area: NairobiArea,
    val rentMultiplier: Double,
    val trafficMultiplier: Double,
    val vibe: String
)

data class OnboardingBackground(
    val id: String,
    val title: String,
    val description: String,
    val bonus: Long,
    val reputationBonus: Int
)
