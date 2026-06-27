package com.stepandemianenko.dsaprep.domain.repository

import com.stepandemianenko.dsaprep.domain.model.ProblemDifficulty
import com.stepandemianenko.dsaprep.domain.model.ProblemSession
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus
import kotlinx.coroutines.flow.Flow

interface ProblemSessionRepository {
    suspend fun insertSession(
        problemTitle: String,
        problemLink: String?,
        difficulty: ProblemDifficulty,
        topic: ProblemTopic,
        startedAtEpochMillis: Long,
        finishedAtEpochMillis: Long,
        durationSeconds: Long,
        goalMinutes: Int,
        solvedStatus: SolvedStatus,
        timeComplexity: String?,
        spaceComplexity: String?,
        mainApproach: String?,
        mistakeOrBlocker: String?,
        confidence: Int,
        dateIso: String
    ): Long

    suspend fun updateSessionReview(
        sessionId: Long,
        reviewPattern: String?,
        reviewKeyInsight: String?,
        reviewMistake: String?,
        reviewRewriteStatus: RewriteStatus?,
        reviewFinalTakeaway: String?
    )

    fun getSessionsForDate(dateIso: String): Flow<List<ProblemSession>>

    fun getRecentSessions(limit: Int): Flow<List<ProblemSession>>

    fun getAllSessions(): Flow<List<ProblemSession>>
}
