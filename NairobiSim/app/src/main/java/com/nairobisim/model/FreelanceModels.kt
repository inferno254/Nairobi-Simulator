package com.nairobisim.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nairobisim.R
import com.nairobisim.data.converters.Converters
import java.util.*

// ==================== FREELANCE SKILLS ====================

enum class FreelanceSkill(val displayName: String, val iconRes: Int) {
    WRITING("Writing", R.drawable.ic_pen),
    GRAPHIC_DESIGN("Graphic Design", R.drawable.ic_palette),
    PROGRAMMING("Programming", R.drawable.ic_code),
    DATA_ENTRY("Data Entry", R.drawable.ic_keyboard),
    TRANSCRIPTION("Transcription", R.drawable.ic_mic),
    DIGITAL_MARKETING("Digital Marketing", R.drawable.ic_megaphone),
    VIDEO_EDITING("Video Editing", R.drawable.ic_film),
    TRANSLATION("Translation", R.drawable.ic_language)
}

// ==================== SKILL LEVEL ====================

enum class FreelanceLevel(val displayName: String, val minXp: Int) {
    BEGINNER("Beginner", 0),
    INTERMEDIATE("Intermediate", 250),
    ADVANCED("Advanced", 750),
    EXPERT("Expert", 1500)
}

// ==================== FREELANCE CLIENT ====================

data class FreelanceClient(
    val id: String,
    val name: String,
    val company: String,
    val reliability: Double,      // 0.0-1.0 chance client pays on time
    val haggleWillingness: Double, // 0.0-1.0 how open to negotiation
    val preferredSkills: List<FreelanceSkill>
) {
    fun paysOnTime(): Boolean = Math.random() < reliability
}

// ==================== FREELANCE GIG ====================

data class FreelanceGig(
    val id: String,
    val title: String,
    val description: String,
    val skill: FreelanceSkill,
    val requiredLevel: FreelanceLevel,
    val basePay: Long,
    val maxPay: Long,              // ceiling after haggling
    val minPay: Long,              // floor client won't go below
    val client: FreelanceClient,
    val deadlineDays: Int,
    val competitionLevel: Int,     // 1-5, higher = more freelancers bidding
    val instantPayment: Boolean,   // false = delayed payment risk
    val createdAt: Long = System.currentTimeMillis(),
    val isTaken: Boolean = false
) {
    fun canAffordSkill(playerLevel: FreelanceLevel): Boolean =
        playerLevel.minXp >= requiredLevel.minXp

    fun effectivePay(playerLevel: FreelanceLevel): Long {
        // Skill barrier: below required level = reduced pay
        val multiplier = if (playerLevel.minXp < requiredLevel.minXp) 0.5 else 1.0
        return (basePay * multiplier).toLong()
    }
}

// ==================== FREELANCE JOB APPLICATION / CONTRACT ====================

enum class FreelanceJobStatus(val displayName: String) {
    AVAILABLE("Available"),
    APPLIED("Applied"),
    ACCEPTED("Accepted"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    PAID("Paid"),
    DELAYED("Payment Delayed"),
    REJECTED("Rejected"),
    FAILED("Failed")
}

@Entity(tableName = "freelance_contract")
@TypeConverters(Converters::class)
data class FreelanceContract(
    @PrimaryKey val id: String,
    val gigId: String,
    val gigTitle: String,
    val skill: FreelanceSkill,
    val agreedPay: Long,
    val status: FreelanceJobStatus,
    val clientName: String,
    val clientReliability: Double,
    val instantPayment: Boolean,
    val startedAt: Long,
    val completedAt: Long? = null,
    val paidAt: Long? = null
)

// ==================== PLAYER FREELANCE PROFILE ====================

data class FreelanceProfile(
    val totalEarnings: Long = 0,
    val gigsCompleted: Int = 0,
    val gigsFailed: Int = 0,
    val skillXp: Map<FreelanceSkill, Int> = emptyMap(),
    val reputation: Int = 0,      // client trust score
    val lastGigAt: Long? = null
) {
    fun xpFor(skill: FreelanceSkill): Int = skillXp[skill] ?: 0
    fun levelFor(skill: FreelanceSkill): FreelanceLevel {
        val xp = xpFor(skill)
        return FreelanceLevel.values()
            .filter { it.minXp <= xp }
            .maxByOrNull { it.minXp } ?: FreelanceLevel.BEGINNER
    }
    fun addXp(skill: FreelanceSkill, amount: Int): FreelanceProfile =
        copy(skillXp = skillXp + (skill to (xpFor(skill) + amount)))
}

// ==================== FREELANCE ECONOMY RESULT ====================

data class FreelanceOutcome(
    val success: Boolean,
    val message: String,
    val payEarned: Long = 0,
    val xpGained: Int = 0,
    val reputationDelta: Int = 0,
    val delayed: Boolean = false
)

// Default Nairobi freelance clients
val DEFAULT_FREELANCE_CLIENTS: List<FreelanceClient> = listOf(
    FreelanceClient("c_001", "James Mwangi", "Nairobi Tech Hub", 0.9, 0.3, listOf(FreelanceSkill.PROGRAMMING, FreelanceSkill.DATA_ENTRY)),
    FreelanceClient("c_002", "Sarah Wanjiku", "Westlands Marketing Co", 0.7, 0.6, listOf(FreelanceSkill.DIGITAL_MARKETING, FreelanceSkill.WRITING)),
    FreelanceClient("c_003", "Kamau Njoroge", "Kilimani Design Studio", 0.6, 0.5, listOf(FreelanceSkill.GRAPHIC_DESIGN, FreelanceSkill.VIDEO_EDITING)),
    FreelanceClient("c_004", "Aisha Hassan", "Eastleigh Translations", 0.8, 0.4, listOf(FreelanceSkill.TRANSLATION, FreelanceSkill.WRITING)),
    FreelanceClient("c_005", "Otieno Omondi", "Kariobangi Data Services", 0.5, 0.7, listOf(FreelanceSkill.DATA_ENTRY, FreelanceSkill.TRANSCRIPTION)),
    FreelanceClient("c_006", "Wanjiku Mutua", "Upper Hill Media", 0.4, 0.8, listOf(FreelanceSkill.VIDEO_EDITING, FreelanceSkill.DIGITAL_MARKETING))
)

// Default freelance gigs
val DEFAULT_FREELANCE_GIGS: List<FreelanceGig> = listOf(
    FreelanceGig("g_001", "Website Landing Page", "Build a responsive landing page for a local startup", FreelanceSkill.PROGRAMMING, FreelanceLevel.INTERMEDIATE, 8000, 12000, 6000, DEFAULT_FREELANCE_CLIENTS[0], 3, 3, true),
    FreelanceGig("g_002", "Logo Design", "Create a brand logo for a new cafe in Westlands", FreelanceSkill.GRAPHIC_DESIGN, FreelanceLevel.BEGINNER, 5000, 8000, 3000, DEFAULT_FREELANCE_CLIENTS[2], 2, 4, false),
    FreelanceGig("g_003", "Blog Article Series", "Write 5 SEO articles for a real estate blog", FreelanceSkill.WRITING, FreelanceLevel.BEGINNER, 6000, 9000, 4000, DEFAULT_FREELANCE_CLIENTS[1], 5, 3, true),
    FreelanceGig("g_004", "Data Cleaning", "Clean and format 10,000 rows of customer data", FreelanceSkill.DATA_ENTRY, FreelanceLevel.BEGINNER, 4000, 6000, 2500, DEFAULT_FREELANCE_CLIENTS[4], 1, 5, true),
    FreelanceGig("g_005", "Podcast Transcription", "Transcribe a 1-hour interview podcast", FreelanceSkill.TRANSCRIPTION, FreelanceLevel.BEGINNER, 3500, 5500, 2000, DEFAULT_FREELANCE_CLIENTS[4], 2, 2, false),
    FreelanceGig("g_006", "Social Media Campaign", "Run a 2-week Instagram ads campaign", FreelanceSkill.DIGITAL_MARKETING, FreelanceLevel.INTERMEDIATE, 10000, 15000, 7000, DEFAULT_FREELANCE_CLIENTS[1], 14, 4, false),
    FreelanceGig("g_007", "Promo Video Edit", "Edit a 3-minute promotional video for a clinic", FreelanceSkill.VIDEO_EDITING, FreelanceLevel.ADVANCED, 15000, 22000, 10000, DEFAULT_FREELANCE_CLIENTS[2], 7, 3, false),
    FreelanceGig("g_008", "Document Translation", "Translate legal docs from English to Swahili", FreelanceSkill.TRANSLATION, FreelanceLevel.INTERMEDIATE, 7000, 10000, 5000, DEFAULT_FREELANCE_CLIENTS[3], 3, 2, true),
    FreelanceGig("g_009", "Mobile App UI", "Design UI screens for a fintech app", FreelanceSkill.GRAPHIC_DESIGN, FreelanceLevel.ADVANCED, 18000, 25000, 12000, DEFAULT_FREELANCE_CLIENTS[2], 10, 5, false),
    FreelanceGig("g_010", "API Integration", "Integrate M-Pesa API into a merchant portal", FreelanceSkill.PROGRAMMING, FreelanceLevel.EXPERT, 25000, 40000, 18000, DEFAULT_FREELANCE_CLIENTS[0], 21, 4, true)
)
