package com.stepandemianenko.dsaprep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stepandemianenko.dsaprep.domain.logic.TopicStat
import com.stepandemianenko.dsaprep.presentation.displayName
import com.stepandemianenko.dsaprep.ui.theme.dustGlow
import com.stepandemianenko.dsaprep.ui.theme.glassCardColors
import com.stepandemianenko.dsaprep.ui.theme.premiumBackgroundBrush
import com.stepandemianenko.dsaprep.viewmodel.ProgressCalendarDay
import com.stepandemianenko.dsaprep.viewmodel.ProgressUiState
import com.stepandemianenko.dsaprep.viewmodel.SessionUiModel

@Composable
fun ProgressScreen(
    state: ProgressUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(premiumBackgroundBrush())
            .dustGlow(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = glassCardColors(0.72f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Momentum", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("Progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Glowing practice signals from your saved problem sessions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SummaryGrid(state) }
        item { TopicCard("Most practised topics", state.mostPractisedTopics, "No practice topics yet.") }
        item { TopicCard("Weak topics", state.weakTopics, "Weak topics appear after low-confidence or solution-led sessions.") }
        item { SessionCalendar(state) }
        item { Text("Recent sessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        if (state.recentSessions.isEmpty()) {
            item {
                Text(
                    "No saved sessions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(state.recentSessions, key = { it.id }) { session ->
                ProgressSessionRow(session)
            }
        }
    }
}

@Composable
private fun SummaryGrid(state: ProgressUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Current", "${state.currentStreak}d", "streak", Modifier.weight(1f))
            StatTile("Longest", "${state.longestStreak}d", "best run", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Days", state.totalPracticeDays.toString(), "practice days", Modifier.weight(1f))
            StatTile("Sessions", state.totalSessions.toString(), "total", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Solved", state.solvedMyselfCount.toString(), "by yourself", Modifier.weight(1f))
            StatTile("Solutions", state.usedSolutionCount.toString(), "used", Modifier.weight(1f))
        }
        StatTile("Average time", state.averageDurationText, "per session", Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = glassCardColors(0.72f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TopicCard(
    title: String,
    topics: List<TopicStat>,
    emptyText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = glassCardColors(0.72f)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (topics.isEmpty()) {
                Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                topics.forEach { topic ->
                    Text(
                        text = "${topic.topic.displayName()} · ${topic.count}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCalendar(state: ProgressUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = glassCardColors(0.72f)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(state.monthLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth()) {
                state.weekdayHeaders.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            state.monthDays.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { day ->
                        CalendarCell(day, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(day: ProgressCalendarDay, modifier: Modifier = Modifier) {
    if (day.isPlaceholder) {
        Box(modifier = modifier.aspectRatio(1f))
        return
    }

    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(14.dp),
        color = if (day.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        contentColor = if (day.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (day.isToday) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday || day.isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ProgressSessionRow(session: SessionUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = glassCardColors(0.72f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${session.difficulty.displayName()} · ${session.topic.displayName()} · ${session.durationText}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${session.status.displayName()} · confidence ${session.confidence}/5",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
