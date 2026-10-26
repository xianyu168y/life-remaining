package com.liferemaining.app.calculator

import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LifeCountdownCalculatorTest {

    private val birth = LocalDate.of(1990, 5, 15)

    @Test
    fun `birth date null returns empty state`() {
        val result = LifeCountdownCalculator.calculate(null, LocalDate.of(2026, 10, 26), 80)
        assertNull(result.birthDate)
        assertNull(result.ageYears)
        assertNull(result.remainingDays)
        assertNull(result.progressPercent)
        assertFalse(result.countdownConfigured)
        assertEquals(0L, result.daysLived)
    }

    @Test
    fun `same day as birth counts zero days lived`() {
        val result = LifeCountdownCalculator.calculate(birth, birth, 80)
        assertEquals(0L, result.daysLived)
        assertEquals(0, result.ageYears)
        assertEquals(0, result.ageMonths)
        assertEquals(0, result.ageDays)
        assertEquals(0.0, result.progressPercent!!, 0.0001)
        assertFalse(result.reachedEnd)
        assertTrue(result.countdownConfigured)
    }

    @Test
    fun `ordinary birth date produces calendar based numbers`() {
        val today = LocalDate.of(2026, 10, 26)
        val result = LifeCountdownCalculator.calculate(birth, today, 80)

        assertEquals(36, result.ageYears)
        assertEquals(5, result.ageMonths)
        assertEquals(11, result.ageDays)

        // 1990-05-15 -> 2026-10-26
        val expectedLived = java.time.temporal.ChronoUnit.DAYS.between(birth, today)
        assertEquals(expectedLived, result.daysLived)

        val end = LocalDate.of(2070, 5, 15)
        assertEquals(end, result.expectedEndDate)
        assertEquals(
            java.time.temporal.ChronoUnit.DAYS.between(birth, end),
            result.totalDays
        )
        assertEquals(
            java.time.temporal.ChronoUnit.DAYS.between(today, end),
            result.remainingDays
        )
        assertFalse(result.reachedEnd)
    }

    @Test
    fun `leap day birth rolls to feb 28 in non leap years`() {
        val leapBirth = LocalDate.of(1984, 2, 29)
        val result = LifeCountdownCalculator.calculate(leapBirth, LocalDate.of(2025, 2, 28), 41)

        assertEquals(LocalDate.of(2025, 2, 28), result.expectedEndDate)
        assertEquals(0L, result.remainingDays)
        assertTrue(result.reachedEnd)
        assertEquals(100.0, result.progressPercent!!, 0.0001)
    }

    @Test
    fun `leap day birth one day before endpoint still has one day left`() {
        val leapBirth = LocalDate.of(1984, 2, 29)
        val result = LifeCountdownCalculator.calculate(leapBirth, LocalDate.of(2025, 2, 27), 41)

        assertEquals(LocalDate.of(2025, 2, 28), result.expectedEndDate)
        assertEquals(1L, result.remainingDays)
        assertFalse(result.reachedEnd)
        assertTrue(result.progressPercent!! < 100.0)
    }

    @Test
    fun `leap day age parts agree with calendar anchors`() {
        val leapBirth = LocalDate.of(1984, 2, 29)
        val parts = LifeCountdownCalculator.ageParts(leapBirth, LocalDate.of(2025, 2, 28))
        assertEquals(40, parts.first)
        assertEquals(11, parts.second)
        assertEquals(30, parts.third)

        val onBirthday = LifeCountdownCalculator.ageParts(leapBirth, LocalDate.of(2025, 3, 1))
        assertEquals(41, onBirthday.first)
        assertEquals(0, onBirthday.second)
        assertEquals(1, onBirthday.third)
    }

    @Test
    fun `today equal to expected endpoint reports reached end with zero remaining`() {
        val expectedEnd = birth.plusYears(80)
        val result = LifeCountdownCalculator.calculate(birth, expectedEnd, 80)

        assertEquals(expectedEnd, result.expectedEndDate)
        assertEquals(0L, result.remainingDays)
        assertTrue(result.reachedEnd)
        assertEquals(100.0, result.progressPercent!!, 0.0001)
    }

    @Test
    fun `today after expected endpoint never shows negative numbers`() {
        val expectedEnd = birth.plusYears(80)
        val result = LifeCountdownCalculator.calculate(
            birth,
            expectedEnd.plusDays(500),
            80
        )

        assertEquals(0L, result.remainingDays)
        assertTrue(result.reachedEnd)
        assertEquals(100.0, result.progressPercent!!, 0.0001)
        // 已度过天数仍然增长，如实反映日历。
        assertTrue(result.daysLived > result.totalDays!!)
    }

    @Test
    fun `progress stays within zero and hundred for extreme lifespans`() {
        val today = LocalDate.of(2026, 10, 26)
        listOf(1, 5, 20, 60, 120).forEach { lifespan ->
            val result = LifeCountdownCalculator.calculate(birth, today, lifespan)
            val progress = result.progressPercent!!
            assertTrue("progress for $lifespan should be >= 0", progress >= 0.0)
            assertTrue("progress for $lifespan should be <= 100", progress <= 100.0)
        }
    }

    @Test
    fun `invalid lifespan yields no remaining days but keeps age`() {
        listOf(null, 0, -1, 121, 1000).forEach { lifespan ->
            val result = LifeCountdownCalculator.calculate(
                birth,
                LocalDate.of(2026, 10, 26),
                lifespan
            )
            assertNull("lifespan $lifespan should not compute endpoint", result.expectedEndDate)
            assertNull(result.remainingDays)
            assertNull(result.progressPercent)
            assertFalse(result.countdownConfigured)
            assertEquals(36, result.ageYears)
        }
    }

    @Test
    fun `future birth date is treated as not configured`() {
        val today = LocalDate.of(2026, 10, 26)
        val result = LifeCountdownCalculator.calculate(today.plusDays(1), today, 80)
        assertFalse(result.countdownConfigured)
        assertEquals(0L, result.daysLived)
        assertNull(result.remainingDays)
    }

    @Test
    fun `first day of month crossing counts correct days`() {
        val result = LifeCountdownCalculator.calculate(
            LocalDate.of(2024, 1, 31),
            LocalDate.of(2024, 3, 1),
            80
        )
        assertEquals(30L, result.daysLived)
        assertEquals(0, result.ageYears)
        assertEquals(1, result.ageMonths)
        assertEquals(1, result.ageDays)
    }

    @Test
    fun `year crossing keeps days consistent`() {
        val result = LifeCountdownCalculator.calculate(
            LocalDate.of(1990, 12, 31),
            LocalDate.of(2026, 1, 1),
            80
        )
        assertEquals(35, result.ageYears)
        assertEquals(0, result.ageMonths)
        assertEquals(1, result.ageDays)
        assertEquals(
            java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.of(1990, 12, 31),
                LocalDate.of(2026, 1, 1)
            ),
            result.daysLived
        )
    }

    @Test
    fun `leap year adds an extra day to total`() {
        val result = LifeCountdownCalculator.calculate(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            1
        )
        // 2024-01-01 + 1 年 = 2025-01-01，2024 是闰年，共 366 天。
        assertEquals(366L, result.totalDays)
        assertEquals(365L, result.daysLived)
        assertEquals(1L, result.remainingDays)
        assertEquals(99.7, result.progressPercent!!, 0.1)
    }

    @Test
    fun `lifespan bounds are enforced`() {
        assertFalse(LifeCountdownCalculator.isValidLifespan(0))
        assertFalse(LifeCountdownCalculator.isValidLifespan(121))
        assertFalse(LifeCountdownCalculator.isValidLifespan(null))
        assertTrue(LifeCountdownCalculator.isValidLifespan(1))
        assertTrue(LifeCountdownCalculator.isValidLifespan(120))
    }
}
