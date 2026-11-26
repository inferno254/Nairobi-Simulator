package com.nairobisim.data.repository

import com.nairobisim.data.dao.*
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*

class ForexRepository(
    private val db: AppDatabase,
    private val playerProfileDao: PlayerProfileDao
) {

    private val openTrades = mutableListOf<ForexTrade>()
    private val tradeHistory = mutableListOf<ForexTrade>()
    private var currentRates = mutableMapOf<String, MarketRate>()

    init {
        loadDefaultPairs()
    }

    private fun loadDefaultPairs() {
        // Initialize with default pairs - rates set by market simulator
    }

    suspend fun getPlayerForexBalance(playerId: String): Double {
        val profile = playerProfileDao.getProfileOnce(playerId)
        return profile?.forexBalance ?: 0.0
    }

    suspend fun updateForexBalance(playerId: String, newBalance: Double) {
        val profile = playerProfileDao.getProfileOnce(playerId) ?: return
        profile.forexBalance = newBalance
        playerProfileDao.updateProfile(profile)
    }

    fun getCurrentRate(symbol: String): MarketRate? = currentRates[symbol]

    fun updateRate(symbol: String, bid: Double, ask: Double) {
        currentRates[symbol] = MarketRate(symbol, bid, ask, ask - bid)
    }

    fun getAllRates(): Map<String, MarketRate> = currentRates.toMap()

    suspend fun openTrade(trade: ForexTrade) {
        db.forexTradeDao().insertTrade(trade)
        openTrades.add(trade)
    }

    suspend fun closeTrade(tradeId: String, closePrice: Double) {
        val trade = openTrades.firstOrNull { it.id == tradeId } ?: return
        val updated = trade.copy(
            isClosed = true,
            closePrice = closePrice,
            closeTime = System.currentTimeMillis(),
            pnl = trade.calculateUnrealizedPnL(closePrice, closePrice)
        )
        db.forexTradeDao().updateTrade(updated)
        openTrades.remove(trade)
        tradeHistory.add(updated)

        // Update player balance
        val profile = playerProfileDao.getProfileOnce("player_1") ?: return
        profile.forexBalance = (profile.forexBalance ?: 0.0) + updated.pnl
        profile.totalNetWorth = profile.calculateNetWorth()
        playerProfileDao.updateProfile(profile)
    }

    fun getOpenTrades(): List<ForexTrade> = openTrades.toList()
    fun getTradeHistory(): List<ForexTrade> = tradeHistory.toList()

    suspend fun getTradeHistoryFromDb(): List<ForexTrade> =
        db.forexTradeDao().getRecentTrades(100).first()

    // Margin calculation
    fun calculateMarginRequired(symbol: String, lotSize: Double, price: Double): Double {
        val pair = DEFAULT_FOREX_PAIRS.find { it.symbol == symbol } ?: return 0.0
        return (price * lotSize * 100000) * pair.marginRate
    }

    fun calculatePnL(symbol: String, type: TradeType, lotSize: Double, openPrice: Double, currentBid: Double, currentAsk: Double): Double {
        val exitPrice = if (type == TradeType.BUY) currentBid else currentAsk
        val priceDiff = exitPrice - openPrice
        return priceDiff * lotSize * 100000
    }
}
