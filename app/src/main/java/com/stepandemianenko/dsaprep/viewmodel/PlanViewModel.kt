package com.stepandemianenko.dsaprep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stepandemianenko.dsaprep.data.RoadmapWeek
import com.stepandemianenko.dsaprep.data.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlanMode(val label: String) {
    ROADMAP("Roadmap"),
    BLIND75("Blind 75")
}

data class PlanUiState(
    val mode: PlanMode = PlanMode.ROADMAP,
    val weeks: List<RoadmapWeekUiModel> = emptyList()
) {
    val completedCount: Int get() = weeks.sumOf { it.completedCount }
    val totalCount: Int get() = weeks.sumOf { it.totalCount }
}

class PlanViewModel(
    private val repository: StudyRepository
) : ViewModel() {
    private var mode = PlanMode.ROADMAP
    // Namespaced item ids ("ROADMAP-w-i" / "BLIND75-w-i"); pre-seeded with `done` items.
    private val checkedItems = MutableStateFlow(seedDone())
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()

    fun selectMode(newMode: PlanMode) {
        if (mode == newMode) return
        mode = newMode
        _uiState.value = buildState()
    }

    fun toggleItem(itemId: String) {
        val current = checkedItems.value
        checkedItems.value = if (current.contains(itemId)) current - itemId else current + itemId
        _uiState.value = buildState()
    }

    private fun weeksFor(m: PlanMode): List<RoadmapWeek> = when (m) {
        PlanMode.ROADMAP -> repository.getRoadmap()
        PlanMode.BLIND75 -> repository.getBlind75()
    }

    private fun itemId(m: PlanMode, weekIndex: Int, itemIndex: Int) = "${m.name}-$weekIndex-$itemIndex"

    private fun seedDone(): Set<String> = buildSet {
        PlanMode.entries.forEach { m ->
            weeksFor(m).forEachIndexed { w, week ->
                week.items.forEachIndexed { i, item ->
                    if (item.done) add(itemId(m, w, i))
                }
            }
        }
    }

    private fun buildState(): PlanUiState {
        val checked = checkedItems.value
        val weeks = weeksFor(mode).mapIndexed { weekIndex, week ->
            val items = week.items.mapIndexed { itemIndex, item ->
                val id = itemId(mode, weekIndex, itemIndex)
                RoadmapItemUiModel(
                    id = id,
                    topic = item.topic,
                    title = item.title,
                    target = item.target,
                    advice = item.advice,
                    isChecked = checked.contains(id)
                )
            }
            RoadmapWeekUiModel(
                id = week.title,
                title = week.title,
                subtitle = week.subtitle,
                items = items,
                completedCount = items.count { it.isChecked }
            )
        }
        return PlanUiState(mode = mode, weeks = weeks)
    }

    class Factory(
        private val repository: StudyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlanViewModel::class.java)) {
                return PlanViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
