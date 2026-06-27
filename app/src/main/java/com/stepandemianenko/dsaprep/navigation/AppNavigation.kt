package com.stepandemianenko.dsaprep.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stepandemianenko.dsaprep.StudyPlanApplication
import com.stepandemianenko.dsaprep.ui.screens.PlanScreen
import com.stepandemianenko.dsaprep.ui.screens.ProgressScreen
import com.stepandemianenko.dsaprep.ui.screens.StopwatchScreen
import com.stepandemianenko.dsaprep.ui.screens.TodayScreen
import com.stepandemianenko.dsaprep.viewmodel.PlanViewModel
import com.stepandemianenko.dsaprep.viewmodel.ProgressViewModel
import com.stepandemianenko.dsaprep.viewmodel.TodayViewModel

private sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
) {
    data object Today : AppDestination(
        route = "today",
        label = "Today",
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
    )

    data object Plan : AppDestination(
        route = "plan",
        label = "Plan",
        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
    )

    data object Stopwatch : AppDestination(
        route = "stopwatch",
        label = "Timer",
        icon = { Icon(Icons.Default.Timer, contentDescription = null) }
    )

    data object Progress : AppDestination(
        route = "progress",
        label = "Progress",
        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) }
    )
}

private val BottomDestinations = listOf(
    AppDestination.Today,
    AppDestination.Plan,
    AppDestination.Stopwatch,
    AppDestination.Progress
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val repository = (LocalContext.current.applicationContext as StudyPlanApplication).repository
    val todayViewModel: TodayViewModel = viewModel(
        factory = TodayViewModel.Factory(repository)
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                BottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = destination.icon,
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Today.route) {
                val state by todayViewModel.uiState.collectAsStateWithLifecycle()

                TodayScreen(
                    state = state,
                    onProblemTitleChanged = todayViewModel::updateProblemTitle,
                    onProblemSearchChanged = todayViewModel::updateProblemSearchQuery,
                    onKnownProblemSelected = todayViewModel::selectKnownProblem,
                    onProblemLinkChanged = todayViewModel::updateProblemLink,
                    onDifficultySelected = todayViewModel::selectDifficulty,
                    onTopicSelected = todayViewModel::selectTopic,
                    onGoalSelected = todayViewModel::selectGoalMinutes,
                    onStartSession = {
                        todayViewModel.startSession()
                        navController.navigate(AppDestination.Stopwatch.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTogglePause = todayViewModel::togglePause,
                    onFinishSession = todayViewModel::showFinishForm,
                    onSolvedStatusSelected = todayViewModel::selectSolvedStatus,
                    onTimeComplexityChanged = todayViewModel::updateTimeComplexity,
                    onSpaceComplexityChanged = todayViewModel::updateSpaceComplexity,
                    onMainApproachChanged = todayViewModel::updateMainApproach,
                    onMistakeOrBlockerChanged = todayViewModel::updateMistakeOrBlocker,
                    onConfidenceSelected = todayViewModel::selectConfidence,
                    onSaveFinish = todayViewModel::saveFinishForm,
                    onReviewPatternChanged = todayViewModel::updateReviewPattern,
                    onReviewKeyInsightChanged = todayViewModel::updateReviewKeyInsight,
                    onReviewMistakeChanged = todayViewModel::updateReviewMistake,
                    onRewriteStatusSelected = todayViewModel::selectRewriteStatus,
                    onReviewFinalTakeawayChanged = todayViewModel::updateReviewFinalTakeaway,
                    onSaveReview = todayViewModel::saveReview
                )
            }

            composable(AppDestination.Plan.route) {
                val viewModel: PlanViewModel = viewModel(
                    factory = PlanViewModel.Factory(repository)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                PlanScreen(
                    state = state,
                    onToggleItem = viewModel::toggleItem,
                    onSelectMode = viewModel::selectMode
                )
            }

            composable(AppDestination.Progress.route) {
                val viewModel: ProgressViewModel = viewModel(
                    factory = ProgressViewModel.Factory(repository)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                ProgressScreen(state = state)
            }

            composable(AppDestination.Stopwatch.route) {
                val state by todayViewModel.uiState.collectAsStateWithLifecycle()

                StopwatchScreen(
                    state = state,
                    onStartStandalone = todayViewModel::startStandaloneSession,
                    onTogglePause = todayViewModel::togglePause,
                    onResetTimer = todayViewModel::resetTimer,
                    onFinishSession = todayViewModel::showFinishForm,
                    onSolvedStatusSelected = todayViewModel::selectSolvedStatus,
                    onTimeComplexityChanged = todayViewModel::updateTimeComplexity,
                    onSpaceComplexityChanged = todayViewModel::updateSpaceComplexity,
                    onMainApproachChanged = todayViewModel::updateMainApproach,
                    onMistakeOrBlockerChanged = todayViewModel::updateMistakeOrBlocker,
                    onConfidenceSelected = todayViewModel::selectConfidence,
                    onSaveFinish = todayViewModel::saveFinishForm,
                    onReviewPatternChanged = todayViewModel::updateReviewPattern,
                    onReviewKeyInsightChanged = todayViewModel::updateReviewKeyInsight,
                    onReviewMistakeChanged = todayViewModel::updateReviewMistake,
                    onRewriteStatusSelected = todayViewModel::selectRewriteStatus,
                    onReviewFinalTakeawayChanged = todayViewModel::updateReviewFinalTakeaway,
                    onSaveReview = todayViewModel::saveReview
                )
            }
        }
    }
}
