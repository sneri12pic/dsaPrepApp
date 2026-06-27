package com.stepandemianenko.dsaprep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stepandemianenko.dsaprep.data.StudyRepository
import com.stepandemianenko.dsaprep.domain.logic.SessionProgressSummary
import com.stepandemianenko.dsaprep.domain.logic.TopicStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class ProgressCalendarDay(
    val dateIso: String?,
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isActive: Boolean
) {
    val isPlaceholder: Boolean get() = dateIso == null
}

data class ProgressUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalPracticeDays: Int = 0,
    val totalSessions: Int = 0,
    val solvedMyselfCount: Int = 0,
    val usedSolutionCount: Int = 0,
    val averageDurationText: String = "00:00",
    val mostPractisedTopics: List<TopicStat> = emptyList(),
    val weakTopics: List<TopicStat> = emptyList(),
    val recentSessions: List<SessionUiModel> = emptyList(),
    val monthLabel: String = "",
    val weekdayHeaders: List<String> = emptyList(),
    val monthDays: List<ProgressCalendarDay> = emptyList(),
    val isLoading: Boolean = true
)

class ProgressViewModel(
    private val repository: StudyRepository
) : ViewModel() {
    private val today = LocalDate.now()
    private val todayIso = today.toString()
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getProgressSummary(todayIso),
                repository.getRecentSessions(8)
            ) { summary, recent ->
                summary to recent
            }.collect { (summary, recent) ->
                _uiState.update {
                    it.copy(
                        currentStreak = summary.currentStreak,
                        longestStreak = summary.longestStreak,
                        totalPracticeDays = summary.totalPracticeDays,
                        totalSessions = summary.totalSessions,
                        solvedMyselfCount = summary.solvedMyselfCount,
                        usedSolutionCount = summary.usedSolutionCount,
                        averageDurationText = formatDuration(summary.averageDurationSeconds),
                        mostPractisedTopics = summary.mostPractisedTopics,
                        weakTopics = summary.weakTopics,
                        recentSessions = recent.map { session -> session.toUiModel() },
                        monthLabel = today.format(monthFormatter),
                        weekdayHeaders = buildWeekdayHeaders(),
                        monthDays = buildMonthDays(summary),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun buildWeekdayHeaders(): List<String> {
        return (0L..6L).map { offset ->
            java.time.DayOfWeek.MONDAY
                .plus(offset)
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }

    private fun buildMonthDays(summary: SessionProgressSummary): List<ProgressCalendarDay> {
        val yearMonth = YearMonth.from(today)
        val firstOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val cells = mutableListOf<ProgressCalendarDay>()

        repeat(firstOfMonth.dayOfWeek.value - 1) {
            cells.add(ProgressCalendarDay(null, 0, isToday = false, isActive = false))
        }
        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val iso = date.toString()
            cells.add(
                ProgressCalendarDay(
                    dateIso = iso,
                    dayOfMonth = day,
                    isToday = date == today,
                    isActive = summary.activeDates.contains(iso)
                )
            )
        }
        while (cells.size % 7 != 0) {
            cells.add(ProgressCalendarDay(null, 0, isToday = false, isActive = false))
        }
        return cells
    }

    class Factory(
        private val repository: StudyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                return ProgressViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
