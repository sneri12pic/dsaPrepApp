package com.stepandemianenko.dsaprep.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "daily_check_ins",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val energyLevel: String,
    val focusLevel: String,
    val note: String?,
    val completedAtLeastOneTask: Boolean
)

fun DailyCheckInEntity.toDailyCheckIn(): DailyCheckIn = DailyCheckIn(
    id = id,
    date = date,
    energyLevel = energyLevel,
    focusLevel = focusLevel,
    note = note,
    completedAtLeastOneTask = completedAtLeastOneTask
)
