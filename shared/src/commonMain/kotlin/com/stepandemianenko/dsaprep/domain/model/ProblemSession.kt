package com.stepandemianenko.dsaprep.domain.model

data class ProblemSession(
    val id: Long,
    val problemTitle: String,
    val problemLink: String?,
    val difficulty: ProblemDifficulty,
    val topic: ProblemTopic,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long?,
    val durationSeconds: Long,
    val goalMinutes: Int,
    val solvedStatus: SolvedStatus,
    val timeComplexity: String?,
    val spaceComplexity: String?,
    val mainApproach: String?,
    val mistakeOrBlocker: String?,
    val confidence: Int,
    val reviewPattern: String?,
    val reviewKeyInsight: String?,
    val reviewMistake: String?,
    val reviewRewriteStatus: RewriteStatus?,
    val reviewFinalTakeaway: String?,
    val dateIso: String
)
