package com.nairobisim.model

// ==================== FOREIGN CAR IMPORT MARKET ====================

enum class ImportSourceCountry(
    val displayName: String,
    val baseShippingCost: Long, // KES to Mombasa
    val shippingDays: Int,
    val reliability: Double // chance car arrives as described
) {
    JAPAN("Japan", 150_000, 21, 0.95),
    UNITED_KINGDOM("United Kingdom", 180_000, 28, 0.90),
    DUBAI("Dubai, UAE", 120_000, 14, 0.85),
    SOUTH_AFRICA("South Africa", 90_000, 10, 0.80),
    THAILAND("Thailand", 200_000, 25, 0.88);

    val currency: String
        get() = when (this) {
            JAPAN -> "JPY"
            UNITED_KINGDOM -> "GBP"
            DUBAI -> "AED"
            SOUTH_AFRICA -> "ZAR"
            THAILAND -> "THB"
        }
}

data class ForeignCarListing(
    val id: String,
    val sourceCountry: ImportSourceCountry,
    val brand: String,        // "Toyota", "Nissan", "Mercedes", "BMW", etc.
    val model: String,        // "Corolla", "Prado", "Range Rover", etc.
    val year: Int,
    val mileageKm: Int,
    val engineCapacity: Int,  // cc - affects excise duty
    val condition: String,    // "Grade 4", "Grade 3.5", etc.
    val priceForeign: Double, // Price in foreign currency
    val exchangeRate: Double, // 1 foreign unit = KES
    val auctionFee: Long,     // Auction house fee in KES
    val imageUrl: String = ""
) {
    val priceKES: Long get() = (priceForeign * exchangeRate).toLong()
    val cifValue: Long get() = priceKES + sourceCountry.baseShippingCost + auctionFee
}

// KRA Import Duty Calculation for motor vehicles
data class ImportDutyBreakdown(
    val cifValue: Long,           // Cost, Insurance, Freight
    val importDuty: Long,         // 25% of CIF
    val exciseDuty: Long,         // 20-30% depends on engine capacity
    val vat: Long,                // 16% of (CIF + Import Duty + Excise)
    val idf: Long,                // 2% of CIF
    val rdl: Long,                // 1.5% of CIF
    val portCharges: Long,        // Mombasa port handling
    val clearingAgentFee: Long,   // Clearing agent
    val totalTaxes: Long,
    val totalLandedCost: Long     // CIF + all taxes + charges
)

fun calculateImportDuty(cifValue: Long, engineCapacityCC: Int): ImportDutyBreakdown {
    val importDuty = (cifValue * 0.25).toLong()

    // Excise duty based on engine capacity (Kenya rates)
    val exciseRate = when {
        engineCapacityCC <= 1500 -> 0.20   // 20% for <= 1500cc
        engineCapacityCC <= 2000 -> 0.25   // 25% for 1501-2000cc
        engineCapacityCC <= 3000 -> 0.30   // 30% for 2001-3000cc
        else -> 0.35                       // 35% for > 3000cc
    }
    val exciseDuty = ((cifValue + importDuty) * exciseRate).toLong()

    val vatBase = cifValue + importDuty + exciseDuty
    val vat = (vatBase * 0.16).toLong()

    val idf = (cifValue * 0.02).toLong()
    val rdl = (cifValue * 0.015).toLong()
    val portCharges = 25_000L
    val clearingAgentFee = 50_000L

    val totalTaxes = importDuty + exciseDuty + vat + idf + rdl
    val totalLandedCost = cifValue + totalTaxes + portCharges + clearingAgentFee

    return ImportDutyBreakdown(
        cifValue = cifValue,
        importDuty = importDuty,
        exciseDuty = exciseDuty,
        vat = vat,
        idf = idf,
        rdl = rdl,
        portCharges = portCharges,
        clearingAgentFee = clearingAgentFee,
        totalTaxes = totalTaxes,
        totalLandedCost = totalLandedCost
    )
}

// Default foreign car listings (copyright-safe names)
val FOREIGN_CAR_MARKET = listOf(
    ForeignCarListing("fc1", ImportSourceCountry.JAPAN, "Toyo", "Corolla Axio", 2018, 45000, 1500, "Grade 4", 1_200_000.0, 0.95, 30_000),
    ForeignCarListing("fc2", ImportSourceCountry.JAPAN, "Toyo", "Prado TX", 2015, 85000, 2700, "Grade 3.5", 2_800_000.0, 0.95, 50_000),
    ForeignCarListing("fc3", ImportSourceCountry.JAPAN, "Niss", "X-Trail", 2017, 60000, 2000, "Grade 4", 1_600_000.0, 0.95, 40_000),
    ForeignCarListing("fc4", ImportSourceCountry.JAPAN, "Hond", "Vezel", 2016, 55000, 1500, "Grade 4", 1_400_000.0, 0.95, 35_000),
    ForeignCarListing("fc5", ImportSourceCountry.UNITED_KINGDOM, "Merz", "C-Class", 2017, 70000, 2000, "Good", 3_200_000.0, 165.0, 60_000),
    ForeignCarListing("fc6", ImportSourceCountry.UNITED_KINGDOM, "BMV", "X5", 2016, 90000, 3000, "Good", 4_500_000.0, 165.0, 80_000),
    ForeignCarListing("fc7", ImportSourceCountry.DUBAI, "Land", "Cruiser V8", 2014, 120000, 4500, "Fair", 3_800_000.0, 34.5, 70_000),
    ForeignCarListing("fc8", ImportSourceCountry.DUBAI, "Pors", "Cayenne", 2015, 80000, 3000, "Fair", 4_200_000.0, 34.5, 90_000),
    ForeignCarListing("fc9", ImportSourceCountry.SOUTH_AFRICA, "For", "Ranger", 2018, 50000, 2200, "Good", 2_400_000.0, 7.2, 45_000),
    ForeignCarListing("fc10", ImportSourceCountry.THAILAND, "Isuz", "D-Max", 2019, 30000, 2500, "Grade 4", 1_900_000.0, 3.8, 40_000)
)

// Import process states
enum class ImportStatus {
    AUCTION_WON,        // Won the auction abroad
    SHIPPING,           // On vessel to Mombasa
    AT_PORT,            // Arrived at Mombasa port
    CUSTOMS_CLEARANCE,  // Being cleared by KRA
    READY_FOR_PICKUP,   // Cleared, ready to collect
    DELIVERED           // Delivered to owner
}

// Customs bribery at port
data class CustomsEncounter(
    val id: String,
    val carListing: ForeignCarListing,
    val dutyOwed: Long,
    val officerDemand: Long,    // What the customs officer asks for
    val minimumAcceptable: Long,
    val successChance: Double,
    val reputationRisk: Double,
    val timestamp: Long = System.currentTimeMillis()
)

val DEFAULT_CUSTOMS_SCENARIOS = listOf(
    CustomsEncounter("cs1", FOREIGN_CAR_MARKET.first(), 500_000, 100_000, 50_000, 0.70, 0.25),
    CustomsEncounter("cs2", FOREIGN_CAR_MARKET.get(1) ?: FOREIGN_CAR_MARKET.first(), 1_200_000, 250_000, 150_000, 0.65, 0.30),
    CustomsEncounter("cs3", FOREIGN_CAR_MARKET.get(2) ?: FOREIGN_CAR_MARKET.first(), 800_000, 150_000, 100_000, 0.75, 0.20)
)

// Imported car that player owns
data class ImportedCar(
    val id: String,
    val listing: ForeignCarListing,
    val dutyBreakdown: ImportDutyBreakdown,
    val status: ImportStatus,
    val purchaseDate: Long = System.currentTimeMillis(),
    val arrivedDate: Long? = null,
    val clearedDate: Long? = null,
    val bribesPaid: Long = 0,
    val finesPaid: Long = 0,
    var currentValue: Long = 0,
    var conditionPercent: Int = 100
) {
    val totalCost: Long get() = listing.priceKES + listing.sourceCountry.baseShippingCost + listing.auctionFee + dutyBreakdown.totalTaxes + dutyBreakdown.portCharges + dutyBreakdown.clearingAgentFee + bribesPaid + finesPaid
    val isLocallyPriced: Boolean get() = currentValue > totalCost
}
