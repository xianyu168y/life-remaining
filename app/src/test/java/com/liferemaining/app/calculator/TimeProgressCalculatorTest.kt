package com.liferemaining.app.calculator

import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class TimeProgressCalculatorTest {

    @Test
    fun `year progress uses real calendar length`() {
        val leapYear = TimeProgressCalculator.yearProgress(LocalDate.of(2024, 12, 31))
        assertEquals(366, leapYear.total)
        assertEquals(366, leapYear.elapsed)
        assertEquals(0, leapYear.remaining)
        assertEquals(100.0, leapYear.percent, 0.0001)

        val commonYear = TimeProgressCalculator.yearProgress(LocalDate.of(2025, 12, 31))
        assertEquals(365, commonYear.total)
        assertEquals(365, commonYear.elapsed)

        val firstDay = TimeProgressCalculator.yearProgress(LocalDate.of(2025, 1, 1))
        assertEquals(1, firstDay.elapsed)
        assertEquals(364, firstDay.remaining)
    }

    @Test
    fun `month progress uses month length`() {
        val februaryLeap = TimeProgressCalculator.monthProgress(LocalDate.of(2024, 2, 10))
        assertEquals(29, februaryLeap.total)
        assertEquals(10, februaryLeap.elapsed)
        assertEquals(19, februaryLeap.remaining)

        val februaryCommon = TimeProgressCalculator.monthProgress(LocalDate.of(2025, 2, 10))
        assertEquals(28, februaryCommon.total)

        val april = TimeProgressCalculator.monthProgress(LocalDate.of(2025, 4, 30))
        assertEquals(30, april.total)
        assertEquals(30, april.elapsed)
        assertEquals(0, april.remaining)
    }

    @Test
    fun `week progress starts on monday`() {
        val monday = LocalDate.of(2026, 10, 26) // Monday
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)

        val week = TimeProgressCalculator.weekProgress(monday)
        assertEquals(1, week.elapsed)
        assertEquals(6, week.remaining)
        assertEquals(7, week.total)

        val sunday = monday.plusDays(6)
        val sundayWeek = TimeProgressCalculator.weekProgress(sunday)
        assertEquals(7, sundayWeek.elapsed)
        assertEquals(0, sundayWeek.remaining)
    }

    @Test
    fun `week start resolves to monday for every weekday`() {
        val monday = LocalDate.of(2026, 10, 26)
        (0L..6L).forEach { offset ->
            val date = monday.plusDays(offset)
            assertEquals(monday, TimeProgressCalculator.weekStart(date))
        }
    }

    @Test
    fun `week days marks past today and future`() {
        val today = LocalDate.of(2026, 10, 28) // Wednesday
        val cells = TimeProgressCalculator.weekDays(today)
        assertEquals(7, cells.size)
        assertEquals(1, cells.count { it.isToday })
        assertEquals(2, cells.count { it.isPast })
        assertEquals(4, cells.count { !it.isPast && !it.isToday })
        assertEquals(LocalDate.of(2026, 10, 26), cells.first().date)
        assertEquals(LocalDate.of(2026, 11, 1), cells.last().date)
        assertTrue(cells[2].isToday)
        assertFalse(cells[3].isPast)
    }

    @Test
    fun `elapsed plus remaining equals total for all periods`() {
        val date = LocalDate.of(2024, 2, 29)
        listOf(
            TimeProgressCalculator.yearProgress(date),
            TimeProgressCalculator.monthProgress(date),
            TimeProgressCalculator.weekProgress(date)
        ).forEach { progress ->
            assertEquals(progress.total, progress.elapsed + progress.remaining)
        }
    }

    @Test
    fun `fraction is clamped`() {
        assertEquals(0f, com.liferemaining.app.domain.model.PeriodProgress(0, 0).fraction)
        assertEquals(1f, com.liferemaining.app.domain.model.PeriodProgress(10, 5).fraction)
        assertEquals(0f, com.liferemaining.app.domain.model.PeriodProgress(-3, 5).fraction)
    }

    @Test
    fun `stats handles missing birth date`() {
        val today = LocalDate.of(2026, 10, 26)
        val stats = TimeProgressCalculator.stats(today, null)
        assertEquals(0L, stats.yearDaysLived)
        assertEquals(7, stats.weekDays.size)
    }
}
