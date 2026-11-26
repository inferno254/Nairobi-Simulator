package com.nairobisim.model

data class NPCharacter(
    val id: String,
    val name: String,
    val archetype: NPCArchetype,
    val location: NairobiArea,
    val business: String?,
    val personality: NPPersonality,
    var kesBalance: Long = 0,
    var reputation: Int = 50,
    var level: Int = 1,
    var isActive: Boolean = false,
    var lastActivity: Long = 0,
    var businessesOwned: List<String> = emptyList(),
    var carsOwned: List<String> = emptyList(),
    var sharePortfolioValue: Double = 0.0,
    var totalNetWorth: Long = 0
) {
    val displayName: String get() = name
    val isCPUControlled: Boolean get() = true
}

enum class NPCArchetype(
    val displayName: String,
    val description: String,
    val startingCapital: Long,
    val riskTolerance: Double, // 0.0-1.0
    val ethics: Double // 0.0-1.0 (higher = less likely to bribe)
) {
    MATATU_OWNER("Matatu Owner", "Transport mogul with route on Ngong Road", 500000, 0.6, 0.4),
    KIOSK_MAMA("Kiosk Mama", "Neighborhood duka owner in Kibera", 80000, 0.3, 0.7),
    TENDERPRENEUR("Tenderpreneur", "Government contracts specialist in Westlands", 2000000, 0.9, 0.2),
    CYBER_CAFE_OWNER("Cyber Cafe Owner", "M-Pesa agent and cyber in Eastleigh", 150000, 0.4, 0.6),
    RESTAURANT_OWNER("Restaurant Owner", "Nyama choma joint in Kilimani", 400000, 0.5, 0.5),
    CAR_DEALER("Car Dealer", "Second-hand import dealer on Mombasa Road", 3000000, 0.7, 0.3),
    REAL_ESTATE_AGENT("Real Estate Agent", "Land broker in Karen/Runda", 5000000, 0.8, 0.3),
    SHARE_TRADER("Share Trader", "NSE day trader in CBD", 1000000, 0.8, 0.5),
    FOREX_TRADER("Forex Trader", "Bureau de change operator in CBD", 500000, 0.9, 0.4),
    CHAMA_LEADER("Chama Leader", "Women's merry-go-round organizer in Kayole", 50000, 0.2, 0.8),
    BODABODA_RIDER("Bodaboda Rider", "Motorcycle taxi operator in Dandora", 150000, 0.5, 0.6),
    FISH_MONGER("Fish Monger", "Gikomba market fish seller", 100000, 0.4, 0.7),
    MITUMBA_DEALER("Mitumba Dealer", "Second-hand clothes in Gikomba", 200000, 0.5, 0.5),
    POSHO_MILL_OWNER("Posho Mill Owner", "Maize miller in rural outskirts", 300000, 0.3, 0.7),
    DAIRY_FARMER("Dairy Farmer", "Zero-grazing dairy farmer in Kiambu", 800000, 0.4, 0.7);

    val moralAlignment: String
        get() = when {
            ethics >= 0.7 -> "Principled"
            ethics >= 0.4 -> "Pragmatic"
            else -> "Flexible"
        }
}

enum class NPPersonality(
    val displayName: String,
    val aggression: Double, // 0.0-1.0
    caution: Double // 0.0-1.0
) {
    AMBITIOUS("The Hustler", 0.8, 0.3),
    CAUTIOUS("The Careful One", 0.3, 0.8),
    BALANCED("The Steady", 0.5, 0.5),
    RUTHLESS("The Shark", 0.9, 0.2),
    HONORABLE("The Principled", 0.4, 0.7);
}

// Default NPC roster - authentic Nairobi business archetypes
val DEFAULT_NPCS = listOf(
    NPCharacter("npc_1", "Mama Mboga", NPCArchetype.KIOSK_MAMA, NairobiArea.KIBERA, "Kiosk", NPPersonality.HONORABLE, kesBalance = 120000, reputation = 75, level = 3),
    NPCharacter("npc_2", "Kamau Matatu", NPCArchetype.MATATU_OWNER, NairobiArea.NGONG_ROAD, "Matatu Sacco", NPPersonality.AMBITIOUS, kesBalance = 800000, reputation = 60, level = 4),
    NPCharacter("npc_3", "Wanjiku Tenders", NPCArchetype.TENDERPRENEUR, NairobiArea.WESTLANDS, "Construction", NPPersonality.RUTHLESS, kesBalance = 5000000, reputation = 30, level = 6),
    NPCharacter("npc_4", "Omondi Cyber", NPCArchetype.CYBER_CAFE_OWNER, NairobiArea.EASTLEIGH, "Cyber Cafe", NPPersonality.BALANCED, kesBalance = 200000, reputation = 65, level = 3),
    NPCharacter("npc_5", "Akinyi Choma", NPCArchetype.RESTAURANT_OWNER, NairobiArea.KILIMANI, "Restaurant", NPPersonality.BALANCED, kesBalance = 600000, reputation = 70, level = 4),
    NPCharacter("npc_6", "Otieno Cars", NPCArchetype.CAR_DEALER, NairobiArea.MOMBASA_ROAD, "Car Dealership", NPPersonality.AMBITIOUS, kesBalance = 12000000, reputation = 45, level = 5),
    NPCharacter("npc_7", "Njeri Land", NPCArchetype.REAL_ESTATE_AGENT, NairobiArea.KAREN, "Real Estate", NPPersonality.RUTHLESS, kesBalance = 25000000, reputation = 25, level = 7),
    NPCharacter("npc_8", "Kipchoge Shares", NPCArchetype.SHARE_TRADER, NairobiArea.CBD, "Share Trading", NPPersonality.AMBITIOUS, kesBalance = 3000000, reputation = 55, level = 5),
    NPCharacter("npc_9", "Atieno Forex", NPCArchetype.FOREX_TRADER, NairobiArea.CBD, "Bureau de Change", NPPersonality.RUTHLESS, kesBalance = 2000000, reputation = 40, level = 4),
    NPCharacter("npc_10", "Muthoni Chama", NPCArchetype.CHAMA_LEADER, NairobiArea.KAYOLE, "Chama Group", NPPersonality.HONORABLE, kesBalance = 80000, reputation = 85, level = 3),
    NPCharacter("npc_11", "Mutua Boda", NPCArchetype.BODABODA_RIDER, NairobiArea.DANDORA, "Bodaboda Fleet", NPPersonality.CAUTIOUS, kesBalance = 150000, reputation = 70, level = 3),
    NPCharacter("npc_12", "Wakio Fish", NPCArchetype.FISH_MONGER, NairobiArea.CBD, "Fish Farm", NPPersonality.HONORABLE, kesBalance = 300000, reputation = 80, level = 4),
    NPCharacter("npc_13", "Njuguna Mitumba", NPCArchetype.MITUMBA_DEALER, NairobiArea.CBD, "Second Hand Clothes", NPPersonality.CAUTIOUS, kesBalance = 400000, reputation = 75, level = 4),
    NPCharacter("npc_14", "Kamau Posho", NPCArchetype.POSHO_MILL_OWNER, NairobiArea.LIMURU, "Posho Mill", NPPersonality.HONORABLE, kesBalance = 500000, reputation = 80, level = 4),
    NPCharacter("npc_15", "Wanjiku Dairy", NPCArchetype.DAIRY_FARMER, NairobiArea.KIAMBU, "Dairy Farm", NPPersonality.HONORABLE, kesBalance = 900000, reputation = 85, level = 4)
)

// Nairobi-specific moral dilemma scenarios
data class MoralDilemma(
    val id: String,
    val title: String,
    val description: String,
    val archetype: NPCArchetype,
    val payBribeConsequence: String,
    val refuseBribeConsequence: String,
    val reputationImpactPay: Int,
    val reputationImpactRefuse: Int,
    val costPay: Long,
    val costRefuse: Long
)

val DEFAULT_MORAL_DILEMMAS = listOf(
    MoralDilemma(
        "dilemma_1", "The Roadblock",
        "A traffic officer flags down your matatu at a Thika Road roadblock. Your license is expired. He says 'something small' will make it disappear.",
        NPCArchetype.MATATU_OWNER,
        "You pay KES 3,000. Officer waves you through. Reputation slightly damaged.",
        "You refuse. Officer impounds your matatu for 3 days. Lost revenue.",
        -10, +15, 3000, 45000
    ),
    MoralDilemma(
        "dilemma_2", "The Permit",
        "A county officer at City Hall has been sitting on your business permit for 3 weeks. Says KES 50,000 will 'expedite' it.",
        NPCArchetype.TENDERPRENEUR,
        "Permit approved in 2 days. Word gets around you pay bribes.",
        "Permit denied. Business can't open. Lost opportunity cost.",
        -20, +25, 50000, 200000
    ),
    MoralDilemma(
        "dilemma_3", "The Tax Audit",
        "A KRA officer from Times Tower wants to 'settle' your tax audit. Says KES 200,000 makes the problem go away.",
        NPCArchetype.TENDERPRENEUR,
        "Audit closed. But you're now known as someone who pays.",
        "Full audit proceeds. 200% penalty plus back taxes.",
        -30, +40, 200000, 800000
    ),
    MoralDilemma(
        "dilemma_4", "The Health Inspector",
        "A public health officer finds 'issues' at your restaurant. Offers to look away for KES 25,000.",
        NPCArchetype.RESTAURANT_OWNER,
        "Inspector leaves. But customers heard rumors.",
        "Restaurant closed for 2 weeks for 'fumigation'.",
        -15, +20, 25000, 300000
    ),
    MoralDilemma(
        "dilemma_5", "The Construction Permit",
        "Nairobi County planning officer has your building approval on hold. Wants KES 150,000 to sign off.",
        NPCArchetype.REAL_ESTATE_AGENT,
        "Permit signed. Construction proceeds on schedule.",
        "Stop-work order issued. Daily penalties accrue.",
        -25, +30, 150000, 500000
    ),
    MoralDilemma(
        "dilemma_6", "The Customs Officer",
        "KRA customs at Mombasa Port holds your imported car shipment. Offers clearance for KES 100,000.",
        NPCArchetype.CAR_DEALER,
        "Cars cleared same day. Customers happy.",
        "Goods seized. Storage fees KES 50,000/day.",
        -20, +25, 100000, 300000
    )
)

// AI Economy Engine - CPU simulation of NPC activities
class NPCSimulationEngine {
    private val npcs = mutableListOf<NPCharacter>()
    private var isRunning = false

    fun initialize() {
        npcs.addAll(DEFAULT_NPCS)
        npcs.forEach { it.isActive = false; it.lastActivity = System.currentTimeMillis() }
    }

    fun getActiveNPCs(): List<NPCharacter> = npcs.filter { it.isActive }

    fun getAllNPCs(): List<NPCharacter> = npcs

    // CPU takes over when no real players active
    fun simulateNPCDay(realPlayersActive: Boolean) {
        if (realPlayersActive) return // CPU only runs when no real players

        isRunning = true
        npcs.forEach { npc ->
            simulateNPCActions(npc)
        }
        isRunning = false
    }

    private fun simulateNPCActions(npc: NPCharacter) {
        val random = java.util.Random()
        val shouldAct = random.nextDouble() < 0.7 // 70% chance NPC acts today

        if (!shouldAct) return

        // Random business activity
        when (random.nextInt(5)) {
            0 -> npc.kesBalance += random.nextInt(50000) + 10000 // Revenue
            1 -> npc.kesBalance = maxOf(0, npc.kesBalance - random.nextInt(20000) + 5000) // Expenses
            2 -> {
                // Moral dilemma - bribery encounter
                val dilemma = DEFAULT_MORAL_DILEMMAS.random()
                val willBribe = random.nextDouble() > (dilemma.reputationImpactPay.toDouble() / 100.0 + npc.archetype.ethics)
                if (willBribe) {
                    npc.kesBalance = maxOf(0, npc.kesBalance - dilemma.costPay)
                    npc.reputation = maxOf(0, npc.reputation + dilemma.reputationImpactPay)
                } else {
                    npc.reputation = minOf(100, npc.reputation + dilemma.reputationImpactRefuse)
                    npc.kesBalance = maxOf(0, npc.kesBalance - dilemma.costRefuse)
                }
            }
            3 -> npc.sharePortfolioValue += random.nextDouble() * 10000 - 5000 // Share trading
            4 -> npc.level = if (npc.kesBalance > npc.level * 1000000) npc.level + 1 else npc.level // Level up
        }

        npc.totalNetWorth = npc.kesBalance + npc.sharePortfolioValue.toLong()
        npc.lastActivity = System.currentTimeMillis()
    }

    fun getLeaderboardNPCs(): List<NPCharacter> = npcs.sortedByDescending { it.totalNetWorth }.take(10)
}

val npcSimulationEngine = NPCSimulationEngine()
