package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.model.*
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class ElectionRepository(private val db: AppDatabase) {

    private val random = Random.Default

    val elections: Flow<List<Election>> = db.electionDao().getAllElections()
    val activeElections: Flow<List<Election>> = db.electionDao().getActiveElections()

    suspend fun getElection(id: String): Election? = db.electionDao().getElection(id)

    // Start a new election for an office with the default NPC candidates
    suspend fun startElection(office: Office): Election {
        val npcs = DEFAULT_NPC_CANDIDATES
            .filter { it.office == office }
            .map { it.copy(office = office) }
        val election = Election(
            id = "election_${office.name}_${System.currentTimeMillis()}",
            office = office,
            stage = ElectionStage.NOMINATION,
            candidates = npcs,
            daysUntilElection = 30
        )
        db.electionDao().insertElection(election)
        return election
    }

    // Player declares candidacy (pays filing fee)
    suspend fun playerDeclare(electionId: String, playerName: String, party: String, manifesto: String): String {
        val election = db.electionDao().getElection(electionId) ?: return "Election not found"
        if (election.candidates.any { it.isPlayer }) return "You are already a candidate"

        val profile = db.playerProfileDao().getProfileOnce("player_1") ?: return "No profile"
        val fee = election.office.filingFee
        if ((profile.kesBalance ?: 0L) < fee) return "Need KES $fee filing fee"

        db.playerProfileDao().updateProfile(profile.copy(kesBalance = (profile.kesBalance ?: 0L) - fee))

        val playerCandidate = Candidate(
            id = "cand_player",
            name = playerName,
            kind = CandidateKind.PLAYER,
            office = election.office,
            party = party,
            baseSupport = 0.10,
            manifesto = manifesto,
            isPlayer = true
        )
        val updated = election.copy(
            candidates = election.candidates + playerCandidate,
            stage = ElectionStage.CAMPAIGN
        )
        db.electionDao().updateElection(updated)
        return "ok"
    }

    // Player campaign action: spend money to boost support
    suspend fun campaign(electionId: String, action: CampaignAction): String {
        val election = db.electionDao().getElection(electionId) ?: return "Election not found"
        val player = election.candidates.firstOrNull { it.isPlayer } ?: return "You are not a candidate"

        val profile = db.playerProfileDao().getProfileOnce("player_1") ?: return "No profile"
        if ((profile.kesBalance ?: 0L) < action.cost) return "Need KES ${action.cost}"

        db.playerProfileDao().updateProfile(profile.copy(kesBalance = (profile.kesBalance ?: 0L) - action.cost))

        val updatedPlayer = when (action) {
            CampaignAction.RALLY -> player.copy(ralliesHeld = player.ralliesHeld + 1)
            CampaignAction.ADS -> player.copy(adsBought = player.adsBought + 1)
            CampaignAction.ENDORSEMENT -> player.copy(endorsements = player.endorsements + 1)
        }
        val candidates = election.candidates.map { if (it.isPlayer) updatedPlayer else it }
        db.electionDao().updateElection(election.copy(candidates = candidates))
        return "ok"
    }

    // Advance the campaign clock by one day; NPCs also campaign randomly
    suspend fun tickDay(electionId: String) {
        val election = db.electionDao().getElection(electionId) ?: return
        if (election.stage == ElectionStage.CONCLUDED) return

        val candidates = election.candidates.map { c ->
            if (!c.isPlayer && random.nextDouble() < 0.4) {
                val roll = random.nextInt(3)
                when (roll) {
                    0 -> c.copy(ralliesHeld = c.ralliesHeld + 1)
                    1 -> c.copy(adsBought = c.adsBought + 1)
                    else -> c.copy(endorsements = c.endorsements + 1)
                }
            } else c
        }

        val days = election.daysUntilElection - 1
        when {
            days <= 0 -> conclude(election.copy(candidates = candidates, daysUntilElection = 0))
            days <= 7 -> db.electionDao().updateElection(
                election.copy(candidates = candidates, daysUntilElection = days, stage = ElectionStage.ELECTION_DAY)
            )
            else -> db.electionDao().updateElection(
                election.copy(candidates = candidates, daysUntilElection = days, stage = ElectionStage.CAMPAIGN)
            )
        }
    }

    // Determine the single winner by weighted projected votes + randomness
    suspend fun conclude(election: Election) {
        val scored = election.candidates.map { c ->
            val base = election.projectedVotes(c)
            val noise = random.nextDouble(-0.05, 0.05)
            c.id to (base + noise)
        }
        val winnerId = scored.maxByOrNull { it.second }?.first
        db.electionDao().updateElection(
            election.copy(stage = ElectionStage.CONCLUDED, winnerId = winnerId, daysUntilElection = 0)
        )
    }
}

enum class CampaignAction(val displayName: String, val cost: Long, val boost: Double) {
    RALLY("Hold a Rally", 50_000, 0.03),
    ADS("Buy TV/Radio Ads", 100_000, 0.02),
    ENDORSEMENT("Secure Endorsement", 200_000, 0.05)
}