package com.liferemaining.app.backup

import com.liferemaining.app.data.backup.BackupCodec
import com.liferemaining.app.data.backup.BackupSnapshot
import com.liferemaining.app.data.backup.DecodeResult
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BackupCodecTest {

    private val profile = UserProfile(
        birthDate = LocalDate.of(1990, 5, 15),
        expectedLifespanYears = 82,
        countdownEnabled = true,
        showExactDays = false,
        themeMode = ThemeMode.DARK,
        accentKey = AccentKey.BLUE,
        dynamicColor = true,
        dailyReminderEnabled = true,
        reminderTime = LocalTime.of(7, 45),
        reminderTone = ReminderTone.DIRECT,
        onboardingCompleted = true
    )

    private val goal = LifeGoal(
        title = "写完一本书",
        description = "哪怕只有三万字",
        category = GoalCategory.CAREER,
        createdAt = LocalDate.of(2026, 1, 2),
        dueDate = LocalDate.of(2027, 1, 2),
        status = GoalStatus.ACTIVE,
        priority = GoalPriority.HIGH,
        isImportant = true
    )

    private val milestone = Milestone(
        title = "生日",
        date = LocalDate.of(1990, 12, 1),
        category = MilestoneCategory.BIRTHDAY,
        note = "和家人吃饭",
        repeatsAnnually = true
    )

    private val reflection = DailyReflection(
        date = LocalDate.of(2026, 10, 26),
        content = "今天的风很轻。",
        createdAt = LocalDateTime.of(2026, 10, 26, 22, 10),
        updatedAt = LocalDateTime.of(2026, 10, 26, 22, 12)
    )

    private val note = YearNote(
        year = 2026,
        content = "换了城市。",
        createdAt = LocalDateTime.of(2026, 12, 31, 23, 0),
        updatedAt = LocalDateTime.of(2026, 12, 31, 23, 0)
    )

    private fun snapshot() = BackupSnapshot(
        profile = profile,
        goals = listOf(goal),
        milestones = listOf(milestone),
        reflections = listOf(reflection),
        yearNotes = listOf(note)
    )

    @Test
    fun `round trip preserves every field`() {
        val encoded = BackupCodec.encode(
            snapshot(),
            exportedAt = LocalDateTime.of(2026, 10, 26, 12, 0)
        )
        val decoded = BackupCodec.decode(encoded)
        assertTrue(decoded is DecodeResult.Success)
        val restored = (decoded as DecodeResult.Success).snapshot

        assertEquals(profile, restored.profile)
        assertEquals(1, restored.goals.size)
        assertEquals(goal.title, restored.goals[0].title)
        assertEquals(goal.dueDate, restored.goals[0].dueDate)
        assertEquals(goal.priority, restored.goals[0].priority)
        assertEquals(goal.isImportant, restored.goals[0].isImportant)

        assertEquals(milestone, restored.milestones[0])
        assertEquals(reflection, restored.reflections[0])
        assertEquals(note, restored.yearNotes[0])
    }

    @Test
    fun `rejects invalid json`() {
        val result = BackupCodec.decode("{not json")
        assertTrue(result is DecodeResult.Failure)
        assertTrue((result as DecodeResult.Failure).reason.contains("JSON"))
    }

    @Test
    fun `rejects blank input`() {
        assertTrue(BackupCodec.decode("   ") is DecodeResult.Failure)
    }

    @Test
    fun `rejects wrong app id`() {
        val encoded = BackupCodec.encode(snapshot())
            .replace("\"com.liferemaining.app\"", "\"com.example.other\"")
        val result = BackupCodec.decode(encoded)
        assertTrue(result is DecodeResult.Failure)
        assertTrue((result as DecodeResult.Failure).reason.contains("来源"))
    }

    @Test
    fun `rejects future version`() {
        val encoded = BackupCodec.encode(snapshot())
            .replace("\"version\": 1", "\"version\": 99")
        val result = BackupCodec.decode(encoded)
        assertTrue(result is DecodeResult.Failure)
        assertTrue((result as DecodeResult.Failure).reason.contains("版本"))
    }

    @Test
    fun `rejects invalid lifespan`() {
        val encoded = BackupCodec.encode(snapshot())
            .replace("\"expectedLifespanYears\": 82", "\"expectedLifespanYears\": 300")
        val result = BackupCodec.decode(encoded)
        assertTrue(result is DecodeResult.Failure)
        assertTrue((result as DecodeResult.Failure).reason.contains("寿命"))
    }

    @Test
    fun `rejects malformed date`() {
        val encoded = BackupCodec.encode(snapshot())
            .replace("\"date\": \"2026-10-26\"", "\"date\": \"2026-13-45\"")
        val result = BackupCodec.decode(encoded)
        assertTrue(result is DecodeResult.Failure)
        assertTrue((result as DecodeResult.Failure).reason.contains("每日记录"))
    }

    @Test
    fun `rejects empty goal title`() {
        val encoded = BackupCodec.encode(snapshot())
            .replace("\"title\": \"写完一本书\"", "\"title\": \"\"")
        val result = BackupCodec.decode(encoded)
        assertTrue(result is DecodeResult.Failure)
        assertTrue((result as DecodeResult.Failure).reason.contains("目标"))
    }

    @Test
    fun `empty snapshot is valid`() {
        val encoded = BackupCodec.encode(
            BackupSnapshot(null, emptyList(), emptyList(), emptyList(), emptyList())
        )
        val decoded = BackupCodec.decode(encoded)
        assertTrue(decoded is DecodeResult.Success)
        assertEquals(0, (decoded as DecodeResult.Success).snapshot.goals.size)
    }

    @Test
    fun `unknown enum values fall back instead of crashing`() {
        val encoded = BackupCodec.encode(snapshot())
            .replace("\"category\": \"CAREER\"", "\"category\": \"NOT_A_CATEGORY\"")
        val decoded = BackupCodec.decode(encoded)
        assertTrue(decoded is DecodeResult.Success)
        assertEquals(
            GoalCategory.OTHER,
            (decoded as DecodeResult.Success).snapshot.goals[0].category
        )
    }
}
