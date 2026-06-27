package com.stepandemianenko.dsaprep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stepandemianenko.dsaprep.domain.model.KnownProblem
import com.stepandemianenko.dsaprep.domain.model.ProblemDifficulty
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus
import com.stepandemianenko.dsaprep.presentation.displayName
import com.stepandemianenko.dsaprep.ui.theme.dustGlow
import com.stepandemianenko.dsaprep.ui.theme.glassCardColors
import com.stepandemianenko.dsaprep.ui.theme.premiumBackgroundBrush
import com.stepandemianenko.dsaprep.viewmodel.FinishFormState
import com.stepandemianenko.dsaprep.viewmodel.ReviewFormState
import com.stepandemianenko.dsaprep.viewmodel.SessionUiModel
import com.stepandemianenko.dsaprep.viewmodel.TodayUiState

@Composable
fun TodayScreen(
    state: TodayUiState,
    onProblemTitleChanged: (String) -> Unit,
    onProblemSearchChanged: (String) -> Unit,
    onKnownProblemSelected: (KnownProblem) -> Unit,
    onProblemLinkChanged: (String) -> Unit,
    onDifficultySelected: (ProblemDifficulty) -> Unit,
    onTopicSelected: (ProblemTopic) -> Unit,
    onGoalSelected: (Int) -> Unit,
    onStartSession: () -> Unit,
    onTogglePause: () -> Unit,
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(premiumBackgroundBrush())
            .dustGlow(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { TodayHeader(state) }

        if (state.activeTitle == null && !state.finishFormVisible && !state.reviewFormVisible) {
            item {
                StartSessionCard(
                    state = state,
                    onProblemSearchChanged = onProblemSearchChanged,
                    onKnownProblemSelected = onKnownProblemSelected,
                    onProblemTitleChanged = onProblemTitleChanged,
                    onProblemLinkChanged = onProblemLinkChanged,
                    onDifficultySelected = onDifficultySelected,
                    onTopicSelected = onTopicSelected,
                    onGoalSelected = onGoalSelected,
                    onStartSession = onStartSession
                )
            }
        }

        if (state.activeTitle != null) {
            item {
                ActiveSessionCard(
                    state = state,
                    onTogglePause = onTogglePause,
                    onFinishSession = onFinishSession
                )
            }
        }

        if (state.finishFormVisible) {
            item {
                FinishSessionCard(
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
        }

        if (state.reviewFormVisible) {
            item {
                ReviewCard(
                    form = state.reviewForm,
                    onPatternChanged = onReviewPatternChanged,
                    onKeyInsightChanged = onReviewKeyInsightChanged,
                    onMistakeChanged = onReviewMistakeChanged,
                    onRewriteStatusSelected = onRewriteStatusSelected,
                    onFinalTakeawayChanged = onReviewFinalTakeawayChanged,
                    onSaveReview = onSaveReview
                )
            }
        }

        state.restAdvice?.let { advice ->
            item { RestAdviceCard(advice) }
        }

        item {
            SectionTitle("Today's sessions")
        }
        if (state.todaySessions.isEmpty()) {
            item {
                Text(
                    text = "No sessions yet. Start one focused problem and let the timer do the tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(state.todaySessions, key = { it.id }) { session ->
                SessionRow(session)
            }
        }
    }
}

@Composable
private fun TodayHeader(state: TodayUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = glassCardColors(0.72f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = state.todayDateText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Today's practice",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Start one problem, finish honestly, then capture the lesson while it is fresh.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${state.sessionCount} session${if (state.sessionCount == 1) "" else "s"} logged today",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StartSessionCard(
    state: TodayUiState,
    onProblemSearchChanged: (String) -> Unit,
    onKnownProblemSelected: (KnownProblem) -> Unit,
    onProblemTitleChanged: (String) -> Unit,
    onProblemLinkChanged: (String) -> Unit,
    onDifficultySelected: (ProblemDifficulty) -> Unit,
    onTopicSelected: (ProblemTopic) -> Unit,
    onGoalSelected: (Int) -> Unit,
    onStartSession: () -> Unit
) {
    CardContainer {
        Text("Start problem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = state.problemSearchQuery,
            onValueChange = onProblemSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search NeetCode 150") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        if (state.problemSearchResults.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.problemSearchResults.forEach { problem ->
                    KnownProblemRow(problem = problem, onClick = { onKnownProblemSelected(problem) })
                }
            }
        }
        OutlinedTextField(
            value = state.problemTitle,
            onValueChange = onProblemTitleChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Problem title") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = state.problemLink,
            onValueChange = onProblemLinkChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Optional LeetCode link") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        ChipGroup(
            title = "Difficulty",
            values = ProblemDifficulty.entries,
            selected = state.selectedDifficulty,
            label = { it.displayName() },
            onSelected = onDifficultySelected
        )
        ChipGroup(
            title = "Topic / pattern",
            values = ProblemTopic.entries,
            selected = state.selectedTopic,
            label = { it.displayName() },
            onSelected = onTopicSelected
        )
        ChipGroup(
            title = "Goal time",
            values = listOf(25, 35, 45, 60),
            selected = state.selectedGoalMinutes,
            label = { "$it min" },
            onSelected = onGoalSelected
        )
        Button(
            onClick = onStartSession,
            enabled = state.isStartEnabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Start timer")
        }
    }
}

@Composable
private fun KnownProblemRow(
    problem: KnownProblem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = glassCardColors(0.54f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = problem.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${problem.difficulty.displayName()} · ${problem.pattern}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveSessionCard(
    state: TodayUiState,
    onTogglePause: () -> Unit,
    onFinishSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = glassCardColors(0.74f),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Active session", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                text = state.activeTitle.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${state.activeDifficulty.displayName()} · ${state.activeTopic.displayName()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.timerText,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onTogglePause,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (state.isTimerPaused) "Resume" else "Pause")
                }
                Button(
                    onClick = onFinishSession,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Finish")
                }
            }
        }
    }
}

@Composable
private fun FinishSessionCard(
    form: FinishFormState,
    onSolvedStatusSelected: (SolvedStatus) -> Unit,
    onTimeComplexityChanged: (String) -> Unit,
    onSpaceComplexityChanged: (String) -> Unit,
    onMainApproachChanged: (String) -> Unit,
    onMistakeOrBlockerChanged: (String) -> Unit,
    onConfidenceSelected: (Int) -> Unit,
    onSaveFinish: () -> Unit
) {
    CardContainer {
        Text("Finish session", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ChipGroup("Result", SolvedStatus.entries, form.solvedStatus, { it.displayName() }, onSolvedStatusSelected)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = form.timeComplexity,
                onValueChange = onTimeComplexityChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Time") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = form.spaceComplexity,
                onValueChange = onSpaceComplexityChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Space") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }
        OutlinedTextField(
            value = form.mainApproach,
            onValueChange = onMainApproachChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Main approach") },
            minLines = 2,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = form.mistakeOrBlocker,
            onValueChange = onMistakeOrBlockerChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mistake or blocker") },
            minLines = 2,
            shape = RoundedCornerShape(16.dp)
        )
        ChipGroup("Confidence", (1..5).toList(), form.confidence, { it.toString() }, onConfidenceSelected)
        Button(onClick = onSaveFinish, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Text("Save result and review")
        }
    }
}

@Composable
private fun ReviewCard(
    form: ReviewFormState,
    onPatternChanged: (String) -> Unit,
    onKeyInsightChanged: (String) -> Unit,
    onMistakeChanged: (String) -> Unit,
    onRewriteStatusSelected: (RewriteStatus) -> Unit,
    onFinalTakeawayChanged: (String) -> Unit,
    onSaveReview: () -> Unit
) {
    CardContainer {
        Text("5-minute review", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(form.pattern, onPatternChanged, Modifier.fillMaxWidth(), label = { Text("Pattern") }, shape = RoundedCornerShape(16.dp))
        OutlinedTextField(form.keyInsight, onKeyInsightChanged, Modifier.fillMaxWidth(), label = { Text("Key insight") }, minLines = 2, shape = RoundedCornerShape(16.dp))
        OutlinedTextField(form.mistake, onMistakeChanged, Modifier.fillMaxWidth(), label = { Text("Mistake") }, minLines = 2, shape = RoundedCornerShape(16.dp))
        ChipGroup("Rewrite from memory", RewriteStatus.entries, form.rewriteStatus, { it.displayName() }, onRewriteStatusSelected)
        OutlinedTextField(form.finalTakeaway, onFinalTakeawayChanged, Modifier.fillMaxWidth(), label = { Text("Final takeaway") }, minLines = 2, shape = RoundedCornerShape(16.dp))
        Button(onClick = onSaveReview, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Text("Save review")
        }
    }
}

@Composable
private fun RestAdviceCard(advice: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.88f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Next step", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(advice, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SessionRow(session: SessionUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = glassCardColors(0.68f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${session.difficulty.displayName()} · ${session.topic.displayName()} · ${session.durationText}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${session.status.displayName()} · confidence ${session.confidence}/5" +
                    if (session.hasReview) " · reviewed" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun CardContainer(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = glassCardColors(0.72f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
