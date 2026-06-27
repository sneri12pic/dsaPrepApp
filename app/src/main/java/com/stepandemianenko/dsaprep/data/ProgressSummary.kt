package com.stepandemianenko.dsaprep.data

import java.time.LocalDate

data class ProgressSummary(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completedStudyDays: Int = 0,
    val weeklyCompletionPercentage: Int = 0,
    val completedDates: Set<LocalDate> = emptySet(),
    val recentCheckIns: List<DailyCheckIn> = emptyList(),
    val checkInsByDate: Map<LocalDate, DailyCheckIn> = emptyMap()
)
