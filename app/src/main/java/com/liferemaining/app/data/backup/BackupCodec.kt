package com.liferemaining.app.data.backup

import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.DailyReflection
import com.liferemaining.app.domain.model.GoalCategory
import com.liferemaining.app.domain.model.GoalPriority
import com.liferemaining.app.domain.model.GoalStatus
import com.liferemaining.app.domain.model.LifeGoal
import com.liferemaining.app.domain.model.Milestone
import com.liferemaining.app.domain.model.MilestoneCategory
import com.liferemaining.app.domain.model.ReminderTone
import com.liferemaining.app.domain.model.ThemeMode
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.domain.model.YearNote
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** 一份完整的本地数据快照，用于导出与导入。 */
data class BackupSnapshot(
    val profile: UserProfile?,
    val goals: List<LifeGoal>,
    val milestones: List<Milestone>,
    val reflections: List<DailyReflection>,
    val yearNotes: List<YearNote>
)

sealed interface DecodeResult {
    data class Success(val snapshot: BackupSnapshot) : DecodeResult
    data class Failure(val reason: String) : DecodeResult
}

/**
 * JSON 编解码器。纯 Kotlin，不碰数据库，便于单元测试。
 *
 * 校验顺序固定：JSON 结构 → app 标识 → 版本 → 每条记录的字段类型与取值范围。
 * 任一步失败都返回 Failure，不产生半份快照。
 */
object BackupCodec {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    private val iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun encode(snapshot: BackupSnapshot, exportedAt: LocalDateTime = LocalDateTime.now()): String {
        val file = BackupFile(
            exportedAt = exportedAt.format(iso),
            profile = snapshot.profile?.let { profile ->
                BackupProfile(
                    birthDate = profile.birthDate?.toString(),
                    expectedLifespanYears = profile.expectedLifespanYears,
                    countdownEnabled = profile.countdownEnabled,
                    showExactDays = profile.showExactDays,
                    themeMode = profile.themeMode.name,
                    accentKey = profile.accentKey.storageKey,
                    dynamicColor = profile.dynamicColor,
                    dailyReminderEnabled = profile.dailyReminderEnabled,
                    reminderTime = profile.reminderTime.toString(),
                    reminderTone = profile.reminderTone.name,
                    onboardingCompleted = profile.onboardingCompleted
                )
            },
            goals = snapshot.goals.map {
                BackupGoal(
                    title = it.title,
                    description = it.description,
                    category = it.category.name,
                    createdAt = it.createdAt.toString(),
                    dueDate = it.dueDate?.toString(),
                    status = it.status.name,
                    priority = it.priority.name,
                    isImportant = it.isImportant
                )
            },
            milestones = snapshot.milestones.map {
                BackupMilestone(
                    title = it.title,
                    date = it.date.toString(),
                    category = it.category.name,
                    note = it.note,
                    repeatsAnnually = it.repeatsAnnually
                )
            },
            reflections = snapshot.reflections.map {
                BackupReflection(
                    date = it.date.toString(),
                    content = it.content,
                    createdAt = it.createdAt.format(iso),
                    updatedAt = it.updatedAt.format(iso)
                )
            },
            yearNotes = snapshot.yearNotes.map {
                BackupYearNote(
                    year = it.year,
                    content = it.content,
                    createdAt = it.createdAt.format(iso),
                    updatedAt = it.updatedAt.format(iso)
                )
            }
        )
        return json.encodeToString(BackupFile.serializer(), file)
    }

    fun decode(raw: String): DecodeResult {
        if (raw.isBlank()) return DecodeResult.Failure("文件为空")
        val file = try {
            json.decodeFromString(BackupFile.serializer(), raw)
        } catch (e: Exception) {
            return DecodeResult.Failure("文件不是有效的备份（JSON 结构不匹配）")
        }

        if (file.app != BackupFile.APP_ID) {
            return DecodeResult.Failure("备份来源不匹配：期望 ${BackupFile.APP_ID}")
        }
        if (file.version != BackupFile.CURRENT_VERSION) {
            return DecodeResult.Failure(
                "不支持的备份版本 ${file.version}，当前支持 ${BackupFile.CURRENT_VERSION}"
            )
        }

        val profile = try {
            file.profile?.let(::toProfile)
        } catch (e: Exception) {
            return DecodeResult.Failure("个人参数无效：${e.message ?: "字段格式错误"}")
        }

        val goals = try {
            file.goals.map(::toGoal)
        } catch (e: Exception) {
            return DecodeResult.Failure("目标数据无效：${e.message ?: "字段格式错误"}")
        }

        val milestones = try {
            file.milestones.map(::toMilestone)
        } catch (e: Exception) {
            return DecodeResult.Failure("重要日期数据无效：${e.message ?: "字段格式错误"}")
        }

        val reflections = try {
            file.reflections.map(::toReflection)
        } catch (e: Exception) {
            return DecodeResult.Failure("每日记录无效：${e.message ?: "字段格式错误"}")
        }

        val notes = try {
            file.yearNotes.map(::toYearNote)
        } catch (e: Exception) {
            return DecodeResult.Failure("年度回忆无效：${e.message ?: "字段格式错误"}")
        }

        return DecodeResult.Success(
            BackupSnapshot(
                profile = profile,
                goals = goals,
                milestones = milestones,
                reflections = reflections,
                yearNotes = notes
            )
        )
    }

    private fun toProfile(dto: BackupProfile): UserProfile {
        val birth = dto.birthDate?.let { LocalDate.parse(it) }
        val lifespan = dto.expectedLifespanYears
        if (lifespan != null && lifespan !in 1..120) {
            throw IllegalArgumentException("预期寿命需在 1..120 之间")
        }
        val time = runCatching { LocalTime.parse(dto.reminderTime) }
            .getOrElse { throw IllegalArgumentException("提醒时间格式应为 HH:mm") }
        return UserProfile(
            birthDate = birth,
            expectedLifespanYears = lifespan,
            countdownEnabled = dto.countdownEnabled,
            showExactDays = dto.showExactDays,
            themeMode = ThemeMode.fromStorage(dto.themeMode),
            accentKey = AccentKey.fromStorage(dto.accentKey),
            dynamicColor = dto.dynamicColor,
            dailyReminderEnabled = dto.dailyReminderEnabled,
            reminderTime = time,
            reminderTone = ReminderTone.entries.firstOrNull { it.name == dto.reminderTone }
                ?: ReminderTone.GENTLE,
            onboardingCompleted = dto.onboardingCompleted
        )
    }

    private fun toGoal(dto: BackupGoal): LifeGoal {
        require(dto.title.isNotBlank()) { "目标标题不能为空" }
        return LifeGoal(
            title = dto.title,
            description = dto.description,
            category = GoalCategory.fromStorage(dto.category),
            createdAt = LocalDate.parse(dto.createdAt),
            dueDate = dto.dueDate?.let { LocalDate.parse(it) },
            status = GoalStatus.fromStorage(dto.status),
            priority = GoalPriority.fromStorage(dto.priority),
            isImportant = dto.isImportant
        )
    }

    private fun toMilestone(dto: BackupMilestone): Milestone {
        require(dto.title.isNotBlank()) { "重要日期标题不能为空" }
        return Milestone(
            title = dto.title,
            date = LocalDate.parse(dto.date),
            category = MilestoneCategory.fromStorage(dto.category),
            note = dto.note,
            repeatsAnnually = dto.repeatsAnnually
        )
    }

    private fun toReflection(dto: BackupReflection): DailyReflection {
        require(dto.content.isNotBlank()) { "每日记录内容不能为空" }
        return DailyReflection(
            date = LocalDate.parse(dto.date),
            content = dto.content,
            createdAt = LocalDateTime.parse(dto.createdAt, iso),
            updatedAt = LocalDateTime.parse(dto.updatedAt, iso)
        )
    }

    private fun toYearNote(dto: BackupYearNote): YearNote {
        require(dto.year in 1900..2200) { "年度回忆的年份超出范围" }
        require(dto.content.isNotBlank()) { "年度回忆内容不能为空" }
        return YearNote(
            year = dto.year,
            content = dto.content,
            createdAt = LocalDateTime.parse(dto.createdAt, iso),
            updatedAt = LocalDateTime.parse(dto.updatedAt, iso)
        )
    }
}
