package com.liferemaining.app.domain

import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 跨一整年逐日验证不变量，防止边界日期上出现 off-by-one。
 * 这是产品里最容易出错、也最容易被用户发现的地方。
 */
class TimeStatsInvariantTest {

    @Test
    fun `iterate every day of a leap year`() {
        var date = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 12, 31)
        while (!date.isAfter(end)) {
            val year = TimeProgressCalculator.yearProgress(date)
            val month = TimeProgressCalculator.monthProgress(date)
            val week = TimeProgressCalculator.weekProgress(date)

            assertEquals(366, year.total)
            assertEquals(date.dayOfYear, year.elapsed)
            assertEquals(year.total, year.elapsed + year.remaining)
            assertTrue(year.percent in 0.0..100.0)

            assertEquals(date.lengthOfMonth(), month.total)
            assertEquals(date.dayOfMonth, month.elapsed)
            assertEquals(month.total, month.elapsed + month.remaining)

            assertEquals(7, week.total)
            assertEquals(date.dayOfWeek.value, week.elapsed)
            assertEquals(week.total, week.elapsed + week.remaining)

            val days = TimeProgressCalculator.weekDays(date)
            assertEquals(7, days.size)
            assertEquals(1, days.count { it.isToday })
            assertEquals(date.dayOfWeek.value - 1, days.count { it.isPast })

            date = date.plusDays(1)
        }
    }

    @Test
    fun `countdown invariants hold across two years of dates`() {
        val birth = LocalDate.of(1992, 2, 29)
        var today = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2025, 12, 31)
        val lifespan = 90
        while (!today.isAfter(end)) {
            val result = LifeCountdownCalculator.calculate(birth, today, lifespan)
            val progress = result.progressPercent!!
            val remaining = result.remainingDays!!
            val total = result.totalDays!!
            assertTrue(progress >= 0.0)
            assertTrue(progress <= 100.0)
            assertTrue(remaining >= 0L)
            assertEquals(total, result.daysLived + remaining)
            if (!result.reachedEnd) {
                assertTrue(remaining > 0L)
            }
            today = today.plusDays(1)
        }
    }

    @Test
    fun `age parts never exceed a plausible bound`() {
        val birth = LocalDate.of(1988, 3, 15)
        var today = birth
        while (today.isBefore(LocalDate.of(2030, 1, 1))) {
            val (y, m, d) = LifeCountdownCalculator.ageParts(birth, today)
            assertTrue("years", y in 0..50)
            assertTrue("months", m in 0..11)
            assertTrue("days", d in 0..30)
            today = today.plusDays(7)
        }
    }
}
