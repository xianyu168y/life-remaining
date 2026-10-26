package com.liferemaining.app.domain

import com.liferemaining.app.notifications.ReminderPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderScheduleTest {

    @Test
    fun `delay before today's reminder`() {
        val delay = ReminderPolicy.initialDelayUntil(
            LocalTime.of(8, 30),
            LocalDateTime.of(2026, 10, 26, 7, 0)
        )
        assertEquals(Duration.ofMinutes(90), delay)
    }

    @Test
    fun `delay rolls to next day after reminder time`() {
        val delay = ReminderPolicy.initialDelayUntil(
            LocalTime.of(8, 30),
            LocalDateTime.of(2026, 10, 26, 9, 0)
        )
        assertEquals(Duration.ofMinutes(23 * 60 + 30), delay)
    }

    @Test
    fun `delay at exact reminder time is one full day`() {
        val delay = ReminderPolicy.initialDelayUntil(
            LocalTime.of(8, 30),
            LocalDateTime.of(2026, 10, 26, 8, 30)
        )
        assertEquals(Duration.ofDays(1), delay)
    }

    @Test
    fun `tone messages are non empty and vary by seed`() {
        com.liferemaining.app.domain.model.ReminderTone.entries.forEach { tone ->
            val first = ReminderPolicy.messageFor(tone, 0)
            val second = ReminderPolicy.messageFor(tone, 1)
            assertTrue(first.isNotBlank())
            assertTrue(second.isNotBlank())
            assertNotEquals(first, second)
        }
    }

    @Test
    fun `negative seed does not crash`() {
        assertTrue(ReminderPolicy.messageFor(
            com.liferemaining.app.domain.model.ReminderTone.GENTLE, -7
        ).isNotBlank())
    }
}
