package com.liferemaining.app.domain.model

import java.time.LocalDate

data class Milestone(
    val id: Long = 0L,
    val title: String,
    val date: LocalDate,
    val category: MilestoneCategory = MilestoneCategory.OTHER,
    val note: String = "",
    val repeatsAnnually: Boolean = false
)
