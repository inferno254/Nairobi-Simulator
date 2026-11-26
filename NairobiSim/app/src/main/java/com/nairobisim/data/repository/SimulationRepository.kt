package com.nairobisim.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.*
import com.nairobisim.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*

class SimulationRepository(private val db: AppDatabase) {

    // Player Profile
    val playerProfile: LiveData<PlayerProfile?> = db.playerProfileDao().getProfile("player_1").asLiveData()

    suspend fun getPlayerProfile(): PlayerProfile? = db.playerProfileDao().getProfileOnce("player_1")

    suspend fun updatePlayerProfile(profile: PlayerProfile) = db.playerProfileDao().updateProfile(profile)

    suspend fun initializePlayer(profile: PlayerProfile) = db.playerProfileDao().insertProfile(profile)

    // Businesses
    val activeBusinesses: LiveData<List<Business>> = db.businessDao().getActiveBusinesses().asLiveData()
    val allBusinesses: LiveData<List<Business>> = db.businessDao().getAllBusinesses().asLiveData()

    suspend fun getBusiness(id: String): Business? = db.businessDao().getBusiness(id)

    suspend fun startBusiness(business: Business) {
        db.businessDao().insertBusiness(business)
        val profile = getPlayerProfile()?.copy(kesBalance = (getPlayerProfile()?.kesBalance ?: 0) - business.capitalInvested)
        profile?.let { updatePlayerProfile(it) }
        recordTransaction(DailyTransaction(
            type = TransactionType.INVESTMENT,
            category = business.category.displayName,
            amount = business.capitalInvested,
            description = "Started ${business.name} in ${business.location.displayName}",
            balanceAfter = profile?.kesBalance ?: 0,
            businessId = business.id
        ))
    }

    suspend fun collectBusinessRevenue(business: Business) {
        val revenue = business.calculateRevenue()
        val expenses = business.monthlyExpenses
        val net = revenue - expenses

        val updatedBusiness = business.copy(
            lastCollection = Date(),
            monthlyRevenue = revenue,
            monthlyExpenses = expenses
        )
        db.businessDao().updateBusiness(updatedBusiness)

        val profile = getPlayerProfile()?.copy(kesBalance = (getPlayerProfile()?.kesBalance ?: 0) + net)
        profile?.let { updatePlayerProfile(it) }

        recordTransaction(DailyTransaction(
            type = TransactionType.INCOME,
            category = business.category.displayName,
            amount = net,
            description = "Collected from ${business.name}: KES $revenue revenue - KES $expenses expenses",
            balanceAfter = profile?.kesBalance ?: 0,
            businessId = business.id
        ))
    }

    suspend fun upgradeBusiness(businessId: String, upgradeCost: Long) {
        val business = getBusiness(businessId)?.copy(level = (getBusiness(businessId)?.level ?: 1) + 1)
        business?.let {
            it.capitalInvested += upgradeCost
            db.businessDao().updateBusiness(it)
        }

        val profile = getPlayerProfile()?.copy(kesBalance = (getPlayerProfile()?.kesBalance ?: 0) - upgradeCost)
        profile?.let { updatePlayerProfile(it) }

        recordTransaction(DailyTransaction(
            type = TransactionType.EXPENSE,
            category = "Business Upgrade",
            amount = upgradeCost,
            description = "Upgraded business to level ${business?.level}",
            balanceAfter = profile?.kesBalance ?: 0,
            businessId = businessId
        ))
    }

    // Transactions
    val recentTransactions: LiveData<List<DailyTransaction>> = db.transactionDao().getRecentTransactions(50).asLiveData()

    suspend fun recordTransaction(transaction: DailyTransaction) {
        db.transactionDao().insertTransaction(transaction)
    }

    suspend fun recordTransactions(transactions: List<DailyTransaction>) {
        db.transactionDao().insertTransactions(transactions)
    }

    suspend fun getMonthlySummary(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val start = cal.time
        cal.add(Calendar.MONTH, 1)
        val end = cal.time

        val income = db.transactionDao().getTotalIncome(start, end) ?: 0
        val expenses = db.transactionDao().getTotalExpenses(start, end) ?: 0
        return Pair(income, expenses)
    }

    // Loans
    val activeLoans: LiveData<List<Loan>> = db.loanDao().getActiveLoans().asLiveData()

    suspend fun takeLoan(loan: Loan) {
        db.loanDao().insertLoan(loan)

        val profile = getPlayerProfile()?.copy(kesBalance = (getPlayerProfile()?.kesBalance ?: 0) + loan.principal)
        profile?.let { updatePlayerProfile(it) }

        recordTransaction(DailyTransaction(
            type = TransactionType.LOAN,
            category = loan.source.name,
            amount = loan.principal,
            description = "Loan from ${loan.source.name}: KES ${loan.principal} at ${loan.interestRate}%/month",
            balanceAfter = profile?.kesBalance ?: 0
        ))
    }

    suspend fun repayLoan(loanId: String, amount: Long) {
        val loan = db.loanDao().getLoan(loanId)
        loan?.let {
            val newBalance = it.outstandingBalance - amount
            val updatedLoan = it.copy(
                outstandingBalance = newBalance,
                isActive = newBalance > 0,
                missedPayments = if (amount >= it.monthlyPayment) it.missedPayments else it.missedPayments + 1
            )
            db.loanDao().updateLoan(updatedLoan)

            val profile = getPlayerProfile()?.copy(kesBalance = (getPlayerProfile()?.kesBalance ?: 0) - amount)
            profile?.let { updatePlayerProfile(it) }

            recordTransaction(DailyTransaction(
                type = TransactionType.EXPENSE,
                category = "Loan Repayment",
                amount = amount,
                description = "Repaid ${loan.source.name}: KES $amount (Balance: KES $newBalance)",
                balanceAfter = profile?.kesBalance ?: 0
            ))
        }
    }

    // Chama
    val activeChamas: LiveData<List<ChamaGroup>> = db.chamaDao().getActiveChamas().asLiveData()

    suspend fun createChama(chama: ChamaGroup) = db.chamaDao().insertChama(chama)

    suspend fun contributeToChama(chamaId: String, memberId: String) {
        val chama = db.chamaDao().getChama(chamaId)
        chama?.let {
            val updatedMembers = it.members.map { member ->
                if (member.userId == memberId) member.copy(hasPaid = true) else member
            }
            val newPot = it.currentPot + it.contributionAmount
            val updatedChama = it.copy(members = updatedMembers, currentPot = newPot)
            db.chamaDao().updateChama(updatedChama)
        }
    }

    // Market Events
    val currentEvents: LiveData<List<MarketEvent>> = db.marketEventDao().getActiveEvents(Date()).asLiveData()

    suspend fun initializeMarketEvents() {
        val events = generateMarketEvents()
        db.marketEventDao().insertEvents(events)
    }

    private fun generateMarketEvents(): List<MarketEvent> {
        val cal = Calendar.getInstance()
        val events = mutableListOf<MarketEvent>()

        // Public Holidays
        val holidays = listOf(
            Triple("New Year", 1, 1),
            Triple("Good Friday", 3, 29),
            Triple("Easter Monday", 4, 1),
            Triple("Labour Day", 5, 1),
            Triple("Madaraka Day", 6, 1),
            Triple("Mashujaa Day", 10, 20),
            Triple("Jamhuri Day", 12, 12),
            Triple("Christmas", 12, 25),
            Triple("Boxing Day", 12, 26)
        )

        holidays.forEach { (name, month, day) ->
            cal.set(2025, month - 1, day)
            events.add(MarketEvent(
                id = "holiday_$name".lowercase().replace(" ", "_"),
                title = name,
                description = "Public holiday - increased consumer spending",
                type = EventType.HOLIDAY,
                startDate = cal.time,
                endDate = cal.time,
                affectedCategories = BusinessCategory.values().toList(),
                affectedAreas = NairobiArea.values().toList(),
                revenueMultiplier = 1.3
            ))
        }

        // Seasonal events
        data class SeasonalEvent(
            val name: String,
            val startMonth: Int,
            val startDay: Int,
            val endMonth: Int,
            val endDay: Int,
            val categories: List<BusinessCategory>,
            val multiplier: Double
        )
        val seasonal = listOf(
            SeasonalEvent("Back to School", 1, 1, 1, 31, listOf(BusinessCategory.KIOSK, BusinessCategory.CYBER_CAFE, BusinessCategory.SECOND_HAND_CLOTHES), 1.5),
            SeasonalEvent("Planting Season", 3, 1, 5, 31, listOf(BusinessCategory.AGROVET, BusinessCategory.POSHO_MILL, BusinessCategory.DAIRY_FARM, BusinessCategory.POULTRY_FARM), 1.4),
            SeasonalEvent("Harvest Season", 9, 1, 11, 30, listOf(BusinessCategory.GREEN_GROCERY, BusinessCategory.POSHO_MILL, BusinessCategory.HORTICULTURE), 1.3),
            SeasonalEvent("Christmas Shopping", 11, 15, 12, 24, listOf(BusinessCategory.KIOSK, BusinessCategory.SECOND_HAND_CLOTHES, BusinessCategory.RESTAURANT, BusinessCategory.SALON_BARBERSHOP), 1.6),
            SeasonalEvent("Long Rains", 3, 1, 5, 31, listOf(BusinessCategory.CAR_WASH, BusinessCategory.BODABODA_FLEET), 0.7),
            SeasonalEvent("Short Rains", 10, 1, 12, 31, listOf(BusinessCategory.CAR_WASH, BusinessCategory.BODABODA_FLEET), 0.8)
        )

        seasonal.forEach { event ->
            cal.set(2025, event.startMonth - 1, event.startDay)
            val start = cal.time
            cal.set(2025, event.endMonth - 1, event.endDay)
            val end = cal.time

            events.add(MarketEvent(
                id = "seasonal_${event.name}".lowercase().replace(" ", "_"),
                title = event.name,
                description = "Seasonal business impact: ${event.categories.joinToString(", ") { it.displayName }}",
                type = EventType.SEASONAL,
                startDate = start,
                endDate = end,
                affectedCategories = event.categories,
                affectedAreas = NairobiArea.values().toList(),
                revenueMultiplier = event.multiplier
            ))
        }

        // Random events (simulated)
        (1..12).forEach { month ->
            if (Math.random() < 0.3) {
                cal.set(2025, month - 1, (1..28).random())
                val start = cal.time
                cal.add(Calendar.DAY_OF_MONTH, (1..7).random())
                val end = cal.time

                val eventTypes = listOf(
                    Triple(EventType.WEATHER, "Heavy rains affecting transport", 0.8),
                    Triple(EventType.INFRASTRUCTURE, "Road construction causing traffic", 0.9),
                    Triple(EventType.HEALTH, "Cholera outbreak in informal settlements", 0.7),
                    Triple(EventType.POLITICAL, "Political rallies disrupting business", 0.85),
                    Triple(EventType.SPORTS, "Major football match - viewing business boom", 1.4)
                )

                val (type, desc, multiplier) = eventTypes.random()
                events.add(MarketEvent(
                    id = "random_${type.name}_$month",
                    title = type.name,
                    description = desc,
                    type = type,
                    startDate = start,
                    endDate = end,
                    affectedCategories = BusinessCategory.values().toList(),
                    affectedAreas = NairobiArea.values().toList(),
                    revenueMultiplier = multiplier
                ))
            }
        }

        return events
    }

    // Simulation State
    val simulationState: LiveData<SimulationState?> = db.simulationStateDao().getState().asLiveData()

    suspend fun getSimulationState(): SimulationState? = db.simulationStateDao().getStateOnce()

    suspend fun updateSimulationState(state: SimulationState) = db.simulationStateDao().updateState(state)

    suspend fun initializeSimulationState() {
        val state = db.simulationStateDao().getStateOnce()
        if (state == null) {
            db.simulationStateDao().insertState(SimulationState())
        }
    }

    // Utility
    suspend fun advanceDay() {
        val state = getSimulationState()?.copy(
            currentDate = Date(System.currentTimeMillis() + 86400000),
            totalDaysSimulated = (getSimulationState()?.totalDaysSimulated ?: 0) + 1
        )
        state?.let { updateSimulationState(it) }

        // Process daily collections
        val businesses = db.businessDao().getActiveBusinesses().first()
        businesses.forEach { business ->
            val dailyRevenue = business.calculateRevenue() / 30
            val dailyExpenses = business.monthlyExpenses / 30

            val profile = getPlayerProfile()?.copy(kesBalance = (getPlayerProfile()?.kesBalance ?: 0) + dailyRevenue - dailyExpenses)
            profile?.let { updatePlayerProfile(it) }
        }

        // Process loan payments
        val loans = db.loanDao().getActiveLoans().first()
        loans.forEach { loan ->
            if (loan.nextPaymentDate.before(Date())) {
                val profile = getPlayerProfile()
                if ((profile?.kesBalance ?: 0) >= loan.monthlyPayment) {
                    repayLoan(loan.id, loan.monthlyPayment)
                } else {
                    db.loanDao().updateLoan(loan.copy(missedPayments = loan.missedPayments + 1))
                }
            }
        }
    }
}
