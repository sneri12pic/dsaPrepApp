package com.stepandemianenko.dsaprep.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stepandemianenko.dsaprep.domain.model.ProblemDifficulty
import com.stepandemianenko.dsaprep.domain.model.ProblemSession
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus

@Entity(
    tableName = "problem_sessions",
    indices = [
        Index("dateIso"),
        Index("topic"),
        Index("difficulty"),
        Index("startedAtEpochMillis")
    ]
)
data class ProblemSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val problemTitle: String,
    val problemLink: String?,
    val difficulty: String,
    val topic: String,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long?,
    val durationSeconds: Long,
    val goalMinutes: Int,
    val solvedStatus: String,
    val timeComplexity: String?,
    val spaceComplexity: String?,
    val mainApproach: String?,
    val mistakeOrBlocker: String?,
    val confidence: Int,
    val reviewPattern: String?,
    val reviewKeyInsight: String?,
    val reviewMistake: String?,
    val reviewRewriteStatus: String?,
    val reviewFinalTakeaway: String?,
    val dateIso: String
)

fun ProblemSessionEntity.toProblemSession(): ProblemSession = ProblemSession(
    id = id,
    problemTitle = problemTitle,
    problemLink = problemLink,
    difficulty = ProblemDifficulty.valueOf(difficulty),
    topic = ProblemTopic.valueOf(topic),
    startedAtEpochMillis = startedAtEpochMillis,
    finishedAtEpochMillis = finishedAtEpochMillis,
    durationSeconds = durationSeconds,
    goalMinutes = goalMinutes,
    solvedStatus = SolvedStatus.valueOf(solvedStatus),
    timeComplexity = timeComplexity,
    spaceComplexity = spaceComplexity,
    mainApproach = mainApproach,
    mistakeOrBlocker = mistakeOrBlocker,
    confidence = confidence,
    reviewPattern = reviewPattern,
    reviewKeyInsight = reviewKeyInsight,
    reviewMistake = reviewMistake,
    reviewRewriteStatus = reviewRewriteStatus?.let(RewriteStatus::valueOf),
    reviewFinalTakeaway = reviewFinalTakeaway,
    dateIso = dateIso
)
