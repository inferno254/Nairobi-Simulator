package com.nairobisim.model

// ==================== NPC JOB ROLES ====================

enum class NPCJobRole(
    val displayName: String,
    val bribeProne: Double,      // 0.0-1.0 likelihood to demand bribe
    val bribeTypical: Long,      // typical bribe amount they demand
    val powerLevel: Int,         // 1-10 how much they can affect you
    val encounterLocation: String
) {
    TRAFFIC_POLICE("Traffic Police", 0.85, 5_000, 7, "Roadblocks across Nairobi"),
    CUSTOMS_OFFICER("KRA Customs Officer", 0.75, 100_000, 9, "Mombasa Port"),
    COUNTY_OFFICER("Nairobi County Officer", 0.70, 50_000, 8, "City Hall"),
    KRA_TAX_OFFICER("KRA Tax Officer", 0.65, 200_000, 9, "Times Tower"),
    HEALTH_INSPECTOR("Public Health Officer", 0.80, 25_000, 7, "Your business premises"),
    BUILDING_INSPECTOR("County Building Inspector", 0.75, 150_000, 8, "Construction sites"),
    LICENSING_OFFICER("Single Business Permit Officer", 0.70, 30_000, 7, "Huduma Centre"),
    IMMIGRATION_OFFICER("Immigration Officer", 0.60, 40_000, 6, "Jomo Kenyatta Airport"),
    POLICE_OFFICER("General Police Officer", 0.80, 10_000, 6, "Police stations"),
    POLITICIAN("Ward MCA / Politician", 0.40, 500_000, 10, "County Assembly");

    val moralDescription: String
        get() = when {
            bribeProne >= 0.8 -> "Notorious for bribes"
            bribeProne >= 0.6 -> "Known to accept 'facilitation fees'"
            else -> "Generally professional"
        }
}

// ==================== NPC CHARACTER WITH JOB ====================

data class NPCCharacterWithJob(
    val id: String,
    val name: String,
    val jobRole: NPCJobRole,
    val location: NairobiArea,
    var kesBalance: Long = 0,
    var reputation: Int = 50,
    var level: Int = 1,
    var bribesCollected: Long = 0,
    var timesCaught: Int = 0,
    var isActive: Boolean = true,
    var lastEncounter: Long = 0,
    var personality: String = "Standard"
) {
    val isCorrupt: Boolean get() = jobRole.bribeProne > 0.5
    val canDemandBribe: Boolean get() = isActive && jobRole.bribeProne > 0.3
}

// ==================== AUTHENTIC NAIROBI NPC ROSTER WITH JOBS ====================

val NAIROBI_NPC_ROSTER = listOf(
    // Traffic Police - the classic Nairobi roadblock
    NPCCharacterWithJob("npc_police_1", "Officer Ochieng", NPCJobRole.TRAFFIC_POLICE, NairobiArea.NGONG_ROAD, kesBalance = 45000, reputation = 35, bribesCollected = 2_500_000, personality = "The Roadblock King"),
    NPCCharacterWithJob("npc_police_2", "Officer Wanjiku", NPCJobRole.TRAFFIC_POLICE, NairobiArea.WESTLANDS, kesBalance = 38000, reputation = 40, bribesCollected = 1_800_000, personality = "Polite but firm"),
    NPCCharacterWithJob("npc_police_3", "Officer Kimani", NPCJobRole.POLICE_OFFICER, NairobiArea.KIBERA, kesBalance = 32000, reputation = 45, bribesCollected = 900_000, personality = "Community officer"),

    // Customs - Mombasa port
    NPCCharacterWithJob("npc_customs_1", "Officer Kipchoge", NPCJobRole.CUSTOMS_OFFICER, NairobiArea.MOMBASA_ROAD, kesBalance = 1_200_000, reputation = 20, bribesCollected = 45_000_000, personality = "The Port Kingpin"),
    NPCCharacterWithJob("npc_customs_2", "Officer Atieno", NPCJobRole.CUSTOMS_OFFICER, NairobiArea.MOMBASA_ROAD, kesBalance = 800_000, reputation = 25, bribesCollected = 32_000_000, personality = "Strict but negotiable"),

    // County - City Hall permits
    NPCCharacterWithJob("npc_county_1", "Officer Njoroge", NPCJobRole.COUNTY_OFFICER, NairobiArea.CBD, kesBalance = 600_000, reputation = 15, bribesCollected = 18_000_000, personality = "Permit gatekeeper"),
    NPCCharacterWithJob("npc_county_2", "Officer Akinyi", NPCJobRole.LICENSING_OFFICER, NairobiArea.CBD, kesBalance = 450_000, reputation = 30, bribesCollected = 12_000_000, personality = "Huduma Centre regular"),

    // KRA - Tax
    NPCCharacterWithJob("npc_kra_1", "Officer Mutua", NPCJobRole.KRA_TAX_OFFICER, NairobiArea.CBD, kesBalance = 2_500_000, reputation = 10, bribesCollected = 85_000_000, personality = "Times Tower feared"),
    NPCCharacterWithJob("npc_kra_2", "Officer Wambui", NPCJobRole.KRA_TAX_OFFICER, NairobiArea.WESTLANDS, kesBalance = 1_800_000, reputation = 15, bribesCollected = 55_000_000, personality = "Audit specialist"),

    // Health Inspector
    NPCCharacterWithJob("npc_health_1", "Officer Otieno", NPCJobRole.HEALTH_INSPECTOR, NairobiArea.KAREN, kesBalance = 300_000, reputation = 30, bribesCollected = 8_000_000, personality = "Restaurant inspector"),
    NPCCharacterWithJob("npc_health_2", "Officer Chepkoech", NPCJobRole.HEALTH_INSPECTOR, NairobiArea.KIBERA, kesBalance = 200_000, reputation = 35, bribesCollected = 5_000_000, personality = "Street food inspector"),

    // Building Inspector
    NPCCharacterWithJob("npc_building_1", "Officer Maina", NPCJobRole.BUILDING_INSPECTOR, NairobiArea.RUNDA, kesBalance = 900_000, reputation = 15, bribesCollected = 25_000_000, personality = "Construction site regular"),
    NPCCharacterWithJob("npc_building_2", "Officer Njeri", NPCJobRole.BUILDING_INSPECTOR, NairobiArea.KAREN, kesBalance = 700_000, reputation = 20, bribesCollected = 18_000_000, personality = "High-end developments"),

    // Politician
    NPCCharacterWithJob("npc_politician_1", "MCA Kamau", NPCJobRole.POLITICIAN, NairobiArea.WESTLANDS, kesBalance = 15_000_000, reputation = 40, bribesCollected = 200_000_000, personality = "Ward development fund"),
    NPCCharacterWithJob("npc_politician_2", "MCA Wanjiku", NPCJobRole.POLITICIAN, NairobiArea.KAREN, kesBalance = 12_000_000, reputation = 35, bribesCollected = 150_000_000, personality = "Women rep aspirant")
)

// ==================== NPC-TO-PLAYER ENCOUNTER SYSTEM ====================

data class NPCEncounter(
    val id: String,
    val npc: NPCCharacterWithJob,
    val playerId: String,
    val encounterType: EncounterType,
    val description: String,
    val bribeDemanded: Long,
    val minimumAcceptable: Long,
    val successChance: Double,        // 0.0-1.0 chance bribe works
    val reputationRisk: Double,       // 0.0-1.0 chance of getting caught
    val consequenceIfCaught: String,
    val consequenceIfRefuse: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isResolved: Boolean = false,
    var wasPaid: Boolean = false,
    var amountPaid: Long = 0,
    var wasCaught: Boolean = false
)

enum class EncounterType {
    TRAFFIC_STOP("Traffic Stop", "Officer flags you down on the road"),
    PERMIT_DELAY("Permit Delay", "Your permit is being held at City Hall"),
    TAX_AUDIT("Tax Audit", "KRA wants to audit your business"),
    HEALTH_INSPECTION("Health Inspection", "Inspector found issues at your premises"),
    BUILDING_VIOLATION("Building Violation", "Construction site violation"),
    CUSTOMS_HOLD("Customs Hold", "Your goods held at Mombasa port"),
    LICENSE_RENEWAL("License Renewal", "Business license needs renewal"),
    POLITICAL_FAVOR("Political Favor", "MCA wants 'contribution' for support");

    val displayName: String
    val description: String

    constructor(displayName: String, description: String) {
        this.displayName = displayName
        this.description = description
    }
}

// ==================== ENCOUNTER OUTCOME ====================

data class EncounterOutcome(
    val encounterId: String,
    val npcName: String,
    val npcRole: NPCJobRole,
    val wasPaid: Boolean,
    val amountPaid: Long,
    val wasCaught: Boolean,
    val fineAmount: Long = 0,
    val reputationChange: Int,       // positive = good, negative = bad
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ==================== NPC ECONOMY ENGINE ====================

class NPCEconomyEngine {
    private val npcs = mutableListOf<NPCCharacterWithJob>()
    private val activeEncounters = mutableListOf<NPCEncounter>()

    init {
        npcs.addAll(NAIROBI_NPC_ROSTER)
    }

    fun getAllNPCs(): List<NPCCharacterWithJob> = npcs

    fun getNPCsByRole(role: NPCJobRole): List<NPCCharacterWithJob> = npcs.filter { it.jobRole == role }

    fun getCorruptNPCs(): List<NPCCharacterWithJob> = npcs.filter { it.isCorrupt }

    // Generate an encounter between a specific NPC role and the player
    fun generateEncounterForPlayer(role: NPCJobRole, playerId: String): NPCEncounter? {
        val npc = npcs.filter { it.jobRole == role && it.isActive }.randomOrNull() ?: return null

        val encounterType = when (role) {
            NPCJobRole.TRAFFIC_POLICE, NPCJobRole.POLICE_OFFICER -> EncounterType.TRAFFIC_STOP
            NPCJobRole.CUSTOMS_OFFICER -> EncounterType.CUSTOMS_HOLD
            NPCJobRole.COUNTY_OFFICER, NPCJobRole.LICENSING_OFFICER -> EncounterType.PERMIT_DELAY
            NPCJobRole.KRA_TAX_OFFICER -> EncounterType.TAX_AUDIT
            NPCJobRole.HEALTH_INSPECTOR -> EncounterType.HEALTH_INSPECTION
            NPCJobRole.BUILDING_INSPECTOR -> EncounterType.BUILDING_VIOLATION
            NPCJobRole.POLITICIAN -> EncounterType.POLITICAL_FAVOR
            NPCJobRole.IMMIGRATION_OFFICER -> EncounterType.LICENSE_RENEWAL
        }

        val encounter = NPCEncounter(
            id = "enc_${System.currentTimeMillis()}",
            npc = npc,
            playerId = playerId,
            encounterType = encounterType,
            description = generateEncounterDescription(role),
            bribeDemanded = npc.jobRole.bribeTypical,
            minimumAcceptable = (npc.jobRole.bribeTypical * 0.4).toLong(),
            successChance = 0.9 - (npc.jobRole.bribeProne * 0.3),
            reputationRisk = npc.jobRole.bribeProne * 0.5,
            consequenceIfCaught = generateCaughtConsequence(role),
            consequenceIfRefuse = generateRefuseConsequence(role)
        )

        activeEncounters.add(encounter)
        return encounter
    }

    // Resolve an encounter - player chooses to pay or refuse
    fun resolveEncounter(encounter: NPCEncounter, pay: Boolean, amount: Long): EncounterOutcome {
        val npc = encounter.npc
        val random = java.util.Random()

        if (pay) {
            val caught = random.nextDouble() < encounter.reputationRisk
            val bribeWorked = !caught && random.nextDouble() < encounter.successChance

            npc.bribesCollected += amount
            npc.lastEncounter = System.currentTimeMillis()

            return if (caught) {
                val fine = amount * 3
                npc.timesCaught++
                EncounterOutcome(
                    encounterId = encounter.id,
                    npcName = npc.name,
                    npcRole = npc.jobRole,
                    wasPaid = true,
                    amountPaid = amount,
                    wasCaught = true,
                    fineAmount = fine,
                    reputationChange = -30,
                    message = "CAUGHT! Paid KES $amount bribe to ${npc.name} (${npc.jobRole.displayName}). Fine: KES $fine. Your reputation is in tatters."
                )
            } else if (bribeWorked) {
                EncounterOutcome(
                    encounterId = encounter.id,
                    npcName = npc.name,
                    npcRole = npc.jobRole,
                    wasPaid = true,
                    amountPaid = amount,
                    wasCaught = false,
                    reputationChange = -10,
                    message = "Problem solved. ${npc.name} accepted KES $amount. But you feel dirty."
                )
            } else {
                EncounterOutcome(
                    encounterId = encounter.id,
                    npcName = npc.name,
                    npcRole = npc.jobRole,
                    wasPaid = true,
                    amountPaid = amount,
                    wasCaught = false,
                    reputationChange = -5,
                    message = "${npc.name} took KES $amount but problem NOT solved. You were played."
                )
            }
        } else {
            val caught = random.nextDouble() < 0.3
            val respected = !caught && random.nextDouble() < 0.6

            npc.lastEncounter = System.currentTimeMillis()

            return if (caught) {
                EncounterOutcome(
                    encounterId = encounter.id,
                    npcName = npc.name,
                    npcRole = npc.jobRole,
                    wasPaid = false,
                    amountPaid = 0,
                    wasCaught = true,
                    reputationChange = -15,
                    message = "You refused. ${npc.name} made your life difficult. Reputation damaged."
                )
            } else if (respected) {
                EncounterOutcome(
                    encounterId = encounter.id,
                    npcName = npc.name,
                    npcRole = npc.jobRole,
                    wasPaid = false,
                    amountPaid = 0,
                    wasCaught = false,
                    reputationChange = +15,
                    message = "You stood your ground. ${npc.name} respected your integrity."
                )
            } else {
                EncounterOutcome(
                    encounterId = encounter.id,
                    npcName = npc.name,
                    npcRole = npc.jobRole,
                    wasPaid = false,
                    amountPaid = 0,
                    wasCaught = false,
                    reputationChange = 0,
                    message = "Standoff. No bribe, no consequence. Tense but resolved."
                )
            }
        }
    }

    private fun generateEncounterDescription(role: NPCJobRole): String = when (role) {
        NPCJobRole.TRAFFIC_POLICE -> "You're driving through ${role.encounterLocation} when an officer flags you down."
        NPCJobRole.CUSTOMS_OFFICER -> "Your imported car is stuck at Mombasa port. An officer wants to 'help'."
        NPCJobRole.COUNTY_OFFICER -> "Your business permit is stuck at City Hall unless you 'cooperate'."
        NPCJobRole.KRA_TAX_OFFICER -> "KRA has flagged your taxes. An officer offers to 'settle' quietly."
        NPCJobRole.HEALTH_INSPECTOR -> "Health inspector found 'issues' at your business premises."
        NPCJobRole.BUILDING_INSPECTOR -> "Your construction site has a 'violation' that needs attention."
        NPCJobRole.LICENSING_OFFICER -> "Your single business permit expires soon. The officer can help."
        NPCJobRole.IMMIGRATION_OFFICER -> "Your work permit needs renewal at immigration."
        NPCJobRole.POLICE_OFFICER -> "A police officer wants to 'discuss' something with you."
        NPCJobRole.POLITICIAN -> "Your ward MCA wants a 'contribution' for community development."
    }

    private fun generateCaughtConsequence(role: NPCJobRole): String = when (role) {
        NPCJobRole.TRAFFIC_POLICE -> "License suspended, vehicle impounded, court date set."
        NPCJobRole.CUSTOMS_OFFICER -> "Goods seized, 200% penalty, blacklisted from importing."
        NPCJobRole.COUNTY_OFFICER -> "Business shut down, permit revoked, demolition notice."
        NPCJobRole.KRA_TAX_OFFICER -> "Full audit, 200% penalty, possible jail time."
        NPCJobRole.HEALTH_INSPECTOR -> "Immediate closure, health violation notice, public shaming."
        NPCJobRole.BUILDING_INSPECTOR -> "Stop-work order, daily fines, possible demolition."
        NPCJobRole.LICENSING_OFFICER -> "License revoked, business illegal, 30-day shutdown."
        NPCJobRole.POLITICIAN -> "Opposition exposes you, campaign funds frozen, public scandal."
        else -> "Official reprimand, warning issued, record marked."
    }

    private fun generateRefuseConsequence(role: NPCJobRole): String = when (role) {
        NPCJobRole.TRAFFIC_POLICE -> "Ticket issued, court date, but you kept your integrity."
        NPCJobRole.CUSTOMS_OFFICER -> "Delays, storage fees, but your name stays clean."
        NPCJobRole.COUNTY_OFFICER -> "Permit delayed, but you can appeal legally."
        NPCJobRole.KRA_TAX_OFFICER -> "Full audit proceeds, but no corruption record."
        NPCJobRole.HEALTH_INSPECTOR -> "Inspection proceeds, issues documented legally."
        NPCJobRole.BUILDING_INSPECTOR -> "Violation notice, but you can fix it properly."
        NPCJobRole.LICENSING_OFFICER -> "Renewal delayed, but processed through proper channels."
        NPCJobRole.POLITICIAN -> "No support from MCA, but your reputation intact."
        else -> "Matter proceeds through official channels."
    }

    fun getNPCReputation(npcId: String): Int = npcs.firstOrNull { it.id == npcId }?.reputation ?: 50

    fun getTotalBribesCollected(): Long = npcs.sumOf { it.bribesCollected }
}

// Singleton instance
val npcEconomy = NPCEconomyEngine()
