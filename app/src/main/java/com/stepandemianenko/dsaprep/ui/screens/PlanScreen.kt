package com.stepandemianenko.dsaprep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stepandemianenko.dsaprep.presentation.displayName
import com.stepandemianenko.dsaprep.ui.theme.dustGlow
import com.stepandemianenko.dsaprep.ui.theme.glassCardColors
import com.stepandemianenko.dsaprep.ui.theme.premiumBackgroundBrush
import com.stepandemianenko.dsaprep.viewmodel.PlanMode
import com.stepandemianenko.dsaprep.viewmodel.PlanUiState
import com.stepandemianenko.dsaprep.viewmodel.RoadmapItemUiModel
import com.stepandemianenko.dsaprep.viewmodel.RoadmapWeekUiModel

@Composable
fun PlanScreen(
    state: PlanUiState,
    onToggleItem: (String) -> Unit,
    onSelectMode: (PlanMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(premiumBackgroundBrush())
            .dustGlow(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { PlanHeader(state, onSelectMode) }
        state.weeks.forEach { week ->
            item(key = week.id) { WeekCard(week, onToggleItem) }
        }
    }
}

@Composable
private fun PlanHeader(state: PlanUiState, onSelectMode: (PlanMode) -> Unit) {
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
            Text("Plan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("DSA practice plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlanMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.mode == mode,
                        onClick = { onSelectMode(mode) },
                        label = { Text(mode.label) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
            Text(
                text = when (state.mode) {
                    PlanMode.ROADMAP -> "Work through a small set of patterns. Use Progress to choose what to redo."
                    PlanMode.BLIND75 -> "75 essential problems grouped by pattern. Ticked = already solved."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.totalCount > 0) {
                Text(
                    text = "${state.completedCount} / ${state.totalCount} done",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                LinearProgressIndicator(
                    progress = { state.completedCount.toFloat() / state.totalCount.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun WeekCard(
    week: RoadmapWeekUiModel,
    onToggleItem: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = glassCardColors(0.72f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(week.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(week.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${week.completedCount}/${week.totalCount}", color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = { week.progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondaryContainer
            )
            week.items.forEach { item ->
                RoadmapRow(item, onToggleItem)
            }
        }
    }
}

@Composable
private fun RoadmapRow(
    item: RoadmapItemUiModel,
    onToggleItem: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggleItem(item.id) }
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (item.advice.isBlank()) item.target else "${item.topic.displayName()} · ${item.target}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (item.advice.isNotBlank()) {
                Text(
                    text = item.advice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
