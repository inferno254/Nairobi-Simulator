package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class BribeRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao
) {

    val defaultScenarios = DEFAULT_BRIBE_SCENARIOS

    suspend fun generateRandomEncounter(playerId: String): BribeEncounter? {
        val scenario = DEFAULT_BRIBE_SCENARIOS.random()
        val encounter = BribeEncounter(
            id = "bribe_${System.currentTimeMillis()}",
            type = scenario.type,
            authority = scenario.authority,
            location = scenario.location,
            demandedAmount = scenario.demandedAmount,
            minimumAcceptable = scenario.minimumAcceptable,
            successChance = scenario.successChance,
            reputationRisk = scenario.reputationRisk,
            consequenceDescription = scenario.consequenceDescription,
            timestamp = System.currentTimeMillis(),
            isResolved = false,
            wasPaid = false,
            amountPaid = 0,
            wasCaught = false
        )
        return encounter
    }

    suspend fun resolveBribe(encounter: BribeEncounter, pay: Boolean, amount: Long): BribeOutcome {
        val profile = playerProfileDao.getProfileOnce("player_1") ?: return BribeOutcome(
            success = false, amountPaid = 0, wasCaught = false,
            message = "Player profile not found"
        )

        if (pay) {
            val balance = profile.kesBalance ?: 0
            if (balance < amount) {
                return BribeOutcome(
                    success = false, amountPaid = 0, wasCaught = false,
                    message = "Insufficient funds. Need KES $amount"
                )
            }

            val caught = Random.nextDouble() < encounter.reputationRisk
            val success = !caught && Random.nextDouble() < encounter.successChance

            profile.kesBalance = balance - amount
            profile.reputation = if (caught) maxOf(0, profile.reputation - 20) else profile.reputation + 5
            profile.totalNetWorth = profile.calculateNetWorth()

            playerProfileDao.updateProfile(profile)

            val resolvedEncounter = encounter.copy(
                isResolved = true, wasPaid = true, amountPaid = amount, wasCaught = caught
            )
            saveEncounter(resolvedEncounter)

            return if (caught) {
                val fine = amount * 2
                BribeOutcome(
                    success = false, amountPaid = amount, wasCaught = true,
                    fineAmount = fine, reputationImpact = -30,
                    message = "Caught! Paid KES $amount bribe + KES $fine fine. Reputation damaged."
                )
            } else if (success) {
                BribeOutcome(
                    success = true, amountPaid = amount, wasCaught = false,
                    message = "Bribe accepted. Problem resolved quietly."
                )
            } else {
                BribeOutcome(
                    success = false, amountPaid = amount, wasCaught = false,
                    message = "Bribe rejected. Try again or face consequences."
                )
            }
        } else {
            val caught = Random.nextDouble() < 0.3
            val success = !caught

            profile.reputation = if (caught) maxOf(0, profile.reputation - 10) else profile.reputation + 10
            profile.totalNetWorth = profile.calculateNetWorth()
            playerProfileDao.updateProfile(profile)

            val resolvedEncounter = encounter.copy(
                isResolved = true, wasPaid = false, amountPaid = 0, wasCaught = caught
            )
            saveEncounter(resolvedEncounter)

            return if (caught) {
                BribeOutcome(
                    success = false, amountPaid = 0, wasCaught = true,
                    reputationImpact = -10,
                    message = "Refused to pay. Authorities took notice. Reputation damaged."
                )
            } else {
                BribeOutcome(
                    success = true, amountPaid = 0, wasCaught = false,
                    message = "Refused to pay. No consequences this time."
                )
            }
        }
    }

    private suspend fun saveEncounter(encounter: BribeEncounter) {
        val profile = playerProfileDao.getProfileOnce("player_1") ?: return
        val updatedHistory = profile.bribeHistory + encounter
        playerProfileDao.updateProfile(profile.copy(bribeHistory = updatedHistory))
    }

    fun getEncounterDescription(type: BribeType): String = when (type) {
        BribeType.TRAFFIC_STOP -> "Police officer flags you down at a roadblock."
        BribeType.PERMIT_APPROVAL -> "County officer delays your business permit."
        BribeType.LICENSE_RENEWAL -> "License renewal officer demands 'processing fee'."
        BribeType.TAX_AUDIT -> "KRA officer wants to 'settle' your tax audit."
        BribeType.CUSTOMS_CLEARANCE -> "Customs officer holds your imported goods."
        BribeType.HEALTH_INSPECTION -> "Health inspector finds 'issues' at your premises."
        BribeType.BUILDING_APPROVAL -> "Planning officer stalls your construction approval."
    }
}
