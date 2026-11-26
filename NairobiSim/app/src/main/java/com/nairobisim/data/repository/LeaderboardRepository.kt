package com.nairobisim.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.*
import com.nairobisim.model.NPCharacter
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class LeaderboardRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val leaderboardCollection = firestore.collection("leaderboard")
    private val weeklyLeaderboardCollection = firestore.collection("weekly_leaderboard")

    suspend fun updatePlayerLeaderboardEntry(profile: PlayerProfile, userProfile: UserProfile?): Boolean {
        return try {
            val entry = LeaderboardEntry(
                playerId = profile.id,
                playerName = profile.displayName,
                totalNetWorth = profile.calculateNetWorth(),
                kesBalance = profile.kesBalance ?: 0,
                forexBalance = profile.forexBalance ?: 0.0,
                businessCount = profile.totalBusinessesOwned,
                level = profile.level,
                avatarUrl = userProfile?.avatarUrl ?: "",
                country = userProfile?.country ?: "Kenya",
                badge = calculateBadge(profile),
                lastUpdated = System.currentTimeMillis()
            )

            leaderboardCollection.document(profile.id).set(entry).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateBadge(profile: PlayerProfile): String {
        val netWorth = profile.calculateNetWorth()
        return when {
            netWorth >= 1_000_000_000 -> "🦁 Billionaire"
            netWorth >= 100_000_000 -> "💎 Mogul"
            netWorth >= 10_000_000 -> "🏆 Tycoon"
            netWorth >= 1_000_000 -> "🥇 Millionaire"
            netWorth >= 500_000 -> "🥈 Rising Star"
            netWorth >= 100_000 -> "🥉 Entrepreneur"
            else -> "🌱 Hustler"
        }
    }

    suspend fun getTopLeaderboard(limit: Int = 100): List<LeaderboardEntry> {
        return try {
            val snapshot = leaderboardCollection
                .orderBy("totalNetWorth", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.map { doc ->
                val data = doc.data as Map<String, Any?>
                LeaderboardEntry(
                    playerId = data["playerId"] as String? ?: doc.id,
                    playerName = data["playerName"] as String? ?: "Unknown",
                    totalNetWorth = (data["totalNetWorth"] as Long?) ?: 0L,
                    kesBalance = (data["kesBalance"] as Long?) ?: 0L,
                    forexBalance = (data["forexBalance"] as Double?) ?: 0.0,
                    businessCount = (data["businessCount"] as Long?)?.toInt() ?: 0,
                    level = (data["level"] as Long?)?.toInt() ?: 1,
                    avatarUrl = data["avatarUrl"] as String? ?: "",
                    country = data["country"] as String? ?: "Kenya",
                    badge = data["badge"] as String? ?: "🌱 Hustler",
                    rank = 0
                )
            }.mapIndexed { index, entry: LeaderboardEntry ->
                entry.copy(rank = index + 1)
            }
        } catch (e: Exception) {
            getLocalLeaderboard(limit)
        }
    }

    suspend fun getWeeklyLeaderboard(limit: Int = 50): List<LeaderboardEntry> {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        return try {
            val snapshot = weeklyLeaderboardCollection
                .whereGreaterThanOrEqualTo("lastUpdated", weekAgo)
                .orderBy("totalNetWorth", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.map { doc ->
                val data = doc.data as Map<String, Any?>
                LeaderboardEntry(
                    playerId = data["playerId"] as String? ?: doc.id,
                    playerName = data["playerName"] as String? ?: "Unknown",
                    totalNetWorth = (data["totalNetWorth"] as Long?) ?: 0L,
                    kesBalance = (data["kesBalance"] as Long?) ?: 0L,
                    forexBalance = (data["forexBalance"] as Double?) ?: 0.0,
                    businessCount = (data["businessCount"] as Long?)?.toInt() ?: 0,
                    level = (data["level"] as Long?)?.toInt() ?: 1,
                    avatarUrl = data["avatarUrl"] as String? ?: "",
                    country = data["country"] as String? ?: "Kenya",
                    badge = data["badge"] as String? ?: "🌱 Hustler",
                    rank = 0
                )
            }.mapIndexed { index, entry: LeaderboardEntry ->
                entry.copy(rank = index + 1)
            }
        } catch (e: Exception) {
            getLocalLeaderboard(limit)
        }
    }

    private suspend fun getLocalLeaderboard(limit: Int): List<LeaderboardEntry> {
        val npcs = com.nairobisim.model.npcSimulationEngine.getLeaderboardNPCs()
        val playerProfile = playerProfileDao.getProfileOnce("player_1")

        val allEntries = mutableListOf<LeaderboardEntry>()

        // Add player
        if (playerProfile != null) {
            allEntries.add(LeaderboardEntry(
                playerId = playerProfile.id,
                playerName = playerProfile.displayName,
                totalNetWorth = playerProfile.calculateNetWorth(),
                kesBalance = playerProfile.kesBalance ?: 0,
                forexBalance = playerProfile.forexBalance ?: 0.0,
                businessCount = playerProfile.totalBusinessesOwned,
                level = playerProfile.level,
                avatarUrl = "",
                country = "Kenya",
                badge = calculateBadge(playerProfile)
            ))
        }

        // Add NPCs
        npcs.forEach { npc: NPCharacter ->
            allEntries.add(LeaderboardEntry(
                playerId = npc.id,
                playerName = npc.name,
                totalNetWorth = npc.totalNetWorth,
                kesBalance = npc.kesBalance,
                forexBalance = 0.0,
                businessCount = npc.businessesOwned.size,
                level = npc.level,
                avatarUrl = "",
                country = "Kenya",
                badge = calculateBadgeFromNetWorth(npc.totalNetWorth)
            ))
        }

        return allEntries
            .sortedByDescending { it.totalNetWorth }
            .take(limit)
            .withIndex()
            .map { (index, entry) -> entry.copy(rank = index + 1) }
    }

    private fun calculateBadgeFromNetWorth(netWorth: Long): String = when {
        netWorth >= 1_000_000_000 -> "🦁 Billionaire"
        netWorth >= 100_000_000 -> "💎 Mogul"
        netWorth >= 10_000_000 -> "🏆 Tycoon"
        netWorth >= 1_000_000 -> "🥇 Millionaire"
        netWorth >= 500_000 -> "🥈 Rising Star"
        netWorth >= 100_000 -> "🥉 Entrepreneur"
        else -> "🌱 Hustler"
    }

    suspend fun getPlayerRank(playerId: String): Int {
        val leaderboard = getTopLeaderboard(1000)
        return leaderboard.indexOfFirst { it.playerId == playerId } + 1
    }

    // Forbes-style wealth breakdown
    data class WealthBreakdown(
        val rank: Int,
        val totalNetWorth: Long,
        val cash: Long,
        val businesses: Long,
        val realEstate: Long,
        val vehicles: Long,
        val stocks: Long,
        val forex: Long,
        val other: Long
    )

    suspend fun getWealthBreakdown(playerId: String): WealthBreakdown? {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return null
        val rank = getPlayerRank(playerId)

        val businessValue = 0L // Would calculate from businesses
        val carValue = profile.ownedCars.sumOf { it.currentValue }
        val shareValue = profile.shareHoldings.sumOf { it.currentValue.toLong() }
        val forexValue = (profile.forexBalance ?: 0.0).toLong()

        return WealthBreakdown(
            rank = rank,
            totalNetWorth = profile.calculateNetWorth(),
            cash = (profile.kesBalance ?: 0) + (profile.bankBalance ?: 0) + (profile.mpesaBalance ?: 0) + (profile.saccosBalance ?: 0),
            businesses = businessValue,
            realEstate = 0L,
            vehicles = carValue,
            stocks = shareValue,
            forex = forexValue,
            other = profile.cryptoWallet ?: 0
        )
    }
}