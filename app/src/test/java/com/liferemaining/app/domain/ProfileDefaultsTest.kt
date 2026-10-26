package com.liferemaining.app.domain

import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.GoalCategory
import com.liferemaining.app.domain.model.GoalPriority
import com.liferemaining.app.domain.model.GoalStatus
import com.liferemaining.app.domain.model.MilestoneCategory
import com.liferemaining.app.domain.model.ThemeMode
import com.liferemaining.app.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProfileDefaultsTest {

    @Test
    fun `fresh profile has no fabricated personal data`() {
        val profile = UserProfile()
        assertNull(profile.birthDate)
        assertNull(profile.expectedLifespanYears)
        assertFalse(profile.onboardingCompleted)
        assertFalse(profile.dailyReminderEnabled)
        assertEquals(ThemeMode.SYSTEM, profile.themeMode)
        assertEquals(AccentKey.TEAL, profile.accentKey)
        assertFalse(profile.countdownConfigured)
    }

    @Test
    fun `countdown requires birth date lifespan and enabled flag`() {
        val today = LocalDate.of(2026, 10, 26)
        val base = UserProfile(birthDate = LocalDate.of(1990, 1, 1), expectedLifespanYears = 80)
        assertTrue(base.countdownConfigured)
        assertFalse(base.copy(countdownEnabled = false).countdownConfigured)
        assertFalse(base.copy(expectedLifespanYears = null).countdownConfigured)
        assertFalse(base.copy(birthDate = null).countdownConfigured)
        assertTrue(today.isAfter(base.birthDate!!))
    }

    @Test
    fun `enum storage parsing falls back safely`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("garbage"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(null))

        assertEquals(AccentKey.MOSS, AccentKey.fromStorage("moss"))
        assertEquals(AccentKey.TEAL, AccentKey.fromStorage(null))

        assertEquals(GoalCategory.TRAVEL, GoalCategory.fromStorage("TRAVEL"))
        assertEquals(GoalCategory.OTHER, GoalCategory.fromStorage("UNKNOWN"))

        assertEquals(GoalStatus.COMPLETED, GoalStatus.fromStorage("COMPLETED"))
        assertEquals(GoalStatus.ACTIVE, GoalStatus.fromStorage(""))

        assertEquals(GoalPriority.HIGH, GoalPriority.fromStorage("HIGH"))
        assertEquals(GoalPriority.MEDIUM, GoalPriority.fromStorage("x"))

        assertEquals(MilestoneCategory.BIRTHDAY, MilestoneCategory.fromStorage("BIRTHDAY"))
        assertEquals(MilestoneCategory.OTHER, MilestoneCategory.fromStorage(null))
    }
}
