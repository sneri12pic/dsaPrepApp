package com.stepandemianenko.dsaprep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus
import com.stepandemianenko.dsaprep.presentation.displayName
import com.stepandemianenko.dsaprep.ui.theme.dustGlow
import com.stepandemianenko.dsaprep.ui.theme.glassCardColors
import com.stepandemianenko.dsaprep.ui.theme.premiumBackgroundBrush
import com.stepandemianenko.dsaprep.viewmodel.FinishFormState
import com.stepandemianenko.dsaprep.viewmodel.ReviewFormState
import com.stepandemianenko.dsaprep.viewmodel.TodayUiState

@Composable
fun StopwatchScreen(
    state: TodayUiState,
    onStartStandalone: () -> Unit,
    onTogglePause: () -> Unit,
    onResetTimer: () -> Unit,
    onFinishSession: () -> Unit,
    onSolvedStatusSelected: (SolvedStatus) -> Unit,
    onTimeComplexityChanged: (String) -> Unit,
    onSpaceComplexityChanged: (String) -> Unit,
    onMainApproachChanged: (String) -> Unit,
    onMistakeOrBlockerChanged: (String) -> Unit,
    onConfidenceSelected: (Int) -> Unit,
    onSaveFinish: () -> Unit,
    onReviewPatternChanged: (String) -> Unit,
    onReviewKeyInsightChanged: (String) -> Unit,
    onReviewMistakeChanged: (String) -> Unit,
    onRewriteStatusSelected: (RewriteStatus) -> Unit,
    onReviewFinalTakeawayChanged: (String) -> Unit,
    onSaveReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(premiumBackgroundBrush())
            .dustGlow()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StopwatchHeader(state)
            Spacer(Modifier.height(82.dp))
            Text(
                text = state.timerText,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 50.sp,
                lineHeight = 54.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = state.activeTitle ?: "Ready when you are",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 14.dp)
            )
            if (state.activeTitle != null) {
                Text(
                    text = "${state.activeDifficulty.displayName()} - ${state.activeTopic.displayName()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(92.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAction(
                    label = if (state.activeTitle == null) "Idle" else "Finish",
                    enabled = state.activeTitle != null,
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    content = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onFinishSession,
                    icon = Icons.Filled.Flag
                )
                CircleAction(
                    label = when {
                        state.activeTitle == null -> "Start"
                        state.isTimerPaused -> "Resume"
                        else -> "Pause"
                    },
                    enabled = true,
                    container = MaterialTheme.colorScheme.primary,
                    content = MaterialTheme.colorScheme.onPrimary,
                    onClick = {
                        if (state.activeTitle == null) onStartStandalone() else onTogglePause()
                    },
                    icon = if (state.activeTitle == null || state.isTimerPaused) {
                        Icons.Filled.PlayArrow
                    } else {
                        Icons.Filled.Pause
                    }
                )
            }

            Button(
                onClick = onResetTimer,
                enabled = state.activeTitle != null || state.finishFormVisible || state.reviewFormVisible || state.timerText != "00:00:00",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = null)
                Text(
                    text = "Reset timer",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(34.dp))

            if (state.finishFormVisible) {
                StopwatchFinishCard(
                    form = state.finishForm,
                    onSolvedStatusSelected = onSolvedStatusSelected,
                    onTimeComplexityChanged = onTimeComplexityChanged,
                    onSpaceComplexityChanged = onSpaceComplexityChanged,
                    onMainApproachChanged = onMainApproachChanged,
                    onMistakeOrBlockerChanged = onMistakeOrBlockerChanged,
                    onConfidenceSelected = onConfidenceSelected,
                    onSaveFinish = onSaveFinish
                )
            }

            if (state.reviewFormVisible) {
                StopwatchReviewCard(
                    form = state.reviewForm,
                    onPatternChanged = onReviewPatternChanged,
                    onKeyInsightChanged = onReviewKeyInsightChanged,
                    onMistakeChanged = onReviewMistakeChanged,
                    onRewriteStatusSelected = onRewriteStatusSelected,
                    onFinalTakeawayChanged = onReviewFinalTakeawayChanged,
                    onSaveReview = onSaveReview
                )
            }

            state.restAdvice?.let { advice ->
                AdvicePanel(advice = advice)
            }
        }
    }
}

@Composable
private fun StopwatchHeader(state: TodayUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Stopwatch",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (state.activeTitle == null) {
                    "Use it with any practice session"
                } else if (state.isTimerPaused) {
                    "Paused"
                } else {
                    "Focused session running"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "DSA",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CircleAction(
    label: String,
    enabled: Boolean,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(128.dp)
            .shadow(16.dp, CircleShape),
        shape = CircleShape,
        color = if (enabled) container else container.copy(alpha = 0.45f),
        contentColor = if (enabled) content else content.copy(alpha = 0.38f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun StopwatchFinishCard(
    form: FinishFormState,
    onSolvedStatusSelected: (SolvedStatus) -> Unit,
    onTimeComplexityChanged: (String) -> Unit,
    onSpaceComplexityChanged: (String) -> Unit,
    onMainApproachChanged: (String) -> Unit,
    onMistakeOrBlockerChanged: (String) -> Unit,
    onConfidenceSelected: (Int) -> Unit,
    onSaveFinish: () -> Unit
) {
    PremiumPanel {
        Text("Finish session", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ChipGroup("Result", SolvedStatus.entries, form.solvedStatus, { it.displayName() }, onSolvedStatusSelected)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(form.timeComplexity, onTimeComplexityChanged, Modifier.weight(1f), label = { Text("Time") }, singleLine = true)
            OutlinedTextField(form.spaceComplexity, onSpaceComplexityChanged, Modifier.weight(1f), label = { Text("Space") }, singleLine = true)
        }
        OutlinedTextField(form.mainApproach, onMainApproachChanged, Modifier.fillMaxWidth(), label = { Text("Main approach") }, minLines = 2)
        OutlinedTextField(form.mistakeOrBlocker, onMistakeOrBlockerChanged, Modifier.fillMaxWidth(), label = { Text("Mistake or blocker") }, minLines = 2)
        ChipGroup("Confidence", (1..5).toList(), form.confidence, { it.toString() }, onConfidenceSelected)
        Button(onClick = onSaveFinish, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Text("Save result")
        }
    }
}

@Composable
private fun StopwatchReviewCard(
    form: ReviewFormState,
    onPatternChanged: (String) -> Unit,
    onKeyInsightChanged: (String) -> Unit,
    onMistakeChanged: (String) -> Unit,
    onRewriteStatusSelected: (RewriteStatus) -> Unit,
    onFinalTakeawayChanged: (String) -> Unit,
    onSaveReview: () -> Unit
) {
    PremiumPanel {
        Text("5-minute review", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(form.pattern, onPatternChanged, Modifier.fillMaxWidth(), label = { Text("Pattern") })
        OutlinedTextField(form.keyInsight, onKeyInsightChanged, Modifier.fillMaxWidth(), label = { Text("Key insight") }, minLines = 2)
        OutlinedTextField(form.mistake, onMistakeChanged, Modifier.fillMaxWidth(), label = { Text("Mistake") }, minLines = 2)
        ChipGroup("Rewrite from memory", RewriteStatus.entries, form.rewriteStatus, { it.displayName() }, onRewriteStatusSelected)
        OutlinedTextField(form.finalTakeaway, onFinalTakeawayChanged, Modifier.fillMaxWidth(), label = { Text("Final takeaway") }, minLines = 2)
        Button(onClick = onSaveReview, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Text("Save review")
        }
    }
}

@Composable
private fun AdvicePanel(advice: String) {
    PremiumPanel {
        Text("Next step", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(advice, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PremiumPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        shape = RoundedCornerShape(26.dp),
        colors = glassCardColors(0.78f),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun <T> ChipGroup(
    title: String,
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            values.forEach { value ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelected(value) },
                    label = { Text(label(value)) },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}
