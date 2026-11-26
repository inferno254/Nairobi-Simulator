package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.*
import com.nairobisim.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class PonziRepository(
    private val db: AppDatabase,
    private val schemeDao: PonziSchemeDao,
    private val investmentDao: PonziInvestmentDao
) {

    private val random = Random.Default

    // Get all available ponzi schemes
    fun getAllSchemes(): Flow<List<PonziScheme>> = schemeDao.getAllSchemes()

    // Get scheme by ID
    suspend fun getScheme(id: String): PonziScheme? = schemeDao.getScheme(id)

    // Invest in a ponzi scheme
    suspend fun invest(schemeId: String, amount: Long): PonziInvestment {
        val scheme = schemeDao.getScheme(schemeId) ?: throw IllegalStateException("Scheme not found")
        if (amount < scheme.minimumInvestment) {
            throw IllegalStateException("Minimum investment is KES ${scheme.minimumInvestment}")
        }

        val expectedReturn = (amount * (1 + scheme.promisedReturn / 100.0)).toLong()
        val expectedDate = System.currentTimeMillis() + (scheme.promisedPeriod * 86400000L)

        // Update scheme total collected
        val updatedScheme = scheme.copy(
            totalCollected = scheme.totalCollected + amount,
            daysActive = scheme.daysActive + 1
        )
        schemeDao.updateScheme(updatedScheme)

        // Check if scheme collapses after this investment
        val collapsed = random.nextDouble() < scheme.collapseRisk
        val finalStatus = if (collapsed) PonziSchemeStatus.COLLAPSED else scheme.status

        if (collapsed) {
            schemeDao.updateScheme(updatedScheme.copy(
                status = PonziSchemeStatus.COLLAPSED,
                collapsedAt = System.currentTimeMillis()
            ))
        }

        val investment = PonziInvestment(
            id = "ponzi_inv_${System.currentTimeMillis()}",
            schemeId = schemeId,
            schemeName = scheme.name,
            amount = amount,
            expectedReturn = expectedReturn,
            expectedDate = expectedDate,
            status = if (collapsed) "LOST" else "ACTIVE"
        )
        investmentDao.insertInvestment(investment)
        return investment
    }

    // Get all investments
    fun getAllInvestments(): Flow<List<PonziInvestment>> = investmentDao.getAllInvestments()

    // Get investments by scheme
    fun getInvestmentsByScheme(schemeId: String): Flow<List<PonziInvestment>> =
        investmentDao.getInvestmentsByScheme(schemeId)

    // Check if any schemes should collapse today
    suspend fun checkSchemeCollapses() {
        val schemes = schemeDao.getAllSchemesOnce()
        val now = System.currentTimeMillis()

        for (scheme in schemes) {
            if (scheme.status == PonziSchemeStatus.ACTIVE) {
                val daysSinceStart = ((now - scheme.createdAt) / 86400000L).toInt()
                // Higher collapse chance the longer it's been active
                val adjustedRisk = scheme.collapseRisk * (1.0 + daysSinceStart * 0.05)

                if (random.nextDouble() < adjustedRisk) {
                    schemeDao.updateScheme(scheme.copy(
                        status = PonziSchemeStatus.COLLAPSED,
                        collapsedAt = now
                    ))

                    // Mark all investments in this scheme as LOST
                    val investments = investmentDao.getInvestmentsBySchemeOnce(scheme.id)
                    for (inv in investments) {
                        if (inv.status == "ACTIVE") {
                            investmentDao.updateInvestment(inv.copy(status = "LOST"))
                        }
                    }
                }
            }
        }
    }

    // Try to collect returns from a scheme
    suspend fun collectReturns(investmentId: String): String {
        val investment = investmentDao.getInvestment(investmentId) ?: throw IllegalStateException("Investment not found")
        val scheme = schemeDao.getScheme(investment.schemeId) ?: throw IllegalStateException("Scheme not found")

        if (investment.status != "ACTIVE") {
            return "Investment already ${investment.status.lowercase()}"
        }

        val now = System.currentTimeMillis()
        val daysPassed = ((now - investment.investedAt) / 86400000L).toInt()

        if (scheme.status == PonziSchemeStatus.COLLAPSED) {
            investmentDao.updateInvestment(investment.copy(status = "LOST"))
            return "Scheme collapsed! You lost KES ${investment.amount}. It was a ponzi scheme."
        }

        if (daysPassed < scheme.promisedPeriod) {
            return "Returns not ready yet. Come back in ${scheme.promisedPeriod - daysPassed} days."
        }

        // Scheme might collapse right when you try to collect
        if (random.nextDouble() < scheme.collapseRisk * 2) {
            schemeDao.updateScheme(scheme.copy(status = PonziSchemeStatus.COLLAPSED, collapsedAt = now))
            investmentDao.updateInvestment(investment.copy(status = "LOST"))
            return "Scheme collapsed while you tried to collect! You lost KES ${investment.amount}."
        }

        // Pay out returns
        schemeDao.updateScheme(scheme.copy(
            totalPaidOut = scheme.totalPaidOut + investment.expectedReturn
        ))
        investmentDao.updateInvestment(investment.copy(
            status = "PAID_OUT",
            paidOutAt = now
        ))

        return "Received KES ${investment.expectedReturn} from ${scheme.name}!"
    }
}
