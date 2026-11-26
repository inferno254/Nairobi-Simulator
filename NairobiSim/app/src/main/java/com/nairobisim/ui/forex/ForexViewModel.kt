package com.nairobisim.ui.forex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nairobisim.data.repository.ForexRepository
import com.nairobisim.model.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ForexViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ForexRepository(
        com.nairobisim.data.database.AppDatabase.getDatabase(application),
        com.nairobisim.data.database.AppDatabase.getDatabase(application).playerProfileDao()
    )

    // UI State
    val _rates = MutableLiveData<Map<String, MarketRate>>()
    val rates: LiveData<Map<String, MarketRate>> = _rates

    val _selectedPair = MutableLiveData<CurrencyPair?>(null)
    val selectedPair: LiveData<CurrencyPair?> = _selectedPair

    val _openTrades = MutableLiveData<List<ForexTrade>>(emptyList())
    val openTrades: LiveData<List<ForexTrade>> = _openTrades

    val _tradeHistory = MutableLiveData<List<ForexTrade>>(emptyList())
    val tradeHistory: LiveData<List<ForexTrade>> = _tradeHistory

    val _balance = MutableLiveData<Double>(0.0)
    val balance: LiveData<Double> = _balance

    val _equity = MutableLiveData<Double>(0.0)
    val equity: LiveData<Double> = _equity

    val _usedMargin = MutableLiveData<Double>(0.0)
    val usedMargin: LiveData<Double> = _usedMargin

    val _freeMargin = MutableLiveData<Double>(0.0)
    val freeMargin: LiveData<Double> = _freeMargin

    val _marginLevel = MutableLiveData<Double>(0.0)
    val marginLevel: LiveData<Double> = _marginLevel

    val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    val _success = MutableLiveData<String?>(null)
    val success: LiveData<String?> = _success

    val availablePairs = DEFAULT_FOREX_PAIRS

    init {
        loadData()
        startPriceSimulation()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bal = repository.getPlayerForexBalance("player_1")
                _balance.value = bal
                _equity.value = bal
                _openTrades.value = repository.getOpenTrades()
                _tradeHistory.value = repository.getTradeHistory()
                _rates.value = repository.getAllRates()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPair(pair: CurrencyPair) {
        _selectedPair.value = pair
    }

    // Simulate price movements (realistic forex volatility)
    private fun startPriceSimulation() {
        for (pair in DEFAULT_FOREX_PAIRS) {
            var basePrice = when (pair.symbol) {
                "KES/USD" -> 130.0
                "KES/GBP" -> 165.0
                "KES/EUR" -> 145.0
                "USD/EUR" -> 0.92
                "USD/GBP" -> 0.79
                "EUR/GBP" -> 0.86
                "USD/KES" -> 0.0077
                "GBP/KES" -> 0.0061
                else -> 1.0
            }

            viewModelScope.launch {
                while (true) {
                    val volatility = when (pair.symbol) {
                        "KES/USD", "KES/GBP", "KES/EUR" -> 0.002
                        else -> 0.001
                    }
                    val change = (Math.random() - 0.5) * 2 * volatility * basePrice
                    basePrice = (basePrice + change).coerceAtLeast(0.001)

                    val spread = pair.typicalSpread
                    val bid = basePrice - spread / 2
                    val ask = basePrice + spread / 2

                    repository.updateRate(pair.symbol, bid, ask)

                    _rates.value = repository.getAllRates()

                    updateUnrealizedPnL()

                    // Simulate different timeframes for different pairs
                    val delay = when (pair.symbol) {
                        "KES/USD", "KES/GBP", "KES/EUR" -> 800L // African pairs move slower
                        else -> 500L
                    }
                    kotlinx.coroutines.delay(delay)
                }
            }
        }
    }

    private fun updateUnrealizedPnL() {
        val openTrades = repository.getOpenTrades()
        var totalUnrealized = 0.0

        for (trade in openTrades) {
            val rate = repository.getCurrentRate(trade.symbol) ?: continue
            val pnl = trade.calculateUnrealizedPnL(rate.bid, rate.ask)
            totalUnrealized += pnl

            // Check stop loss / take profit
            if (trade.isStopLossHit(rate.bid, rate.ask) || trade.isTakeProfitHit(rate.bid, rate.ask)) {
                viewModelScope.launch {
                    repository.closeTrade(trade.id, if (trade.type == TradeType.BUY) rate.bid else rate.ask)
                }
            }
        }

        val bal = _balance.value ?: 0.0
        _equity.value = bal + totalUnrealized

        // Update margin
        var usedMargin = 0.0
        for (trade in openTrades) {
            usedMargin += repository.calculateMarginRequired(trade.symbol, trade.lotSize, trade.openPrice)
        }
        _usedMargin.value = usedMargin
        _freeMargin.value = (bal + totalUnrealized) - usedMargin
        _marginLevel.value = if (usedMargin > 0) ((bal + totalUnrealized) / usedMargin) * 100 else 0.0
    }

    fun openTrade(
        symbol: String,
        type: TradeType,
        lotSize: Double,
        stopLoss: Double = 0.0,
        takeProfit: Double = 0.0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val rate = repository.getCurrentRate(symbol) ?: throw Exception("No rate available")
                val entryPrice = if (type == TradeType.BUY) rate.ask else rate.bid
                val pair = DEFAULT_FOREX_PAIRS.find { it.symbol == symbol } ?: throw Exception("Invalid pair")

                val marginRequired = repository.calculateMarginRequired(symbol, lotSize, entryPrice)
                val bal = _balance.value ?: 0.0
                val freeMargin = _freeMargin.value ?: 0.0

                if (freeMargin < marginRequired) {
                    _error.value = "Insufficient margin. Required: KES ${marginRequired.toInt()}, Available: KES ${freeMargin.toInt()}"
                    return@launch
                }

                if (lotSize < pair.minLotSize || lotSize > pair.maxLotSize) {
                    _error.value = "Lot size must be between ${pair.minLotSize} and ${pair.maxLotSize}"
                    return@launch
                }

                val trade = ForexTrade(
                    id = "fx_${System.currentTimeMillis()}",
                    symbol = symbol,
                    type = type,
                    lotSize = lotSize,
                    openPrice = entryPrice,
                    stopLoss = stopLoss,
                    takeProfit = takeProfit
                )

                repository.openTrade(trade)

                // Deduct margin from balance
                val newBal = bal - marginRequired
                repository.updateForexBalance("player_1", newBal)
                _balance.value = newBal

                _openTrades.value = repository.getOpenTrades()
                _success.value = "Opened ${type.name} ${lotSize} lots ${symbol} at ${entryPrice.toString().substringBeforeLast('.').padEnd(4, '0')}"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun closeTrade(tradeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val trade = repository.getOpenTrades().firstOrNull { it.id == tradeId } ?: throw Exception("Trade not found")
                val rate = repository.getCurrentRate(trade.symbol) ?: throw Exception("No rate")
                val closePrice = if (trade.type == TradeType.BUY) rate.bid else rate.ask

                repository.closeTrade(tradeId, closePrice)

                // Add margin back + PnL to balance
                val marginBack = repository.calculateMarginRequired(trade.symbol, trade.lotSize, trade.openPrice)
                val pnl = trade.calculateUnrealizedPnL(rate.bid, rate.ask)
                val bal = _balance.value ?: 0.0
                val newBal = bal + marginBack + pnl
                repository.updateForexBalance("player_1", newBal)
                _balance.value = newBal

                _openTrades.value = repository.getOpenTrades()
                _tradeHistory.value = repository.getTradeHistory()
                _success.value = "Closed trade. P&L: KES ${pnl.toInt()}"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
    fun clearSuccess() { _success.value = null }
}
