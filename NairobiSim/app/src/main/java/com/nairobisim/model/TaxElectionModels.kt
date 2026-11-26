package com.nairobisim.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nairobisim.data.converters.Converters
import java.util.*

// ==================== TAX SYSTEM (KRA) ====================

enum class TaxType(val displayName: String, val description: String) {
    PAYE("PAYE (Income Tax)", "Monthly pay-as-you-earn on your business profit"),
    BUSINESS_TAX("Business Tax", "Annual tax on company turnover"),
    PROPERTY_RATES("Property Rates", "County government rates on owned property"),
    VAT("VAT", "16% value added tax on taxable sales"),
    CAPITAL_GAINS("Capital Gains Tax", "5% on profit from selling assets/shares/cars"),
    NHIF("NHIF / SHIF", "Health insurance contribution"),
    NSSF("NSSF", "National Social Security Fund contribution")
}

// A progressive tax bracket (monthly, KES)
data class TaxBracket(
    val upperBound: Long,   // upper limit of this band (Long.MAX_VALUE for top)
    val rate: Double        // fraction, e.g. 0.25
)

// Realistic Kenyan PAYE monthly bands (2024/2025)
val PAYE_BRACKETS: List<TaxBracket> = listOf(
    TaxBracket(24_000, 0.0),
    TaxBracket(32_333, 0.10),
    TaxBracket(500_000, 0.25),
    TaxBracket(800_000, 0.30),
    TaxBracket(Long.MAX_VALUE, 0.35)
)

// A single applied tax line in a period
@Entity(tableName = "tax_record")
@TypeConverters(Converters::class)
data class TaxRecord(
    @PrimaryKey val id: String,
    val type: TaxType,
    val period: String,          // e.g. "2026-11" or "2026-Q4"
    val taxableBase: Long,
    val amount: Long,
    val paid: Boolean = false,
    val dueAt: Long? = null,     // deadline for quarterly filings
    val penalty: Long = 0,       // late-filing penalty accrued
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalDue: Long get() = amount + penalty
}

// A full tax bill produced at period close
data class TaxBill(
    val period: String,
    val lines: List<TaxRecord>,
    val total: Long
) {
    val payable: Long get() = lines.filter { !it.paid }.sumOf { it.amount }
}

// ==================== ELECTIONS ====================

enum class Office(val displayName: String, val filingFee: Long, val seats: Int, val termYears: Int) {
    MCCA("Nairobi Governor", 500_000, 1, 5),
    SENATOR("Nairobi Senator", 200_000, 1, 5),
    MP("Member of Parliament", 100_000, 1, 5),
    MCA("Ward Representative (MCA)", 20_000, 1, 5)
}

enum class CandidateKind { PLAYER, NPC }

enum class ElectionStage(val displayName: String) {
    NOMINATION("Nominations Open"),
    CAMPAIGN("Campaign Period"),
    ELECTION_DAY("Election Day"),
    CONCLUDED("Results Announced")
}

@Entity(tableName = "candidate")
@TypeConverters(Converters::class)
data class Candidate(
    @PrimaryKey val id: String,
    val name: String,
    val kind: CandidateKind,
    val office: Office,
    val party: String,
    val campaignFunds: Long = 0,
    val baseSupport: Double = 0.0,     // 0..1 underlying popularity
    val ralliesHeld: Int = 0,
    val adsBought: Int = 0,
    val endorsements: Int = 0,
    val manifesto: String = "",
    val isPlayer: Boolean = false
)

@Entity(tableName = "election")
@TypeConverters(Converters::class)
data class Election(
    @PrimaryKey val id: String,
    val office: Office,
    val stage: ElectionStage,
    val candidates: List<Candidate>,
    val daysUntilElection: Int = 30,
    val winnerId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val leadingCandidate: Candidate?
        get() = candidates.maxByOrNull { projectedVotes(it) }

    fun projectedVotes(c: Candidate): Double {
        val spent = (c.ralliesHeld * 0.03) + (c.adsBought * 0.02) + (c.endorsements * 0.05)
        return (c.baseSupport + spent).coerceIn(0.0, 1.0)
    }

    fun totalProjected(c: Candidate): Int = (projectedVotes(c) * 100_000).toInt()
}

// Default NPC candidates for Nairobi
val DEFAULT_NPC_CANDIDATES: List<Candidate> = listOf(
    Candidate("cand_npc_1", "Hon. Peter Mwangi", CandidateKind.NPC, Office.MCCA, "Party of Development", baseSupport = 0.35, manifesto = "Better roads and clean water for all wards."),
    Candidate("cand_npc_2", "Dr. Sarah Otieno", CandidateKind.NPC, Office.MCCA, "Nairobi First", baseSupport = 0.30, manifesto = "Free maternal healthcare and youth jobs."),
    Candidate("cand_npc_3", "Businessman Kamau", CandidateKind.NPC, Office.MCCA, "Independent", baseSupport = 0.20, manifesto = "Cut county wastage, lower business permits."),
    Candidate("cand_npc_4", "Activist Achieng", CandidateKind.NPC, Office.MCCA, "Green Kenya", baseSupport = 0.15, manifesto = "Plant 10 million trees, ban single-use plastic.")
)