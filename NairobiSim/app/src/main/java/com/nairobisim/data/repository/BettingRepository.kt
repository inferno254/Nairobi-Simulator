package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.*
import com.nairobisim.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class BettingRepository(
    private val db: AppDatabase,
    private val matchDao: BettingMatchDao,
    private val betSlipDao: BetSlipDao,
    private val balanceDao: BettingBalanceDao
) {

    private val random = Random.Default

    // Get available matches to bet on
    fun getAvailableMatches(): Flow<List<BettingMatch>> = matchDao.getUpcomingMatches()

    // Get player's betting balance
    suspend fun getBalance(): BettingBalance {
        val existing = balanceDao.getBalance()
        return existing ?: BettingBalance(balance = 50000) // starting betting wallet
    }

    // Place a bet
    suspend fun placeBet(
        matchId: String,
        betType: BetType,
        selection: String,
        stake: Long,
        odds: Double
    ): BetSlip {
        val balance = getBalance()
        if (balance.balance < stake) {
            throw IllegalStateException("Insufficient betting balance")
        }

        val match = matchDao.getMatch(matchId) ?: throw IllegalStateException("Match not found")
        val matchName = "${match.homeTeam} vs ${match.awayTeam}"
        val potentialWin = (stake * odds).toLong()

        // Deduct from balance
        val newBalance = balance.copy(
            balance = balance.balance - stake,
            totalWagered = balance.totalWagered + stake,
            betsPlaced = balance.betsPlaced + 1,
            lastUpdated = System.currentTimeMillis()
        )
        balanceDao.insertBalance(newBalance)

        val slip = BetSlip(
            id = "bet_${System.currentTimeMillis()}",
            matchId = matchId,
            matchName = matchName,
            betType = betType,
            selection = selection,
            odds = odds,
            stake = stake,
            status = BetStatus.PENDING,
            potentialWin = potentialWin
        )
        betSlipDao.insertBet(slip)
        return slip
    }

    // Get all bets
    fun getAllBets(): Flow<List<BetSlip>> = betSlipDao.getAllBets()

    // Get pending bets
    fun getPendingBets(): Flow<List<BetSlip>> = betSlipDao.getPendingBets()

    // Settle a bet (random outcome for simulation)
    suspend fun settleBet(slipId: String): BetSlip {
        val slip = betSlipDao.getBet(slipId) ?: throw IllegalStateException("Bet not found")
        if (slip.status != BetStatus.PENDING) return slip

        val match = matchDao.getMatch(slip.matchId) ?: throw IllegalStateException("Match not found")

        // Simulate match result based on odds (lower odds = more likely)
        val won = random.nextDouble() < (1.0 / slip.odds.coerceAtLeast(1.1))

        val newStatus = if (won) BetStatus.WON else BetStatus.LOST
        val actualWin = if (won) slip.potentialWin else 0L

        val updatedSlip = slip.copy(
            status = newStatus,
            actualWin = actualWin,
            settledAt = System.currentTimeMillis()
        )
        betSlipDao.updateBet(updatedSlip)

        // Update balance
        val balance = getBalance()
        val newBalance = balance.copy(
            balance = balance.balance + actualWin,
            totalWon = balance.totalWon + actualWin,
            totalLost = balance.totalLost + (if (won) 0L else slip.stake),
            betsWon = balance.betsWon + (if (won) 1 else 0),
            lastUpdated = System.currentTimeMillis()
        )
        balanceDao.insertBalance(newBalance)

        return updatedSlip
    }

    // Generate random match results for simulation
    suspend fun simulateMatchResults() {
        val matches = matchDao.getUpcomingMatchesOnce()
        for (match in matches) {
            if (!match.isFinished) {
                val homeScore = random.nextInt(0, 5)
                val awayScore = random.nextInt(0, 5)
                val winner = if (homeScore > awayScore) "HOME"
                else if (awayScore > homeScore) "AWAY" else "DRAW"

                val finishedMatch = match.copy(
                    isFinished = true,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    winner = winner
                )
                matchDao.updateMatch(finishedMatch)

                // Settle all bets on this match
                val bets = betSlipDao.getAllBetsOnce()
                for (bet in bets) {
                    if (bet.matchId == match.id && bet.status == BetStatus.PENDING) {
                        settleBet(bet.id)
                    }
                }
            }
        }
    }

    // Add money to betting wallet
    suspend fun addToBettingWallet(amount: Long) {
        val balance = getBalance()
        val newBalance = balance.copy(
            balance = balance.balance + amount,
            lastUpdated = System.currentTimeMillis()
        )
        balanceDao.insertBalance(newBalance)
    }
}
