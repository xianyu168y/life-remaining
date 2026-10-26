package com.liferemaining.app.domain.model

import java.time.LocalDate

data class LifeGoal(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val category: GoalCategory = GoalCategory.OTHER,
    val createdAt: LocalDate,
    val dueDate: LocalDate? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val isImportant: Boolean = false
) {
    val isCompleted: Boolean get() = status == GoalStatus.COMPLETED
}
