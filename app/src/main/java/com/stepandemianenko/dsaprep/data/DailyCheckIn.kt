package com.stepandemianenko.dsaprep.data

import java.time.LocalDate

data class DailyCheckIn(
    val id: Long,
    val date: LocalDate,
    val energyLevel: String,
    val focusLevel: String,
    val note: String?,
    val completedAtLeastOneTask: Boolean
)
