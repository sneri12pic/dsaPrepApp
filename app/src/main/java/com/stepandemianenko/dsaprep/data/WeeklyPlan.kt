package com.stepandemianenko.dsaprep.data

/**
 * Static, in-code domain model for the weekly roadmap timeline.
 *
 * The weekly plan is intentionally NOT persisted: it is a fixed study roadmap
 * authored in [StudyRepository.getRoadmap]. Only the user's checked/expanded
 * state is tracked (in-memory, in the ViewModel).
 */
data class PlanWeek(
    val id: String,
    val title: String,
    val subtitle: String,
    val days: List<PlanDay>
)

/**
 * A single day row in a week. Each day is a checkable task with a short [topic]
 * headline and a one-line [detail]. [dayLabel] is the short weekday (e.g. "Mon").
 */
data class PlanDay(
    val id: String,
    val dayLabel: String,
    val topic: String,
    val detail: String
)
