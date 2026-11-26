package com.nairobisim.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nairobisim.R
import com.nairobisim.data.converters.Converters
import java.util.*

// ==================== LIFE PATHS ====================

enum class LifePath(val displayName: String, val description: String, val iconRes: Int) {
    STAY("Stay as is", "Continue current lifestyle", R.drawable.ic_street),
    SCHOOL("Go to School", "Pursue education and skills", R.drawable.ic_chama),
    LEGIT_BIZ("Legitimate Business", "Start a lawful enterprise", R.drawable.ic_business),
    TRADE("Learn a Trade", "Apprenticeship in carpentry, plumbing, electrical", R.drawable.ic_car),
    CREATIVE("Creative Arts", "Music, art, fashion, content creation", R.drawable.ic_palette),
    SPORTS("Sports & Athletics", "Football, running, boxing career", R.drawable.ic_trophy),
    RELIGIOUS("Religious Path", "Church, mosque, community service", R.drawable.ic_settings),
    LEAVE_CITY("Leave Nairobi", "Move to rural area or upcountry", R.drawable.ic_location)
}

// ==================== PERSUASION APPROACHES ====================

enum class PersuasionApproach(val displayName: String, val description: String, val iconRes: Int) {
    EMPATHETIC("Empathetic", "Listen, understand their pain, gentle guidance", R.drawable.ic_handshake),
    PRACTICAL("Practical", "Show the money, costs, and concrete steps", R.drawable.ic_money),
    AUTHORITATIVE("Authoritative", "Direct, firm, tell them what they need to hear", R.drawable.ic_run),
    INSPIRATIONAL("Inspirational", "Paint a vision of who they could become", R.drawable.ic_trophy),
    TOUGH_LOVE("Tough Love", "Call out their excuses, challenge them hard", R.drawable.ic_fight),
    RELIGIOUS("Religious/Spiritual", "Appeal to faith, prayer, divine purpose", R.drawable.ic_settings)
}

// ==================== PERSON PSYCHOLOGY ====================

data class PersonPsychology(
    val fear: Double,
    val ambition: Double,
    val familyResponsibility: Double,
    val pride: Double,
    val riskTolerance: Double,
    val trustInPlayer: Double
)

// ==================== REAL BARRIERS ====================

data class LifeBarrier(
    val type: BarrierType,
    val description: String,
    val severity: Int,
    val canPlayerHelp: Boolean,
    val costToResolve: Long
)

enum class BarrierType(val displayName: String) {
    MONEY("Money Needed"),
    CHILDCARE("Childcare"),
    TRANSPORT("Transport"),
    STIGMA("Stigma / Reputation"),
    FAMILY_PRESSURE("Family Pressure"),
    LEGAL_ISSUES("Legal / Criminal Record"),
    EDUCATION_GAP("Missing Education"),
    HOPELESSNESS("Hopelessness")
}

// ==================== COUNSELING SESSION ====================

enum class SessionStatus(val displayName: String) {
    FIRST_MEETING("First Meeting"),
    BUILDING_TRUST("Building Trust"),
    DEEP_CONVERSATION("Deep Conversation"),
    READY_TO_DECIDE("Ready to Decide"),
    FOLLOW_UP("Follow Up Needed")
}

@Entity(tableName = "counseling_session")
@TypeConverters(Converters::class)
data class CounselingSession(
    @PrimaryKey val id: String,
    val personId: String,
    val personName: String,
    val status: SessionStatus,
    val meetingsHeld: Int,
    val trustLevel: Int,
    val chosenPath: LifePath? = null,
    val isDecided: Boolean = false,
    val isConvinced: Boolean = false,
    val isOffended: Boolean = false,
    val lastApproach: PersuasionApproach? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==================== COUNSELING RESULT ====================

enum class CounselingOutcome(val displayName: String, val color: String) {
    CONVINCED("Convinced", "#2E7D32"),
    THINKING("Thinking About It", "#1565C0"),
    RESISTED("Resisted", "#FB8C00"),
    OFFENDED("Offended", "#C62828"),
    NEEDS_MORE_TIME("Needs More Time", "#9E9E9E"),
    ALREADY_ON_PATH("Already On That Path", "#6A1B9A")
}

data class CounselingResult(
    val sessionId: String,
    val personName: String,
    val outcome: CounselingOutcome,
    val suggestedPath: LifePath?,
    val approach: PersuasionApproach,
    val message: String,
    val trustDelta: Int,
    val karmaDelta: Int,
    val wisdomDelta: Int,
    val barrierAddressed: BarrierType?,
    val needsFollowUp: Boolean = false
)

// ==================== MENTOR PROFILE ====================

data class MentorProfile(
    val totalPeopleMet: Int = 0,
    val totalConvinced: Int = 0,
    val totalOffended: Int = 0,
    val totalFollowUps: Int = 0,
    val karma: Int = 0,
    val wisdom: Int = 0,
    val charisma: Int = 0,
    val lastCounseledAt: Long? = null
) {
    val successRate: Double
        get() = if (totalPeopleMet > 0) totalConvinced.toDouble() / totalPeopleMet else 0.0
    val trustLevel: String
        get() = when {
            karma >= 80 -> "Respected"
            karma >= 50 -> "Trusted"
            karma >= 20 -> "Known"
            karma >= 0 -> "Stranger"
            else -> "Distrusted"
        }
}

// ==================== SMART PERSON PROFILES ====================

@Entity(tableName = "smart_person")
@TypeConverters(Converters::class)
data class SmartPerson(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val location: String,
    val currentPath: String,
    val psychology: PersonPsychology,
    val barriers: List<LifeBarrier>,
    val suggestedPaths: List<LifePath>,
    val bestApproaches: List<PersuasionApproach>,
    val worstApproaches: List<PersuasionApproach>,
    val story: String,
    val struggle: String,
    val dream: String,
    val meetingsNeeded: Int,
    val baseTrust: Int,
    val moneyAvailable: Long,
    val createdAt: Long = System.currentTimeMillis()
)

// Default Nairobi smart persons
val DEFAULT_SMART_PERSONS: List<SmartPerson> = listOf(

    SmartPerson(
        id = "sp_001",
        name = "Juma",
        age = 19,
        location = "Kibera",
        currentPath = "Sharp boy / street hustler",
        psychology = PersonPsychology(
            fear = 0.7, ambition = 0.6, familyResponsibility = 0.8,
            pride = 0.5, riskTolerance = 0.4, trustInPlayer = 0.2
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.MONEY, "Needs KES 30,000 for electrician training", 3, true, 30000),
            LifeBarrier(BarrierType.STIGMA, "Crew will call him a traitor", 3, false, 0),
            LifeBarrier(BarrierType.LEGAL_ISSUES, "Past run-ins with kanjos", 2, false, 0)
        ),
        suggestedPaths = listOf(LifePath.TRADE, LifePath.SCHOOL),
        bestApproaches = listOf(PersuasionApproach.EMPATHETIC, PersuasionApproach.PRACTICAL),
        worstApproaches = listOf(PersuasionApproach.AUTHORITATIVE, PersuasionApproach.TOUGH_LOVE),
        story = "Juma dropped out of secondary school after his father lost his job. He now runs with corner boys demanding 'karamu' from matatus. He hates the violence but fears going home empty-handed.",
        struggle = "Between loyalty to his crew and the desire for a different life.",
        dream = "To become an electrician and wire houses in estate developments.",
        meetingsNeeded = 3,
        baseTrust = 20,
        moneyAvailable = 500
    ),

    SmartPerson(
        id = "sp_002",
        name = "Amina",
        age = 21,
        location = "Eastleigh",
        currentPath = "University dropout",
        psychology = PersonPsychology(
            fear = 0.4, ambition = 0.8, familyResponsibility = 0.7,
            pride = 0.6, riskTolerance = 0.3, trustInPlayer = 0.3
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.MONEY, "University fees unpaid", 4, true, 80000),
            LifeBarrier(BarrierType.FAMILY_PRESSURE, "Family expects her to marry young", 3, false, 0)
        ),
        suggestedPaths = listOf(LifePath.LEGIT_BIZ, LifePath.SCHOOL),
        bestApproaches = listOf(PersuasionApproach.PRACTICAL, PersuasionApproach.INSPIRATIONAL),
        worstApproaches = listOf(PersuasionApproach.TOUGH_LOVE, PersuasionApproach.AUTHORITATIVE),
        story = "Amina was studying business but had to drop out when her family couldn't pay fees. She now sells clothes at Gikomba market but dreams of owning her own boutique.",
        struggle = "Financial pressure and family expectations to marry young.",
        dream = "To open a fashion boutique in Westlands and employ other young women.",
        meetingsNeeded = 2,
        baseTrust = 30,
        moneyAvailable = 2000
    ),

    SmartPerson(
        id = "sp_003",
        name = "Kevin",
        age = 25,
        location = "Kariobangi",
        currentPath = "Jobless youth",
        psychology = PersonPsychology(
            fear = 0.6, ambition = 0.5, familyResponsibility = 0.5,
            pride = 0.7, riskTolerance = 0.2, trustInPlayer = 0.1
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.HOPELESSNESS, "Two years of job rejections", 4, false, 0),
            LifeBarrier(BarrierType.EDUCATION_GAP, "Diploma but no practical skills", 2, false, 0)
        ),
        suggestedPaths = listOf(LifePath.TRADE, LifePath.LEGIT_BIZ),
        bestApproaches = listOf(PersuasionApproach.INSPIRATIONAL, PersuasionApproach.EMPATHETIC),
        worstApproaches = listOf(PersuasionApproach.TOUGH_LOVE, PersuasionApproach.AUTHORITATIVE),
        story = "Kevin graduated with a diploma in IT but has been jobless for two years. He spends days at the cyber cafe applying for jobs that never respond. He's considering crime.",
        struggle = "Hopelessness after years of rejection.",
        dream = "To get a decent IT job or start a tech repair shop.",
        meetingsNeeded = 4,
        baseTrust = 10,
        moneyAvailable = 300
    ),

    SmartPerson(
        id = "sp_004",
        name = "Mama Njeri",
        age = 34,
        location = "Kawangware",
        currentPath = "Single mother / charcoal seller",
        psychology = PersonPsychology(
            fear = 0.5, ambition = 0.7, familyResponsibility = 0.95,
            pride = 0.4, riskTolerance = 0.1, trustInPlayer = 0.4
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.MONEY, "Can't afford boarding school fees", 4, true, 60000),
            LifeBarrier(BarrierType.CHILDCARE, "Three children need care", 3, false, 0)
        ),
        suggestedPaths = listOf(LifePath.LEGIT_BIZ, LifePath.TRADE),
        bestApproaches = listOf(PersuasionApproach.PRACTICAL, PersuasionApproach.EMPATHETIC),
        worstApproaches = listOf(PersuasionApproach.INSPIRATIONAL, PersuasionApproach.TOUGH_LOVE),
        story = "Mama Njeri sells charcoal to feed her three children. Her husband left her. She works 14 hours a day but can't afford school fees for her eldest.",
        struggle = "Exhaustion and the fear that her children will follow the streets.",
        dream = "To save enough to send her children to boarding school.",
        meetingsNeeded = 2,
        baseTrust = 40,
        moneyAvailable = 1500
    ),

    SmartPerson(
        id = "sp_005",
        name = "Brian",
        age = 17,
        location = "Dandora",
        currentPath = "Dump site scavenger",
        psychology = PersonPsychology(
            fear = 0.8, ambition = 0.4, familyResponsibility = 0.3,
            pride = 0.6, riskTolerance = 0.5, trustInPlayer = 0.1
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.EDUCATION_GAP, "Never finished primary school", 4, false, 0),
            LifeBarrier(BarrierType.MONEY, "Needs welding tools and training", 3, true, 25000),
            LifeBarrier(BarrierType.STIGMA, "Dandora scavenger label", 2, false, 0)
        ),
        suggestedPaths = listOf(LifePath.TRADE, LifePath.SCHOOL),
        bestApproaches = listOf(PersuasionApproach.EMPATHETIC, PersuasionApproach.INSPIRATIONAL),
        worstApproaches = listOf(PersuasionApproach.AUTHORITATIVE, PersuasionApproach.RELIGIOUS),
        story = "Brian scavenges scrap metal at Dandora dumpsite to survive. He's bright but never finished primary school. Gang leaders have offered him 'work'.",
        struggle = "Poverty and the lure of quick money from gangs.",
        dream = "To learn welding and work in an industrial area.",
        meetingsNeeded = 4,
        baseTrust = 15,
        moneyAvailable = 100
    ),

    SmartPerson(
        id = "sp_006",
        name = "Grace",
        age = 23,
        location = "Kilimani",
        currentPath = "Unpaid intern",
        psychology = PersonPsychology(
            fear = 0.5, ambition = 0.7, familyResponsibility = 0.4,
            pride = 0.8, riskTolerance = 0.3, trustInPlayer = 0.3
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.MONEY, "No savings to quit unpaid internship", 3, true, 15000),
            LifeBarrier(BarrierType.STIGMA, "Afraid to admit she's being exploited", 2, false, 0)
        ),
        suggestedPaths = listOf(LifePath.LEGIT_BIZ, LifePath.CREATIVE),
        bestApproaches = listOf(PersuasionApproach.EMPATHETIC, PersuasionApproach.PRACTICAL),
        worstApproaches = listOf(PersuasionApproach.TOUGH_LOVE, PersuasionApproach.AUTHORITATIVE),
        story = "Grace has been interning at a marketing agency for 8 months with no pay. She's talented but afraid to ask for money or quit. Her parents think she's employed.",
        struggle = "Exploitation and pride preventing her from speaking up.",
        dream = "To become a brand strategist and earn her own money.",
        meetingsNeeded = 2,
        baseTrust = 35,
        moneyAvailable = 800
    ),

    SmartPerson(
        id = "sp_007",
        name = "Odingo",
        age = 29,
        location = "Mathare",
        currentPath = "Former convict",
        psychology = PersonPsychology(
            fear = 0.6, ambition = 0.7, familyResponsibility = 0.6,
            pride = 0.5, riskTolerance = 0.4, trustInPlayer = 0.2
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.STIGMA, "Ex-convict label scares employers", 4, false, 0),
            LifeBarrier(BarrierType.LEGAL_ISSUES, "Criminal record", 3, false, 0),
            LifeBarrier(BarrierType.FAMILY_PRESSURE, "Old crew pressuring him back", 3, false, 0)
        ),
        suggestedPaths = listOf(LifePath.LEGIT_BIZ, LifePath.TRADE),
        bestApproaches = listOf(PersuasionApproach.PRACTICAL, PersuasionApproach.RELIGIOUS),
        worstApproaches = listOf(PersuasionApproach.AUTHORITATIVE, PersuasionApproach.INSPIRATIONAL),
        story = "Odingo served 3 years for petty theft. He's out now but employers won't hire ex-convicts. His old crew keeps visiting, pressuring him to rejoin.",
        struggle = "Stigma and the temptation to return to old ways.",
        dream = "To start a car wash and hire other former convicts.",
        meetingsNeeded = 3,
        baseTrust = 25,
        moneyAvailable = 500
    ),

    SmartPerson(
        id = "sp_008",
        name = "Zawadi",
        age = 18,
        location = "Korogocho",
        currentPath = "Teen mother",
        psychology = PersonPsychology(
            fear = 0.7, ambition = 0.6, familyResponsibility = 0.9,
            pride = 0.4, riskTolerance = 0.2, trustInPlayer = 0.3
        ),
        barriers = listOf(
            LifeBarrier(BarrierType.CHILDCARE, "Baby needs constant care", 4, false, 0),
            LifeBarrier(BarrierType.MONEY, "No income, mother resents the baby", 4, true, 20000),
            LifeBarrier(BarrierType.EDUCATION_GAP, "Dropped out in Form 3", 3, false, 0)
        ),
        suggestedPaths = listOf(LifePath.TRADE, LifePath.SCHOOL),
        bestApproaches = listOf(PersuasionApproach.EMPATHETIC, PersuasionApproach.PRACTICAL),
        worstApproaches = listOf(PersuasionApproach.TOUGH_LOVE, PersuasionApproach.AUTHORITATIVE),
        story = "Zawadi got pregnant in Form 3 and dropped out. She lives with her mother who resents the baby. She has no income and no support from the father.",
        struggle = "Shame, isolation, and no path forward.",
        dream = "To learn hairdressing and open a salon to support her child.",
        meetingsNeeded = 3,
        baseTrust = 30,
        moneyAvailable = 0
    )
)

// ==================== BETTING SYSTEM ====================

enum class SportType(val displayName: String, val iconRes: Int) {
    FOOTBALL("Football (Soccer)", R.drawable.ic_trophy),
    RUGBY("Rugby", R.drawable.ic_trophy),
    ATHLETICS("Athletics", R.drawable.ic_trophy),
    BOXING("Boxing", R.drawable.ic_fight)
}

enum class BetType(val displayName: String, val description: String) {
    MATCH_WINNER("Match Winner", "Pick which team wins"),
    OVER_UNDER("Over/Under Goals", "Total goals over or under 2.5"),
    BOTH_SCORE("Both Teams Score", "Both teams to score"),
    FIRST_GOALSCORER("First Goalscorer", "Who scores first"),
    CORRECT_SCORE("Correct Score", "Exact final score"),
    DOUBLE_CHANCE("Double Chance", "Win or draw for a team")
}

enum class BetStatus(val displayName: String, val color: String) {
    PENDING("Pending", "#FB8C00"),
    WON("Won", "#2E7D32"),
    LOST("Lost", "#C62828"),
    VOID("Void", "#9E9E9E"),
    MATCH_CANCELLED("Match Cancelled", "#9E9E9E")
}

@Entity(tableName = "betting_match")
@TypeConverters(Converters::class)
data class BettingMatch(
    @PrimaryKey val id: String,
    val sport: SportType,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val matchTime: Long,
    val homeOdds: Double,
    val drawOdds: Double,
    val awayOdds: Double,
    val overOdds: Double,
    val underOdds: Double,
    val bttsYesOdds: Double,
    val bttsNoOdds: Double,
    val isLive: Boolean = false,
    val isFinished: Boolean = false,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val winner: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bet_slip")
@TypeConverters(Converters::class)
data class BetSlip(
    @PrimaryKey val id: String,
    val matchId: String,
    val matchName: String,
    val betType: BetType,
    val selection: String,
    val odds: Double,
    val stake: Long,
    val status: BetStatus,
    val potentialWin: Long,
    val actualWin: Long = 0,
    val placedAt: Long = System.currentTimeMillis(),
    val settledAt: Long? = null
)

@Entity(tableName = "betting_balance")
@TypeConverters(Converters::class)
data class BettingBalance(
    @PrimaryKey val id: String = "betting_wallet",
    val balance: Long = 0,
    val totalWagered: Long = 0,
    val totalWon: Long = 0,
    val totalLost: Long = 0,
    val betsPlaced: Int = 0,
    val betsWon: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

// ==================== PONZI SCHEME SYSTEM ====================

enum class PonziSchemeStatus(val displayName: String, val color: String) {
    ACTIVE("Active / Collecting", "#FB8C00"),
    PAYING("Paying Returns", "#2E7D32"),
    COLLAPSED("Collapsed - Gone", "#C62828"),
    INVESTIGATED("Under Investigation", "#9E9E9E")
}

enum class PonziSchemeType(val displayName: String, val description: String) {
    INVESTMENT_CLUB("Investment Club", "Pool money, get high monthly returns"),
    FOREX_TRADING("Forex Trading", "Expert traders doubling your money"),
    CRYPTO_MINING("Crypto Mining", "Bitcoin mining farm investment"),
    REAL_ESTATE("Real Estate Flip", "Buy cheap, sell high in days"),
    AGRICULTURE("Agri-Investment", "Farm funding with harvest returns")
}

@Entity(tableName = "ponzi_scheme")
@TypeConverters(Converters::class)
data class PonziScheme(
    @PrimaryKey val id: String,
    val name: String,
    val type: PonziSchemeType,
    val promoterName: String,
    val location: String,
    val promisedReturn: Double,
    val promisedPeriod: Int,
    val minimumInvestment: Long,
    val status: PonziSchemeStatus,
    val isKnownPonzi: Boolean = false,
    val collapseRisk: Double = 0.1,
    val daysActive: Int = 0,
    val totalCollected: Long = 0,
    val totalPaidOut: Long = 0,
    val collapseDay: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val collapsedAt: Long? = null
)

@Entity(tableName = "ponzi_investment")
@TypeConverters(Converters::class)
data class PonziInvestment(
    @PrimaryKey val id: String,
    val schemeId: String,
    val schemeName: String,
    val amount: Long,
    val expectedReturn: Long,
    val expectedDate: Long,
    val status: String = "ACTIVE",
    val investedAt: Long = System.currentTimeMillis(),
    val paidOutAt: Long? = null
)

// Default Nairobi betting matches
val DEFAULT_BETTING_MATCHES: List<BettingMatch> = listOf(
    BettingMatch(
        id = "bm_001",
        sport = SportType.FOOTBALL,
        homeTeam = "Gor Mahia",
        awayTeam = "AFC Leopards",
        league = "FKF Premier League",
        matchTime = System.currentTimeMillis() + 86400000,
        homeOdds = 2.10,
        drawOdds = 3.20,
        awayOdds = 3.40,
        overOdds = 1.85,
        underOdds = 1.95,
        bttsYesOdds = 1.75,
        bttsNoOdds = 2.05
    ),
    BettingMatch(
        id = "bm_002",
        sport = SportType.FOOTBALL,
        homeTeam = "Tusker FC",
        awayTeam = "Ulinzi Stars",
        league = "FKF Premier League",
        matchTime = System.currentTimeMillis() + 172800000,
        homeOdds = 1.85,
        drawOdds = 3.40,
        awayOdds = 4.10,
        overOdds = 2.00,
        underOdds = 1.80,
        bttsYesOdds = 1.90,
        bttsNoOdds = 1.90
    ),
    BettingMatch(
        id = "bm_003",
        sport = SportType.FOOTBALL,
        homeTeam = "Kenya",
        awayTeam = "Uganda",
        league = "CECAFA Cup",
        matchTime = System.currentTimeMillis() + 259200000,
        homeOdds = 1.95,
        drawOdds = 3.30,
        awayOdds = 3.80,
        overOdds = 1.90,
        underOdds = 1.90,
        bttsYesOdds = 1.80,
        bttsNoOdds = 2.00
    )
)

// Default Nairobi ponzi schemes
val DEFAULT_PONZI_SCHEMES: List<PonziScheme> = listOf(
    PonziScheme(
        id = "ponzi_001",
        name = "Nairobi Wealth Club",
        type = PonziSchemeType.INVESTMENT_CLUB,
        promoterName = "Mr. Otieno",
        location = "Westlands",
        promisedReturn = 100.0,
        promisedPeriod = 30,
        minimumInvestment = 10000,
        status = PonziSchemeStatus.ACTIVE,
        isKnownPonzi = true,
        collapseRisk = 0.15,
        daysActive = 5,
        totalCollected = 5000000,
        totalPaidOut = 2000000
    ),
    PonziScheme(
        id = "ponzi_002",
        name = "Forex Masters Kenya",
        type = PonziSchemeType.FOREX_TRADING,
        promoterName = "Sarah Wanjiku",
        location = "Kilimani",
        promisedReturn = 50.0,
        promisedPeriod = 14,
        minimumInvestment = 25000,
        status = PonziSchemeStatus.ACTIVE,
        isKnownPonzi = true,
        collapseRisk = 0.10,
        daysActive = 12,
        totalCollected = 12000000,
        totalPaidOut = 3000000
    ),
    PonziScheme(
        id = "ponzi_003",
        name = "Bitcoin Mine East Africa",
        type = PonziSchemeType.CRYPTO_MINING,
        promoterName = "James Mwangi",
        location = "Upper Hill",
        promisedReturn = 200.0,
        promisedPeriod = 60,
        minimumInvestment = 50000,
        status = PonziSchemeStatus.ACTIVE,
        isKnownPonzi = true,
        collapseRisk = 0.08,
        daysActive = 45,
        totalCollected = 25000000,
        totalPaidOut = 5000000
    ),
    PonziScheme(
        id = "ponzi_004",
        name = "Maji Mazuri Real Estate",
        type = PonziSchemeType.REAL_ESTATE,
        promoterName = "Kamau Njoroge",
        location = "Karen",
        promisedReturn = 30.0,
        promisedPeriod = 90,
        minimumInvestment = 100000,
        status = PonziSchemeStatus.ACTIVE,
        isKnownPonzi = true,
        collapseRisk = 0.05,
        daysActive = 8,
        totalCollected = 15000000,
        totalPaidOut = 0
    )
)
