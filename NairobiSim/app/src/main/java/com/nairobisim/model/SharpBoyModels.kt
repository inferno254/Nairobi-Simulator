package com.nairobisim.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nairobisim.data.converters.Converters
import androidx.room.TypeConverters
import java.util.*

// ==================== SHARP BOYS / STREET ENCOUNTER SYSTEM ====================
// Nairobi street thugs who harass pedestrians, demand money, steal phones/wallets

enum class SharpBoyType(
    val displayName: String,
    val description: String,
    val aggressionLevel: Int,
    val typicalDemand: Long,
    val violenceRisk: Double,
    val phoneTheftRisk: Double,
    val color: String
) {
    CORNER_LOITERER("Corner Loiterer", "Hangs by the stage, demands 'karamu' (tribute)", 3, 200, 0.1, 0.2, "#757575"),
    PHONE_SNATCHER("Phone Snatcher", "Runs off with your phone in broad daylight", 5, 0, 0.3, 0.9, "#C62828"),
    MATATU_TOUT("Matatu Tout", "Forces you onto his matatu, overcharges", 4, 500, 0.2, 0.1, "#FB8C00"),
    PARKING_BOY("Parking Boy", "Demands payment to watch your car", 3, 100, 0.15, 0.05, "#1565C0"),
    KANJO("City Inspector", "Harasses street vendors, confiscates goods", 6, 5000, 0.4, 0.1, "#D32F2F"),
    TRAFFIC_POLICE("Traffic Police", "Roadblock bribe demand", 7, 5000, 0.3, 0.05, "#C62828"),
    SLUM_WARLORD("Slum Warlord", "Controls territory, demands protection money", 9, 10000, 0.8, 0.3, "#4A148C"),
    CORRUPT_OFFICIAL("Corrupt Official", "Demands bribe for permit/license", 8, 50000, 0.5, 0.1, "#1A8A3A");
}

enum class StreetEncounterOutcome(
    val displayName: String,
    val description: String,
    val color: String
) {
    PAID("Paid", "Gave them what they wanted", "#2E7D32"),
    REFUSED("Refused", "Stood your ground", "#1565C0"),
    NEGOTIATED("Negotiated", "Talked them down to less", "#FB8C00"),
    FOUGHT("Fought", "Physical confrontation - risky", "#C62828"),
    ESCAPED("Escaped", "Ran away before they could catch you", "#6A1B9A"),
    PHONE_STOLEN("Phone Stolen", "They took your phone - must buy new one", "#4A148C"),
    INJURED("Injured", "Got hurt - medical costs", "#C62828"),
    ARRESTED("Arrested", "Police picked you up too", "#1A8A3A");
}

@Entity(tableName = "sharp_boy_encounter")
@TypeConverters(Converters::class)
data class SharpBoyEncounter(
    @PrimaryKey val id: String,
    val boyType: SharpBoyType,
    val location: String,
    val timeOfDay: String,
    val demandedAmount: Long,
    val minimumAcceptable: Long,
    val successChance: Double,
    val violenceRisk: Double,
    val phoneTheftRisk: Double,
    val reputationImpact: Int,
    val timestamp: Long = System.currentTimeMillis(),
    var isResolved: Boolean = false,
    var outcome: StreetEncounterOutcome? = null,
    var amountPaid: Long = 0,
    var phoneStolen: Boolean = false,
    var injured: Boolean = false,
    var medicalCost: Long = 0,
    var details: String = ""
)

data class SharpBoyOutcome(
    val outcome: StreetEncounterOutcome,
    val amountPaid: Long,
    val phoneStolen: Boolean,
    val injured: Boolean,
    val medicalCost: Long,
    val reputationChange: Int,
    val message: String,
    val color: String
)

val DEFAULT_SHARP_BOY_SCENARIOS = listOf(
    SharpBoyEncounter(
        id = "sb_1",
        boyType = SharpBoyType.CORNER_LOITERER,
        location = "Kibera Stage",
        timeOfDay = "Morning Rush",
        demandedAmount = 200,
        minimumAcceptable = 100,
        successChance = 0.60,
        violenceRisk = 0.10,
        phoneTheftRisk = 0.15,
        reputationImpact = -5,
        details = "A young man blocks your path at the matatu stage. 'Karamu ndiyo ya kuingia hapa.' (Tribute is required to pass here.) He has two friends behind him."
    ),
    SharpBoyEncounter(
        id = "sb_2",
        boyType = SharpBoyType.PHONE_SNATCHER,
        location = "River Road",
        timeOfDay = "Evening",
        demandedAmount = 0,
        minimumAcceptable = 0,
        successChance = 0.30,
        violenceRisk = 0.25,
        phoneTheftRisk = 0.85,
        reputationImpact = -10,
        details = "You're walking down River Road talking on your phone. A youth on a motorcycle pulls up, snatches your phone, and speeds away before you can react."
    ),
    SharpBoyEncounter(
        id = "sb_3",
        boyType = SharpBoyType.MATATU_TOUT,
        location = "CBD Bus Station",
        timeOfDay = "Morning Rush",
        demandedAmount = 500,
        minimumAcceptable = 200,
        successChance = 0.50,
        violenceRisk = 0.20,
        phoneTheftRisk = 0.05,
        reputationImpact = -3,
        details = "The matatu tout shouts 'Stage 20! Stage 20!' and grabs your arm, forcing you into his vehicle. The fare is double the normal rate."
    ),
    SharpBoyEncounter(
        id = "sb_4",
        boyType = SharpBoyType.PARKING_BOY,
        location = "Westlands Parking",
        timeOfDay = "Afternoon",
        demandedAmount = 100,
        minimumAcceptable = 50,
        successChance = 0.70,
        violenceRisk = 0.15,
        phoneTheftRisk = 0.05,
        reputationImpact = -2,
        details = "A parking boy appears as you park your car. 'Nipe kodi ya kuwaacha gari hapa.' (Pay parking fee.) He claims to watch your car but disappears when you return."
    ),
    SharpBoyEncounter(
        id = "sb_5",
        boyType = SharpBoyType.KANJO,
        location = "City Hall Street",
        timeOfDay = "Morning",
        demandedAmount = 5000,
        minimumAcceptable = 2000,
        successChance = 0.35,
        violenceRisk = 0.30,
        phoneTheftRisk = 0.10,
        reputationImpact = -15,
        details = "Nairobi County askaris (kanjos) confiscate your goods and demand a bribe to release them. They threaten to take you to court."
    ),
    SharpBoyEncounter(
        id = "sb_6",
        boyType = SharpBoyType.SLUM_WARLORD,
        location = "Mathare",
        timeOfDay = "Late Night",
        demandedAmount = 10000,
        minimumAcceptable = 5000,
        successChance = 0.20,
        violenceRisk = 0.70,
        phoneTheftRisk = 0.30,
        reputationImpact = -25,
        details = "Walking through Mathare at night, a group of armed young men surround you. 'Hii ni eneo yetu. Lipa rent ya usiku.' (This is our area. Pay night rent.) They have machetes."
    ),
    SharpBoyEncounter(
        id = "sb_7",
        boyType = SharpBoyType.CORRUPT_OFFICIAL,
        location = "Harambee House",
        timeOfDay = "Morning",
        demandedAmount = 50000,
        minimumAcceptable = 25000,
        successChance = 0.15,
        violenceRisk = 0.05,
        phoneTheftRisk = 0.05,
        reputationImpact = -20,
        details = "A government official holds your business permit hostage. 'Something small for the chairman.' Without payment, your business stays closed."
    ),
    SharpBoyEncounter(
        id = "sb_8",
        boyType = SharpBoyType.TRAFFIC_POLICE,
        location = "Thika Road Roadblock",
        timeOfDay = "Evening",
        demandedAmount = 5000,
        minimumAcceptable = 2000,
        successChance = 0.40,
        violenceRisk = 0.10,
        phoneTheftRisk = 0.05,
        reputationImpact = -10,
        details = "Traffic police stop your car at a roadblock. Your license has a minor issue. They demand 'something for cold water' to let you pass without a ticket."
    )
)

data class PlayerPhone(
    val id: String = "phone_1",
    val brand: String = "Tecno",
    val model: String = "Spark 8",
    val purchasePrice: Long = 12000,
    var currentValue: Long = 12000,
    var isStolen: Boolean = false,
    var lastStolenDate: Long = 0,
    val replacementCost: Long = 15000
) {
    val needsReplacement: Boolean
        get() = isStolen
}
