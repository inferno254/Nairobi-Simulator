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

    // Pay a single unpaid record
    suspend fun payRecord(recordId: String): Boolean {
        val record = db.taxRecordDao().getRecord(recordId) ?: return false
        if (record.paid) return false
        val profile = db.playerProfileDao().getProfileOnce("player_1") ?: return false
        val balance = profile.kesBalance ?: 0L
        if (balance < record.amount) return false
        db.playerProfileDao().updateProfile(profile.copy(kesBalance = balance - record.amount))
        db.taxRecordDao().updateRecord(record.copy(paid = true))
        return true
    }

    suspend fun totalUnpaid(): Long =
        db.taxRecordDao().getAllRecordsOnce().filter { !it.paid }.sumOf { it.amount }
}