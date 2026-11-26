package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.SharpBoyEncounterDao
import com.nairobisim.model.*
import kotlinx.coroutines.*
import java.util.UUID
import kotlin.random.Random

class SharpBoysRepository(
    private val db: AppDatabase,
    private val sharpBoyEncounterDao: SharpBoyEncounterDao
) {

    private val random = kotlin.random.Random.Default

    // Generate a random encounter based on location and time
    suspend fun generateEncounter(location: String, timeOfDay: String): SharpBoyEncounter? {
        val scenarios = DEFAULT_SHARP_BOY_SCENARIOS.filter {
            it.location.contains(location, ignoreCase = true) ||
            it.timeOfDay.contains(timeOfDay, ignoreCase = true) ||
            random.nextDouble() < 0.3  // 30% chance of random encounter anywhere
        }

        if (scenarios.isEmpty()) return null

        val scenario = scenarios.random(random)
        val demandVariation = (scenario.demandedAmount * 0.5).toLong() .. (scenario.demandedAmount * 1.5).toLong()

        return scenario.copy(
            id = "sb_${UUID.randomUUID().toString().substring(0, 8)}",
            demandedAmount = random.nextLong(demandVariation.first, demandVariation.last + 1),
            minimumAcceptable = scenario.minimumAcceptable,
            successChance = scenario.successChance,
            violenceRisk = scenario.violenceRisk,
            phoneTheftRisk = scenario.phoneTheftRisk
        )
    }

    // Resolve an encounter based on player choice and stats
    suspend fun resolveEncounter(
        encounter: SharpBoyEncounter,
        playerReputation: Int,
        playerStreetCred: Int,
        playerKesBalance: Long,
        choice: StreetEncounterOutcome,
        hasPhone: Boolean = true
    ): SharpBoyOutcome {
        val boyType = encounter.boyType
        val adjustedSuccess = encounter.successChance + (playerStreetCred * 0.01)
        val adjustedViolence = encounter.violenceRisk - (playerReputation * 0.005)
        val adjustedTheft = encounter.phoneTheftRisk

        return when (choice) {
            StreetEncounterOutcome.PAID -> {
                val amountPaid = encounter.minimumAcceptable.coerceAtMost(playerKesBalance)
                SharpBoyOutcome(
                    outcome = StreetEncounterOutcome.PAID,
                    amountPaid = amountPaid,
                    phoneStolen = false,
                    injured = false,
                    medicalCost = 0,
                    reputationChange = -5,
                    message = "Paid KES $amountPaid to ${boyType.displayName}. They let you go, but you're lighter in the pocket.",
                    color = "#2E7D32"
                )
            }
            StreetEncounterOutcome.REFUSED -> {
                val escaped = random.nextDouble() < adjustedSuccess
                val caught = !escaped
                val violenceHit = caught && random.nextDouble() < adjustedViolence

                if (escaped) {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.REFUSED,
                        amountPaid = 0,
                        phoneStolen = false,
                        injured = false,
                        medicalCost = 0,
                        reputationChange = +10,
                        message = "You stood your ground and walked away. Street cred +10.",
                        color = "#1565C0"
                    )
                } else if (violenceHit) {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.INJURED,
                        amountPaid = encounter.demandedAmount.coerceAtMost(playerKesBalance),
                        phoneStolen = random.nextDouble() < adjustedTheft,
                        injured = true,
                        medicalCost = 5000,
                        reputationChange = -15,
                        message = "They caught you, beat you up, and took your money. Medical bill: KES 5,000.",
                        color = "#C62828"
                    )
                } else {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.PHONE_STOLEN,
                        amountPaid = 0,
                        phoneStolen = true,
                        injured = false,
                        medicalCost = 0,
                        reputationChange = -10,
                        message = "They snatched your phone and ran. You need to buy a new one.",
                        color = "#4A148C"
                    )
                }
            }
            StreetEncounterOutcome.NEGOTIATED -> {
                val negotiatedAmount = encounter.minimumAcceptable.coerceAtMost(playerKesBalance)
                val success = random.nextDouble() < 0.7

                if (success) {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.NEGOTIATED,
                        amountPaid = negotiatedAmount,
                        phoneStolen = false,
                        injured = false,
                        medicalCost = 0,
                        reputationChange = -2,
                        message = "Negotiated down to KES $negotiatedAmount. They accepted and let you go.",
                        color = "#FB8C00"
                    )
                } else {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.PAID,
                        amountPaid = encounter.demandedAmount.coerceAtMost(playerKesBalance),
                        phoneStolen = false,
                        injured = false,
                        medicalCost = 0,
                        reputationChange = -10,
                        message = "Negotiation failed. They demanded the full KES ${encounter.demandedAmount}.",
                        color = "#C62828"
                    )
                }
            }
            StreetEncounterOutcome.FOUGHT -> {
                val fightWon = random.nextDouble() < (0.3 + playerStreetCred * 0.005)

                if (fightWon) {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.FOUGHT,
                        amountPaid = 0,
                        phoneStolen = false,
                        injured = random.nextDouble() < 0.3,
                        medicalCost = if (random.nextDouble() < 0.3) 3000 else 0,
                        reputationChange = +25,
                        message = "You fought back and won! Street cred +25. They won't mess with you again.",
                        color = "#1A8A3A"
                    )
                } else {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.INJURED,
                        amountPaid = encounter.demandedAmount.coerceAtMost(playerKesBalance),
                        phoneStolen = true,
                        injured = true,
                        medicalCost = 8000,
                        reputationChange = -20,
                        message = "You lost the fight. They took your money, your phone, and left you hurt. Medical: KES 8,000.",
                        color = "#C62828"
                    )
                }
            }
            StreetEncounterOutcome.ESCAPED -> {
                val escaped = random.nextDouble() < (0.5 + playerStreetCred * 0.005)

                if (escaped) {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.ESCAPED,
                        amountPaid = 0,
                        phoneStolen = false,
                        injured = false,
                        medicalCost = 0,
                        reputationChange = +5,
                        message = "You ran fast and escaped. Street cred +5.",
                        color = "#6A1B9A"
                    )
                } else {
                    SharpBoyOutcome(
                        outcome = StreetEncounterOutcome.PAID,
                        amountPaid = encounter.minimumAcceptable.coerceAtMost(playerKesBalance),
                        phoneStolen = random.nextDouble() < adjustedTheft,
                        injured = random.nextDouble() < 0.2,
                        medicalCost = if (random.nextDouble() < 0.2) 4000 else 0,
                        reputationChange = -10,
                        message = "They caught you while running. You paid KES ${encounter.minimumAcceptable} to get away.",
                        color = "#C62828"
                    )
                }
            }
            else -> SharpBoyOutcome(
                outcome = StreetEncounterOutcome.PAID,
                amountPaid = encounter.minimumAcceptable.coerceAtMost(playerKesBalance),
                phoneStolen = false,
                injured = false,
                medicalCost = 0,
                reputationChange = -5,
                message = "Paid KES ${encounter.minimumAcceptable} to make them go away.",
                color = "#2E7D32"
            )
        }
    }

    // Save encounter to database
    suspend fun saveEncounter(encounter: SharpBoyEncounter) {
        sharpBoyEncounterDao.insertEncounter(encounter)
    }

    // Get encounter history
    val encounterHistory = sharpBoyEncounterDao.getAllEncounters()

    suspend fun getEncounterCount(): Int = sharpBoyEncounterDao.getCount()
}
