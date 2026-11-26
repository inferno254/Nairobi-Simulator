package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class NPCEncounterRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao
) {

    val economy = com.nairobisim.model.npcEconomy

    suspend fun generateRandomEncounter(playerId: String, preferredRole: NPCJobRole? = null): NPCEncounter? {
        val role = preferredRole ?: economy.getCorruptNPCs().random().jobRole
        return economy.generateEncounterForPlayer(role, playerId)
    }

    suspend fun resolveEncounter(encounter: NPCEncounter, pay: Boolean, amount: Long): EncounterOutcome {
        val profile = playerProfileDao.getProfileOnce("player_1") ?: return EncounterOutcome(
            encounterId = encounter.id,
            npcName = encounter.npc.name,
            npcRole = encounter.npc.jobRole,
            wasPaid = pay,
            amountPaid = if (pay) amount else 0,
            wasCaught = false,
            reputationChange = 0,
            message = "Player profile not found"
        )

        val outcome = economy.resolveEncounter(encounter, pay, amount)

        if (outcome.wasPaid) {
            val balance = profile.kesBalance ?: 0
            val totalCost = outcome.amountPaid + outcome.fineAmount
            if (balance < totalCost) {
                return outcome.copy(message = "Insufficient funds. Needed KES $totalCost")
            }
            profile.kesBalance = balance - totalCost
        }

        profile.reputation = (profile.reputation ?: 0) + outcome.reputationChange
        profile.totalNetWorth = profile.calculateNetWorth()

        // Track bribe history
        if (outcome.wasPaid && outcome.amountPaid > 0) {
            val bribe = com.nairobisim.model.BribeEncounter(
                id = "bribe_${System.currentTimeMillis()}",
                type = com.nairobisim.model.BribeType.TRAFFIC_STOP,
                authority = outcome.npcName,
                location = encounter.npc.jobRole.encounterLocation,
                demandedAmount = encounter.bribeDemanded,
                minimumAcceptable = encounter.minimumAcceptable,
                successChance = encounter.successChance,
                reputationRisk = encounter.reputationRisk,
                consequenceDescription = outcome.message,
                timestamp = System.currentTimeMillis(),
                isResolved = true,
                wasPaid = true,
                amountPaid = outcome.amountPaid,
                wasCaught = outcome.wasCaught
            )
            val history = profile.bribeHistory + bribe
            playerProfileDao.updateProfile(profile.copy(
                bribeHistory = history,
                kesBalance = profile.kesBalance,
                reputation = profile.reputation,
                totalNetWorth = profile.totalNetWorth
            ))
        } else {
            playerProfileDao.updateProfile(profile.copy(
                kesBalance = profile.kesBalance,
                reputation = profile.reputation,
                totalNetWorth = profile.totalNetWorth
            ))
        }

        return outcome
    }

    fun getAllNPCs(): List<NPCCharacterWithJob> = economy.getAllNPCs()

    fun getNPCsByRole(role: NPCJobRole): List<NPCCharacterWithJob> = economy.getNPCsByRole(role)

    fun getCorruptNPCs(): List<NPCCharacterWithJob> = economy.getCorruptNPCs()

    fun getTotalBribesInCity(): Long = economy.getTotalBribesCollected()

    fun getNPCReputation(npcId: String): Int = economy.getNPCReputation(npcId)
}
