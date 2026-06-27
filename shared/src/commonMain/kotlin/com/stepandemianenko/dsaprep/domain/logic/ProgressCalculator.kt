package com.stepandemianenko.dsaprep.domain.logic

import com.stepandemianenko.dsaprep.domain.model.ProblemSession
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

data class TopicStat(
    val topic: ProblemTopic,
    val count: Int
)

data class SessionProgressSummary(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalPracticeDays: Int = 0,
    val totalSessions: Int = 0,
    val solvedMyselfCount: Int = 0,
    val usedSolutionCount: Int = 0,
    val averageDurationSeconds: Long = 0,
    val mostPractisedTopics: List<TopicStat> = emptyList(),
    val weakTopics: List<TopicStat> = emptyList(),
    val activeDates: Set<String> = emptySet()
)

object ProgressCalculator {
    fun calculate(
        sessions: List<ProblemSession>,
        todayIso: String
    ): SessionProgressSummary {
        if (sessions.isEmpty()) return SessionProgressSummary()

        val activeDates = sessions.map { it.dateIso }.toSet()
        val topicCounts = sessions.groupingBy { it.topic }.eachCount()
        val weakTopicCounts = sessions
            .filter { it.confidence <= 2 || it.solvedStatus == SolvedStatus.USED_SOLUTION || it.solvedStatus == SolvedStatus.COULD_NOT_SOLVE }
            .groupingBy { it.topic }
            .eachCount()

        return SessionProgressSummary(
            currentStreak = currentStreak(activeDates, todayIso),
            longestStreak = longestStreak(activeDates),
            totalPracticeDays = activeDates.size,
            totalSessions = sessions.size,
            solvedMyselfCount = sessions.count { it.solvedStatus == SolvedStatus.SOLVED_MYSELF },
            usedSolutionCount = sessions.count { it.solvedStatus == SolvedStatus.USED_SOLUTION },
            averageDurationSeconds = sessions.map { it.durationSeconds }.average().toLong(),
            mostPractisedTopics = topicCounts.toStats(),
            weakTopics = weakTopicCounts.toStats(),
            activeDates = activeDates
        )
    }

    private fun Map<ProblemTopic, Int>.toStats(): List<TopicStat> {
        return entries
            .sortedWith(compareByDescending<Map.Entry<ProblemTopic, Int>> { it.value }.thenBy { it.key.name })
            .take(3)
            .map { TopicStat(topic = it.key, count = it.value) }
    }

    private fun currentStreak(activeDates: Set<String>, todayIso: String): Int {
        val sorted = activeDates.sorted()
        if (sorted.isEmpty()) return 0
        val todayIndex = sorted.indexOf(todayIso)
        val startDate = when {
            todayIndex >= 0 -> todayIso
            sorted.lastOrNull() == previousIsoDate(todayIso) -> previousIsoDate(todayIso)
            else -> return 0
        }

        var streak = 0
        var date = startDate
        while (activeDates.contains(date)) {
            streak++
            date = previousIsoDate(date)
        }
        return streak
    }

    private fun longestStreak(activeDates: Set<String>): Int {
        val sortedDates = activeDates.sorted()
        if (sortedDates.isEmpty()) return 0

        var longest = 1
        var current = 1
        for (index in 1 until sortedDates.size) {
            if (previousIsoDate(sortedDates[index]) == sortedDates[index - 1]) {
                current++
            } else {
                longest = maxOf(longest, current)
                current = 1
            }
        }
        return maxOf(longest, current)
    }

    private fun previousIsoDate(dateIso: String): String {
        val year = dateIso.substring(0, 4).toInt()
        val month = dateIso.substring(5, 7).toInt()
        val day = dateIso.substring(8, 10).toInt()
        val previous = LocalDate(year, month, day).minus(DatePeriod(days = 1))
        return previous.toString()
    }
}
