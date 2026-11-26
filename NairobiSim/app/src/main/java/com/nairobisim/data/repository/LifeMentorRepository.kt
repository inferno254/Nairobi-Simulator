package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.CounselingSessionDao
import com.nairobisim.data.dao.SmartPersonDao
import com.nairobisim.model.*
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class LifeMentorRepository(
    private val db: AppDatabase,
    private val smartPersonDao: SmartPersonDao,
    private val counselingSessionDao: CounselingSessionDao
) {

    private val random = Random.Default

    fun generateCases(mentorProfile: MentorProfile): List<SmartPerson> {
        return DEFAULT_SMART_PERSONS.map { person ->
            val adjustedTrust = (person.psychology.trustInPlayer + (mentorProfile.karma * 0.01)).coerceIn(0.0, 1.0)
            person.copy(psychology = person.psychology.copy(trustInPlayer = adjustedTrust))
        }
    }

    suspend fun convince(
        person: SmartPerson,
        suggestedPath: LifePath,
        approach: PersuasionApproach,
        mentorProfile: MentorProfile
    ): CounselingResult {
        val playerWisdom = mentorProfile.wisdom
        val playerKarma = mentorProfile.karma

        var successChance = person.psychology.trustInPlayer
        successChance += (playerWisdom * 0.01)
        successChance += (playerKarma * 0.005)
        successChance -= person.psychology.pride

        if (approach in person.bestApproaches) successChance += 0.2
        if (approach in person.worstApproaches) successChance -= 0.25

        when (suggestedPath) {
            LifePath.SCHOOL -> successChance -= 0.1
            LifePath.TRADE -> successChance -= 0.05
            LifePath.LEAVE_CITY -> successChance -= 0.15
            LifePath.RELIGIOUS -> successChance -= 0.05
            else -> {}
        }
        successChance = successChance.coerceIn(0.05, 0.95)

        if (person.currentPath.contains(suggestedPath.displayName, ignoreCase = true)) {
            return buildResult(person, suggestedPath, approach, CounselingOutcome.ALREADY_ON_PATH,
                "${person.name} is already on the ${suggestedPath.displayName} path.",
                0, 0, 0, null, false)
        }

        val roll = random.nextDouble()
        val outcome: CounselingOutcome
        val message: String
        val trustDelta: Int
        val karmaDelta: Int
        val wisdomDelta: Int
        val barrierAddressed: BarrierType?

        if (roll < successChance) {
            outcome = CounselingOutcome.CONVINCED
            message = "${person.name} listened. 'You're right - I should try ${suggestedPath.displayName.lowercase()}.' They feel hopeful for the first time."
            trustDelta = 15
            karmaDelta = 15
            wisdomDelta = 5
            barrierAddressed = person.barriers.firstOrNull { it.canPlayerHelp }?.type
        } else if (roll < successChance + 0.25) {
            outcome = CounselingOutcome.THINKING
            message = "${person.name} said they'll think about it. 'Maybe... I'll consider ${suggestedPath.displayName.lowercase()}.'"
            trustDelta = 5
            karmaDelta = 5
            wisdomDelta = 2
            barrierAddressed = null
        } else if (roll < successChance + 0.45) {
            outcome = CounselingOutcome.RESISTED
            message = "${person.name} pushed back. 'That's easy for you to say. You don't know my life.'"
            trustDelta = -2
            karmaDelta = -2
            wisdomDelta = 1
            barrierAddressed = null
        } else {
            outcome = CounselingOutcome.OFFENDED
            message = "${person.name} got offended. 'Mind your own business!' They walked away angry."
            trustDelta = -10
            karmaDelta = -10
            wisdomDelta = 0
            barrierAddressed = null
        }

        return buildResult(person, suggestedPath, approach, outcome, message,
            trustDelta, karmaDelta, wisdomDelta, barrierAddressed,
            outcome == CounselingOutcome.THINKING)
    }

    private fun buildResult(
        person: SmartPerson,
        suggestedPath: LifePath,
        approach: PersuasionApproach,
        outcome: CounselingOutcome,
        message: String,
        trustDelta: Int,
        karmaDelta: Int,
        wisdomDelta: Int,
        barrierAddressed: BarrierType?,
        needsFollowUp: Boolean
    ): CounselingResult = CounselingResult(
        sessionId = person.id,
        personName = person.name,
        outcome = outcome,
        suggestedPath = suggestedPath,
        approach = approach,
        message = message,
        trustDelta = trustDelta,
        karmaDelta = karmaDelta,
        wisdomDelta = wisdomDelta,
        barrierAddressed = barrierAddressed,
        needsFollowUp = needsFollowUp
    )

    fun getSessionHistory(): Flow<List<CounselingSession>> =
        counselingSessionDao.getAllSessionsFlow()

    suspend fun getProfile(): MentorProfile {
        val sessions = counselingSessionDao.getAllSessionsOnce()
        return MentorProfile(
            totalPeopleMet = sessions.map { it.personId }.distinct().size,
            totalConvinced = sessions.count { it.isConvinced },
            totalOffended = sessions.count { it.isOffended },
            karma = sessions.count { it.isConvinced } * 5,
            wisdom = sessions.size * 2
        )
    }
}