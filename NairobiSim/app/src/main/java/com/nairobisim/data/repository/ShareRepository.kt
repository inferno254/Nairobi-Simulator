package com.nairobisim.data.repository

import com.nairobisim.data.dao.ShareTradeDao
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.model.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class ShareRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao,
    private val shareTradeDao: ShareTradeDao
) {

    private val sharePrices = ConcurrentHashMap<String, Double>()
    private val openPositions = mutableListOf<ShareTrade>()
    private val tradeHistory = mutableListOf<ShareTrade>()

    init {
        initializePrices()
    }

    private fun initializePrices() {
        for (company in NAIROBI_SHARE_MARKET) {
            sharePrices[company.symbol] = company.currentPrice
        }
    }

    suspend fun getPlayerShareBalance(playerId: String): Double {
        val profile = playerProfileDao.getProfileOnce(playerId)
        return profile?.forexBalance ?: 0.0 // Using forexBalance field for share cash too
    }

    // Actually we need a separate field. Let's use a different approach:
    // Store share cash in a separate way - use forexBalance for shares too temporarily
    // Better: add a dedicated field. For now, let's track share portfolio value separately.

    fun getCurrentPrice(symbol: String): Double = sharePrices[symbol] ?: 0.0

    fun getAllPrices(): Map<String, Double> = sharePrices.toMap()

    fun getCompany(symbol: String): ShareCompany? = NAIROBI_SHARE_MARKET.find { it.symbol == symbol }

    // Simulate price movements
    fun simulatePriceChange(symbol: String) {
        val company = NAIROBI_SHARE_MARKET.find { it.symbol == symbol } ?: return
        val current = sharePrices[symbol] ?: company.currentPrice
        val volatility = when (company.sector) {
            ShareSector.TELECOM -> 0.02
            ShareSector.ENERGY -> 0.03
            ShareSector.BANKING -> 0.025
            ShareSector.BEVERAGE -> 0.015
            ShareSector.REAL_ESTATE -> 0.02
            ShareSector.MEDIA -> 0.035
            ShareSector.TRANSPORT -> 0.03
            ShareSector.AGRICULTURE -> 0.04
        }
        val change = (Math.random() - 0.5) * 2 * volatility * current
        val newPrice = (current + change).coerceAtLeast(0.01)
        sharePrices[symbol] = newPrice
    }

    suspend fun buyShares(symbol: String, shares: Int, pricePerShare: Double) {
        val totalCost = shares * pricePerShare
        val profile = playerProfileDao.getProfileOnce("player_1") ?: return

        val currentBalance = profile.forexBalance ?: 0.0
        if (currentBalance < totalCost) {
            throw Exception("Insufficient funds. Need KES $totalCost, have KES ${currentBalance.toInt()}")
        }

        val trade = ShareTrade(
            id = "sh_${System.currentTimeMillis()}",
            symbol = symbol,
            type = TradeType.BUY,
            shares = shares,
            price = pricePerShare
        )

        shareTradeDao.insertTrade(trade)
        openPositions.add(trade)

        // Deduct from balance
        profile.forexBalance = currentBalance - totalCost
        profile.totalNetWorth = profile.calculateNetWorth()
        playerProfileDao.updateProfile(profile)
    }

    suspend fun sellShares(symbol: String, sharesToSell: Int) {
        // Find holdings for this symbol
        val profile = playerProfileDao.getProfileOnce("player_1") ?: return
        val holdings = profile.shareHoldings
        val holding = holdings.find { it.symbol == symbol } ?: throw Exception("No shares of $symbol owned")

        if (holding.shares < sharesToSell) {
            throw Exception("Only ${holding.shares} shares of $symbol owned")
        }

        val currentPrice = getCurrentPrice(symbol)
        val totalProceeds = sharesToSell * currentPrice

        val trade = ShareTrade(
            id = "sh_${System.currentTimeMillis()}",
            symbol = symbol,
            type = TradeType.SELL,
            shares = sharesToSell,
            price = currentPrice
        )

        shareTradeDao.insertTrade(trade)
        openPositions.add(trade)

        // Add to balance
        profile.forexBalance = (profile.forexBalance ?: 0.0) + totalProceeds
        playerProfileDao.updateProfile(profile)
    }

    fun getOpenPositions(): List<ShareTrade> = openPositions.toList()
    fun getTradeHistory(): List<ShareTrade> = tradeHistory.toList()
}
