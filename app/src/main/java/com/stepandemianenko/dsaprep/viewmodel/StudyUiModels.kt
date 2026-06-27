package com.stepandemianenko.dsaprep.viewmodel

import com.stepandemianenko.dsaprep.domain.model.ProblemDifficulty
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus

data class SessionUiModel(
    val id: Long,
    val title: String,
    val link: String?,
    val difficulty: ProblemDifficulty,
    val topic: ProblemTopic,
    val status: SolvedStatus,
    val durationText: String,
    val confidence: Int,
    val dateIso: String,
    val hasReview: Boolean
)

data class RoadmapItemUiModel(
    val id: String,
    val topic: ProblemTopic,
    val title: String,
    val target: String,
    val advice: String,
    val isChecked: Boolean
)

data class RoadmapWeekUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val items: List<RoadmapItemUiModel>,
    val completedCount: Int
) {
    val totalCount: Int get() = items.size
    val progress: Float get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
}

data class FinishFormState(
    val solvedStatus: SolvedStatus = SolvedStatus.SOLVED_MYSELF,
    val timeComplexity: String = "",
    val spaceComplexity: String = "",
    val mainApproach: String = "",
    val mistakeOrBlocker: String = "",
    val confidence: Int = 3
)

data class ReviewFormState(
    val pattern: String = "",
    val keyInsight: String = "",
    val mistake: String = "",
    val rewriteStatus: RewriteStatus = RewriteStatus.NOT_YET,
    val finalTakeaway: String = ""
)
