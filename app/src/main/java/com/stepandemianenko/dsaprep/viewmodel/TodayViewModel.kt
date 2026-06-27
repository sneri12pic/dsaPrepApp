package com.stepandemianenko.dsaprep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stepandemianenko.dsaprep.data.StudyRepository
import com.stepandemianenko.dsaprep.domain.logic.RestAdviceCalculator
import com.stepandemianenko.dsaprep.domain.model.KnownProblem
import com.stepandemianenko.dsaprep.domain.model.Neetcode150Catalog
import com.stepandemianenko.dsaprep.domain.model.ProblemDifficulty
import com.stepandemianenko.dsaprep.domain.model.ProblemSession
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class ActiveSession(
    val problemTitle: String,
    val problemLink: String?,
    val difficulty: ProblemDifficulty,
    val topic: ProblemTopic,
    val goalMinutes: Int,
    val startedAtEpochMillis: Long,
    val accumulatedSeconds: Long = 0,
    val lastResumedAtEpochMillis: Long = startedAtEpochMillis,
    val isPaused: Boolean = false
)

data class TodayUiState(
    val todayDateText: String = "",
    val sessionCount: Int = 0,
    val problemSearchQuery: String = "",
    val problemSearchResults: List<KnownProblem> = emptyList(),
    val problemTitle: String = "",
    val problemLink: String = "",
    val selectedDifficulty: ProblemDifficulty = ProblemDifficulty.MEDIUM,
    val selectedTopic: ProblemTopic = ProblemTopic.ARRAY,
    val selectedGoalMinutes: Int = 35,
    val isStartEnabled: Boolean = false,
    val activeTitle: String? = null,
    val activeTopic: ProblemTopic = ProblemTopic.ARRAY,
    val activeDifficulty: ProblemDifficulty = ProblemDifficulty.MEDIUM,
    val timerText: String = "00:00:00",
    val isTimerPaused: Boolean = false,
    val finishFormVisible: Boolean = false,
    val finishForm: FinishFormState = FinishFormState(),
    val reviewFormVisible: Boolean = false,
    val reviewForm: ReviewFormState = ReviewFormState(),
    val restAdvice: String? = null,
    val todaySessions: List<SessionUiModel> = emptyList()
)

class TodayViewModel(
    private val repository: StudyRepository
) : ViewModel() {
    private val today = LocalDate.now()
    private val todayIso = today.toString()
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")

    private var activeSession: ActiveSession? = null
    private var timerJob: Job? = null
    private var reviewSessionId: Long? = null

    private val _uiState = MutableStateFlow(
        TodayUiState(todayDateText = today.format(dateFormatter))
    )
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSessionsForDate(todayIso).collect { sessions ->
                _uiState.update {
                    it.copy(
                        sessionCount = sessions.size,
                        todaySessions = sessions.map { session -> session.toUiModel() }
                    )
                }
            }
        }
    }

    fun updateProblemTitle(value: String) {
        _uiState.update { it.copy(problemTitle = value, isStartEnabled = value.isNotBlank()) }
    }

    fun updateProblemSearchQuery(value: String) {
        _uiState.update {
            it.copy(
                problemSearchQuery = value,
                problemSearchResults = Neetcode150Catalog.search(value)
            )
        }
    }

    fun selectKnownProblem(problem: KnownProblem) {
        _uiState.update {
            it.copy(
                problemSearchQuery = problem.title,
                problemSearchResults = emptyList(),
                problemTitle = problem.title,
                problemLink = problem.leetcodeUrl,
                selectedDifficulty = problem.difficulty,
                selectedTopic = problem.topic,
                isStartEnabled = true
            )
        }
    }

    fun updateProblemLink(value: String) {
        _uiState.update { it.copy(problemLink = value) }
    }

    fun selectDifficulty(value: ProblemDifficulty) {
        _uiState.update { it.copy(selectedDifficulty = value) }
    }

    fun selectTopic(value: ProblemTopic) {
        _uiState.update { it.copy(selectedTopic = value) }
    }

    fun selectGoalMinutes(value: Int) {
        _uiState.update { it.copy(selectedGoalMinutes = value) }
    }

    fun startSession() {
        val state = uiState.value
        if (state.problemTitle.isBlank()) return

        val now = System.currentTimeMillis()
        activeSession = ActiveSession(
            problemTitle = state.problemTitle,
            problemLink = state.problemLink.takeIf { it.isNotBlank() },
            difficulty = state.selectedDifficulty,
            topic = state.selectedTopic,
            goalMinutes = state.selectedGoalMinutes,
            startedAtEpochMillis = now
        )
        _uiState.update {
            it.copy(
                problemTitle = "",
                problemLink = "",
                problemSearchQuery = "",
                problemSearchResults = emptyList(),
                isStartEnabled = false,
                activeTitle = state.problemTitle,
                activeTopic = state.selectedTopic,
                activeDifficulty = state.selectedDifficulty,
                timerText = "00:00:00",
                isTimerPaused = false,
                finishFormVisible = false,
                reviewFormVisible = false,
                restAdvice = null
            )
        }
        startTicker()
    }

    fun startStandaloneSession() {
        val state = uiState.value
        val now = System.currentTimeMillis()
        activeSession = ActiveSession(
            problemTitle = "Untitled practice session",
            problemLink = null,
            difficulty = state.selectedDifficulty,
            topic = state.selectedTopic,
            goalMinutes = state.selectedGoalMinutes,
            startedAtEpochMillis = now
        )
        _uiState.update {
            it.copy(
                problemTitle = "",
                problemLink = "",
                problemSearchQuery = "",
                problemSearchResults = emptyList(),
                isStartEnabled = false,
                activeTitle = "Untitled practice session",
                activeTopic = state.selectedTopic,
                activeDifficulty = state.selectedDifficulty,
                timerText = "00:00:00",
                isTimerPaused = false,
                finishFormVisible = false,
                reviewFormVisible = false,
                restAdvice = null
            )
        }
        startTicker()
    }

    fun togglePause() {
        val session = activeSession ?: return
        val now = System.currentTimeMillis()
        activeSession = if (session.isPaused) {
            session.copy(
                isPaused = false,
                lastResumedAtEpochMillis = now
            )
        } else {
            session.copy(
                isPaused = true,
                accumulatedSeconds = elapsedSeconds(session, now)
            )
        }
        _uiState.update { it.copy(isTimerPaused = activeSession?.isPaused == true) }
    }

    fun resetTimer() {
        activeSession = null
        timerJob?.cancel()
        reviewSessionId = null
        _uiState.update {
            it.copy(
                activeTitle = null,
                timerText = "00:00:00",
                isTimerPaused = false,
                finishFormVisible = false,
                finishForm = FinishFormState(),
                reviewFormVisible = false,
                reviewForm = ReviewFormState(),
                restAdvice = null
            )
        }
    }

    fun showFinishForm() {
        val session = activeSession ?: return
        val now = System.currentTimeMillis()
        activeSession = session.copy(
            isPaused = true,
            accumulatedSeconds = elapsedSeconds(session, now)
        )
        _uiState.update {
            it.copy(
                isTimerPaused = true,
                finishFormVisible = true,
                reviewFormVisible = false,
                restAdvice = null
            )
        }
    }

    fun selectSolvedStatus(value: SolvedStatus) {
        _uiState.update { it.copy(finishForm = it.finishForm.copy(solvedStatus = value)) }
    }

    fun updateTimeComplexity(value: String) {
        _uiState.update { it.copy(finishForm = it.finishForm.copy(timeComplexity = value)) }
    }

    fun updateSpaceComplexity(value: String) {
        _uiState.update { it.copy(finishForm = it.finishForm.copy(spaceComplexity = value)) }
    }

    fun updateMainApproach(value: String) {
        _uiState.update { it.copy(finishForm = it.finishForm.copy(mainApproach = value)) }
    }

    fun updateMistakeOrBlocker(value: String) {
        _uiState.update { it.copy(finishForm = it.finishForm.copy(mistakeOrBlocker = value)) }
    }

    fun selectConfidence(value: Int) {
        _uiState.update { it.copy(finishForm = it.finishForm.copy(confidence = value.coerceIn(1, 5))) }
    }

    fun saveFinishForm() {
        val session = activeSession ?: return
        val form = uiState.value.finishForm
        val finishedAt = System.currentTimeMillis()
        val duration = elapsedSeconds(session, finishedAt)
        viewModelScope.launch {
            val sessionId = repository.insertSession(
                problemTitle = session.problemTitle,
                problemLink = session.problemLink,
                difficulty = session.difficulty,
                topic = session.topic,
                startedAtEpochMillis = session.startedAtEpochMillis,
                finishedAtEpochMillis = finishedAt,
                durationSeconds = duration,
                goalMinutes = session.goalMinutes,
                solvedStatus = form.solvedStatus,
                timeComplexity = form.timeComplexity,
                spaceComplexity = form.spaceComplexity,
                mainApproach = form.mainApproach,
                mistakeOrBlocker = form.mistakeOrBlocker,
                confidence = form.confidence,
                dateIso = todayIso
            )
            activeSession = null
            timerJob?.cancel()
            reviewSessionId = sessionId
            _uiState.update {
                it.copy(
                    activeTitle = null,
                    finishFormVisible = false,
                    reviewFormVisible = true,
                    timerText = formatDuration(duration),
                    finishForm = FinishFormState(),
                    reviewForm = ReviewFormState(),
                    restAdvice = null
                )
            }
        }
    }

    fun updateReviewPattern(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(pattern = value)) }
    }

    fun updateReviewKeyInsight(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(keyInsight = value)) }
    }

    fun updateReviewMistake(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(mistake = value)) }
    }

    fun selectRewriteStatus(value: RewriteStatus) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(rewriteStatus = value)) }
    }

    fun updateReviewFinalTakeaway(value: String) {
        _uiState.update { it.copy(reviewForm = it.reviewForm.copy(finalTakeaway = value)) }
    }

    fun saveReview() {
        val sessionId = reviewSessionId ?: return
        val form = uiState.value.reviewForm
        viewModelScope.launch {
            repository.updateSessionReview(
                sessionId = sessionId,
                reviewPattern = form.pattern,
                reviewKeyInsight = form.keyInsight,
                reviewMistake = form.mistake,
                reviewRewriteStatus = form.rewriteStatus,
                reviewFinalTakeaway = form.finalTakeaway
            )
            val session = repository.getSessionById(sessionId)
            _uiState.update {
                it.copy(
                    reviewFormVisible = false,
                    restAdvice = session?.let(RestAdviceCalculator::adviceFor),
                    reviewForm = ReviewFormState()
                )
            }
            reviewSessionId = null
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val session = activeSession
                if (session != null) {
                    val seconds = elapsedSeconds(session, System.currentTimeMillis())
                    _uiState.update { it.copy(timerText = formatDuration(seconds)) }
                }
                delay(1_000)
            }
        }
    }

    private fun elapsedSeconds(session: ActiveSession, now: Long): Long {
        return if (session.isPaused) {
            session.accumulatedSeconds
        } else {
            session.accumulatedSeconds + ((now - session.lastResumedAtEpochMillis) / 1_000)
        }
    }

    class Factory(
        private val repository: StudyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
                return TodayViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

fun ProblemSession.toUiModel(): SessionUiModel = SessionUiModel(
    id = id,
    title = problemTitle,
    link = problemLink,
    difficulty = difficulty,
    topic = topic,
    status = solvedStatus,
    durationText = formatDuration(durationSeconds),
    confidence = confidence,
    dateIso = dateIso,
    hasReview = !reviewFinalTakeaway.isNullOrBlank() ||
        !reviewKeyInsight.isNullOrBlank() ||
        !reviewMistake.isNullOrBlank() ||
        !reviewPattern.isNullOrBlank()
)

fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
