package com.stepandemianenko.dsaprep.data

import java.time.LocalDate

data class StudyTask(
    val id: Long,
    val title: String,
    val category: String,
    val date: LocalDate,
    val isCompleted: Boolean
)
