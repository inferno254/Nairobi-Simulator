package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.model.*
import java.util.*

class TaxRepository(private val db: AppDatabase) {

    val records = db.taxRecordDao().getAllRecords()

    // Compute progressive PAYE on a monthly taxable income
    fun computePAYE(monthlyIncome: Long): Long {
        var remaining = monthlyIncome
        var lower = 0L
        var tax = 0.0
        for (bracket in PAYE_BRACKETS) {
            val bandWidth = bracket.upperBound - lower
            val taxableInBand = minOf(remaining, bandWidth).coerceAtLeast(0)
            tax += taxableInBand * bracket.rate
            remaining -= taxableInBand
            lower = bracket.upperBound
            if (remaining <= 0) break
        }
        // Personal relief (KES 2,400/month)
        val relief = 2400L
        return (tax - relief.toDouble()).coerceAtLeast(0.0).toLong()
    }

    // Build a full monthly tax bill for the player
    suspend fun buildMonthlyBill(period: String): TaxBill {
        val profile = db.playerProfileDao().getProfileOnce("player_1")
        val businesses = db.businessDao().getActiveBusinessesOnce()

        val grossProfit = businesses.sumOf { it.netMonthlyIncome }.coerceAtLeast(0)
        val lines = mutableListOf<TaxRecord>()

        // PAYE on profit
        val paye = computePAYE(grossProfit)
        if (paye > 0) lines.add(TaxRecord("tax_${period}_paye", TaxType.PAYE, period, grossProfit, paye))

        // NHIF/SHIF: 2.75% of income, min 300, max 1700
        val shif = (grossProfit * 0.0275).toLong().coerceIn(300, 1700)
        lines.add(TaxRecord("tax_${period}_shif", TaxType.NHIF, period, grossProfit, shif))

        // NSSF: 6% up to 4320 base
        val nssf = (grossProfit * 0.06).toLong().coerceAtMost(4320)
        lines.add(TaxRecord("tax_${period}_nssf", TaxType.NSSF, period, grossProfit, nssf))

        // Business tax: 1% of turnover for small biz (turnover < 1M/yr => monthly gross)
        val businessTax = (grossProfit * 0.01).toLong()
        if (businessTax > 0) lines.add(TaxRecord("tax_${period}_biz", TaxType.BUSINESS_TAX, period, grossProfit, businessTax))

        return TaxBill(period, lines, lines.sumOf { it.amount })
    }

    // Apply (deduct) a bill from the player's KES balance and mark records paid
    suspend fun applyBill(bill: TaxBill): Boolean {
        val profile = db.playerProfileDao().getProfileOnce("player_1") ?: return false
        val balance = profile.kesBalance ?: 0L
        if (balance < bill.total) return false

        val updated = profile.copy(kesBalance = balance - bill.total)
        db.playerProfileDao().updateProfile(updated)

        for (line in bill.lines) {
            db.taxRecordDao().insertRecord(line.copy(paid = true))
        }
        return true
    }

    // Build a quarterly KRA bill (VAT + business tax) with a filing deadline.
    suspend fun buildQuarterlyBill(quarter: String, dueAt: Long): TaxBill {
        val businesses = db.businessDao().getActiveBusinessesOnce()
        val quarterRevenue = businesses.sumOf { it.calculateRevenue() } // 3 months of monthly revenue
        val lines = mutableListOf<TaxRecord>()

        // VAT 16% on taxable sales
        val vat = (quarterRevenue * 0.16).toLong()
        lines.add(TaxRecord("tax_${quarter}_vat", TaxType.VAT, quarter, quarterRevenue, vat, dueAt = dueAt))

        // Business tax 1% of turnover
        val biz = (quarterRevenue * 0.01).toLong()
        lines.add(TaxRecord("tax_${quarter}_bizq", TaxType.BUSINESS_TAX, quarter, quarterRevenue, biz, dueAt = dueAt))

        return TaxBill(quarter, lines, lines.sumOf { it.amount })
    }

    // Add late-filing penalties to unpaid quarterly records past their deadline.
    // 5% base + 1% per 30 days late (KRA-style escalating penalty).
    suspend fun applyLatePenalties() {
        val now = System.currentTimeMillis()
        val unpaid = db.taxRecordDao().getAllRecordsOnce().filter { !it.paid && it.dueAt != null }
        for (r in unpaid) {
            val due = r.dueAt ?: continue
            if (now > due) {
                val daysLate = ((now - due) / 86_400_000L).toInt()
                val monthsLate = (daysLate / 30).coerceAtMost(6)
                val penalty = (r.amount * (0.05 + 0.01 * monthsLate)).toLong()
                if (penalty > r.penalty) {
                    db.taxRecordDao().updateRecord(r.copy(penalty = penalty))
                }
            }
        }
    }

    // Accountant auto-pays all due bills if the balance covers them.
    suspend fun accountantSettle(): Long {
        val profile = db.playerProfileDao().getProfileOnce("player_1") ?: return 0
        if (!profile.hasAccountant) return 0
        var balance = profile.kesBalance ?: 0L
        var paidTotal = 0L
        val unpaid = db.taxRecordDao().getAllRecordsOnce().filter { !it.paid }
        for (r in unpaid) {
            if (balance >= r.totalDue) {
                balance -= r.totalDue
                paidTotal += r.totalDue
                db.taxRecordDao().updateRecord(r.copy(paid = true))
            }
        }
        if (paidTotal > 0) {
            db.playerProfileDao().updateProfile(profile.copy(kesBalance = balance))
        }
        return paidTotal
    }

    suspend fun hireAccountant(cost: Long): Boolean {
        val profile = db.playerProfileDao().getProfileOnce("player_1") ?: return false
        if (profile.hasAccountant) return false
        if ((profile.kesBalance ?: 0L) < cost) return false
        db.playerProfileDao().updateProfile(
            profile.copy(kesBalance = (profile.kesBalance ?: 0L) - cost, hasAccountant = true)
        )
        return true
    }

    // Pay a single unpaid record
    suspend fun payRecord(recordId: String): Boolean {
        val record = db.taxRecordDao().getRecord(recordId) ?: return false
        if (record.paid) return false
        val profile = db.playerProfileDao().getProfileOnce("player_1") ?: return false
        val balance = profile.kesBalance ?: 0L
        if (balance < record.totalDue) return false
        db.playerProfileDao().updateProfile(profile.copy(kesBalance = balance - record.totalDue))
        db.taxRecordDao().updateRecord(record.copy(paid = true))
        return true
    }

    suspend fun totalUnpaid(): Long =
        db.taxRecordDao().getAllRecordsOnce().filter { !it.paid }.sumOf { it.totalDue }
}