package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.FreelanceContractDao
import com.nairobisim.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class FreelanceRepository(
    private val db: AppDatabase,
    private val contractDao: FreelanceContractDao
) {

    private val random = Random.Default

    // Generate a fresh pool of available gigs each time
    fun generateAvailableGigs(playerLevel: FreelanceProfile): List<FreelanceGig> {
        return DEFAULT_FREELANCE_GIGS.map { gig ->
            val effectivePay = gig.effectivePay(playerLevel.levelFor(gig.skill))
            gig.copy(
                basePay = effectivePay.coerceAtLeast(gig.minPay),
                maxPay = (effectivePay * 1.3).toLong().coerceAtLeast(effectivePay)
            )
        }.filter { !it.isTaken }
    }

    // Haggling: player offers a price, client counters or accepts
    // Returns outcome with agreed pay, or null if client walks away
    suspend fun haggle(gig: FreelanceGig, playerOffer: Long, playerLevel: FreelanceLevel): FreelanceOutcome {
        if (!gig.canAffordSkill(playerLevel)) {
            return FreelanceOutcome(
                success = false,
                message = "Client rejected: your ${gig.skill.displayName} skill is too low for this gig.",
                payEarned = 0
            )
        }

        val client = gig.client
        val minAcceptable = gig.minPay
        val maxPay = gig.maxPay

        // Client decides based on offer vs min/max and haggle willingness
        val agreedPay: Long
        val clientAccepts: Boolean

        if (playerOffer >= maxPay) {
            agreedPay = maxPay
            clientAccepts = true
        } else if (playerOffer >= minAcceptable) {
            // Client may counter based on haggle willingness
            val counterChance = client.haggleWillingness
            if (random.nextDouble() < counterChance) {
                // Client counters somewhere between offer and max
                val counter = (playerOffer + ((maxPay - playerOffer) * 0.4)).toLong().coerceAtLeast(minAcceptable)
                agreedPay = counter.coerceAtLeast(playerOffer)
                clientAccepts = true
            } else {
                agreedPay = playerOffer
                clientAccepts = true
            }
        } else {
            // Below minimum - client may walk away
            if (random.nextDouble() < 0.3) {
                return FreelanceOutcome(
                    success = false,
                    message = "${client.name} walked away - your offer of KES $playerOffer was too low.",
                    payEarned = 0
                )
            }
            agreedPay = minAcceptable
            clientAccepts = true
        }

        // Competition: another freelancer might snatch the gig
        val competitionRoll = random.nextInt(1, 6)
        if (competitionRoll <= gig.competitionLevel) {
            return FreelanceOutcome(
                success = false,
                message = "Another freelancer snatched the gig before you could accept.",
                payEarned = 0
            )
        }

        // Create contract
        val contract = FreelanceContract(
            id = "fc_${UUID.randomUUID().toString().substring(0, 8)}",
            gigId = gig.id,
            gigTitle = gig.title,
            skill = gig.skill,
            agreedPay = agreedPay,
            status = FreelanceJobStatus.ACCEPTED,
            clientName = client.name,
            clientReliability = client.reliability,
            instantPayment = gig.instantPayment,
            startedAt = System.currentTimeMillis()
        )
        contractDao.insertContract(contract)

        // Determine if payment is delayed
        val delayed = !client.paysOnTime() && !gig.instantPayment
        val payEarned = if (delayed) 0L else agreedPay
        val xpGained = (agreedPay / 1000).toInt().coerceAtLeast(10)
        val repDelta = if (delayed) -5 else 5

        return FreelanceOutcome(
            success = true,
            message = if (delayed) {
                "Gig accepted at KES $agreedPay, but ${client.name} delayed payment."
            } else {
                "Gig accepted! Earned KES $agreedPay from ${client.name}."
            },
            payEarned = payEarned,
            xpGained = xpGained,
            reputationDelta = repDelta,
            delayed = delayed
        )
    }

    // Complete a gig and trigger payment (may be delayed)
    suspend fun completeContract(contractId: String): FreelanceOutcome {
        val contract = contractDao.getContractOnce(contractId) ?: return FreelanceOutcome(
            success = false, message = "Contract not found."
        )

        val updated = contract.copy(status = FreelanceJobStatus.COMPLETED, completedAt = System.currentTimeMillis())
        contractDao.updateContract(updated)

        val delayed = random.nextDouble() >= contract.clientReliability && !contract.instantPayment
        val payEarned = if (delayed) 0L else contract.agreedPay
        val xpGained = (contract.agreedPay / 1000).toInt().coerceAtLeast(10)
        val repDelta = if (delayed) -10 else 10

        if (delayed) {
            contractDao.updateContract(updated.copy(status = FreelanceJobStatus.DELAYED))
            return FreelanceOutcome(
                success = true,
                message = "Work completed, but ${contract.clientName} delayed payment. Check back later.",
                payEarned = 0,
                xpGained = xpGained,
                reputationDelta = repDelta,
                delayed = true
            )
        }

        contractDao.updateContract(updated.copy(status = FreelanceJobStatus.PAID, paidAt = System.currentTimeMillis()))
        return FreelanceOutcome(
            success = true,
            message = "Paid! Received KES ${contract.agreedPay} from ${contract.clientName}.",
            payEarned = contract.agreedPay,
            xpGained = xpGained,
            reputationDelta = repDelta,
            delayed = false
        )
    }

    // Collect delayed payment (may finally pay or never pay)
    suspend fun collectDelayedPayment(contractId: String): FreelanceOutcome {
        val contract = contractDao.getContractOnce(contractId) ?: return FreelanceOutcome(
            success = false, message = "Contract not found."
        )
        if (contract.status != FreelanceJobStatus.DELAYED) {
            return FreelanceOutcome(success = false, message = "No delayed payment to collect.")
        }

        val finallyPaid = contract.clientReliability > 0.5 && random.nextDouble() < contract.clientReliability
        if (finallyPaid) {
            contractDao.updateContract(contract.copy(status = FreelanceJobStatus.PAID, paidAt = System.currentTimeMillis()))
            return FreelanceOutcome(
                success = true,
                message = "${contract.clientName} finally paid KES ${contract.agreedPay}.",
                payEarned = contract.agreedPay,
                xpGained = 5,
                reputationDelta = 5,
                delayed = false
            )
        } else {
            contractDao.updateContract(contract.copy(status = FreelanceJobStatus.FAILED))
            return FreelanceOutcome(
                success = false,
                message = "${contract.clientName} never paid. You lost KES ${contract.agreedPay}.",
                payEarned = 0,
                reputationDelta = -15
            )
        }
    }

    // Get all contracts for the player
    fun getContracts(): Flow<List<FreelanceContract>> = contractDao.getAllContracts()

    // Get active (non-terminal) contracts
    fun getActiveContracts(): Flow<List<FreelanceContract>> = contractDao.getActiveContracts()

    // Get completed but unpaid (delayed) contracts
    fun getDelayedContracts(): Flow<List<FreelanceContract>> = contractDao.getDelayedContracts()

    // Get player freelance profile stats
    suspend fun getProfile(): FreelanceProfile {
        val contracts = contractDao.getAllContractsOnce()
        val completed = contracts.count { it.status == FreelanceJobStatus.PAID }
        val failed = contracts.count { it.status == FreelanceJobStatus.FAILED }
        val earnings = contracts.filter { it.status == FreelanceJobStatus.PAID }.sumOf { it.agreedPay }
        val lastGig = contracts.maxByOrNull { it.startedAt }?.startedAt

        // Aggregate skill XP from completed contracts grouped by skill
        val skillXp = contracts
            .filter { it.status == FreelanceJobStatus.PAID }
            .groupBy { it.skill }
            .mapValues { (_, list) -> list.sumOf { (it.agreedPay / 1000).toInt().coerceAtLeast(10) } }

        return FreelanceProfile(
            totalEarnings = earnings,
            gigsCompleted = completed,
            gigsFailed = failed,
            skillXp = skillXp,
            reputation = (completed * 10) - (failed * 5),
            lastGigAt = lastGig
        )
    }
}
