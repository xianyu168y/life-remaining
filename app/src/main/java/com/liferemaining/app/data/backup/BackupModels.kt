package com.liferemaining.app.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 导出/导入的 JSON 结构。字段全部使用字符串日期，跨版本稳定。
 * version 用于导入时判断兼容性；不兼容直接拒绝，不覆盖现有数据。
 */
@Serializable
data class BackupFile(
    val version: Int,
    @SerialName("app") val app: String,
    val exportedAt: String,
    val profile: BackupProfile? = null,
    val goals: List<BackupGoal> = emptyList(),
    val milestones: List<BackupMilestone> = emptyList(),
    val reflections: List<BackupReflection> = emptyList(),
    val yearNotes: List<BackupYearNote> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
        const val APP_ID = "com.liferemaining.app"
    }
}

@Serializable
data class BackupProfile(
    val birthDate: String? = null,
    val expectedLifespanYears: Int? = null,
    val countdownEnabled: Boolean = true,
    val showExactDays: Boolean = true,
    val themeMode: String = "SYSTEM",
    val accentKey: String = "teal",
    val dynamicColor: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val reminderTime: String = "08:30",
    val reminderTone: String = "GENTLE",
    val onboardingCompleted: Boolean = true
)

@Serializable
data class BackupGoal(
    val title: String,
    val description: String = "",
    val category: String = "OTHER",
    val createdAt: String,
    val dueDate: String? = null,
    val status: String = "ACTIVE",
    val priority: String = "MEDIUM",
    val isImportant: Boolean = false
)

@Serializable
data class BackupMilestone(
    val title: String,
    val date: String,
    val category: String = "OTHER",
    val note: String = "",
    val repeatsAnnually: Boolean = false
)

@Serializable
data class BackupReflection(
    val date: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BackupYearNote(
    val year: Int,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)

sealed interface ImportResult {
    data class Success(
        val goals: Int,
        val milestones: Int,
        val reflections: Int,
        val yearNotes: Int
    ) : ImportResult

    data class Failure(val reason: String) : ImportResult
}
