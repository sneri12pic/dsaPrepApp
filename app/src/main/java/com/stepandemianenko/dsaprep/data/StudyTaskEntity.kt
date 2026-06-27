package com.stepandemianenko.dsaprep.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val date: LocalDate,
    val isCompleted: Boolean = false
)

fun StudyTaskEntity.toStudyTask(): StudyTask = StudyTask(
    id = id,
    title = title,
    category = category,
    date = date,
    isCompleted = isCompleted
)
