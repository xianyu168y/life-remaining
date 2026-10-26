package com.liferemaining.app.calculator

import com.liferemaining.app.domain.calculator.MilestoneCalculator
import com.liferemaining.app.domain.model.Milestone
import com.liferemaining.app.domain.model.MilestoneCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MilestoneCalculatorTest {

    private fun milestone(
        title: String,
        date: LocalDate,
        repeats: Boolean = false
    ) = Milestone(
        id = title.hashCode().toLong(),
        title = title,
        date = date,
        category = MilestoneCategory.OTHER,
        repeatsAnnually = repeats
    )

    @Test
    fun `annual milestone in current year when not yet passed`() {
        val item = milestone("生日", LocalDate.of(1990, 12, 1), repeats = true)
        val occurrence = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2026, 10, 26))
        assertEquals(LocalDate.of(2026, 12, 1), occurrence.nextDate)
        assertEquals(36L, occurrence.daysUntil)
        assertFalse(occurrence.isPast)
    }

    @Test
    fun `annual milestone rolls to next year when already passed`() {
        val item = milestone("纪念日", LocalDate.of(2015, 3, 8), repeats = true)
        val occurrence = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2026, 10, 26))
        assertEquals(LocalDate.of(2027, 3, 8), occurrence.nextDate)
    }

    @Test
    fun `annual milestone today counts zero days`() {
        val item = milestone("今天", LocalDate.of(2000, 10, 26), repeats = true)
        val occurrence = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2026, 10, 26))
        assertEquals(LocalDate.of(2026, 10, 26), occurrence.nextDate)
        assertEquals(0L, occurrence.daysUntil)
    }

    @Test
    fun `feb 29 annual milestone uses feb 28 in common years`() {
        val item = milestone("闰日", LocalDate.of(2000, 2, 29), repeats = true)
        val inCommonYear = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2026, 1, 1))
        assertEquals(LocalDate.of(2026, 2, 28), inCommonYear.nextDate)

        val inLeapYear = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2028, 1, 1))
        assertEquals(LocalDate.of(2028, 2, 29), inLeapYear.nextDate)
    }

    @Test
    fun `feb 29 annual milestone after feb 28 rolls to next year`() {
        val item = milestone("闰日", LocalDate.of(2000, 2, 29), repeats = true)
        val occurrence = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2026, 3, 1))
        assertEquals(LocalDate.of(2027, 2, 28), occurrence.nextDate)
    }

    @Test
    fun `one-off milestone in the past is flagged not faked`() {
        val item = milestone("旅行", LocalDate.of(2026, 1, 5))
        val occurrence = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2026, 10, 26))
        assertEquals(LocalDate.of(2026, 1, 5), occurrence.nextDate)
        assertTrue(occurrence.isPast)
        assertTrue(occurrence.daysUntil < 0)
    }

    @Test
    fun `one-off future milestone keeps original date`() {
        val item = milestone("考试", LocalDate.of(2026, 12, 20))
        val occurrence = MilestoneCalculator.nextOccurrence(item, LocalDate.of(2026, 10, 26))
        assertEquals(LocalDate.of(2026, 12, 20), occurrence.nextDate)
        assertEquals(55L, occurrence.daysUntil)
        assertFalse(occurrence.isPast)
    }

    @Test
    fun `upcoming sorts by days and limits results`() {
        val today = LocalDate.of(2026, 10, 26)
        val items = listOf(
            milestone("C", LocalDate.of(2026, 12, 1), repeats = true),
            milestone("A", LocalDate.of(2026, 11, 1), repeats = true),
            milestone("B", LocalDate.of(2026, 11, 15), repeats = true),
            milestone("D", LocalDate.of(2026, 10, 20), repeats = true)
        )
        val upcoming = MilestoneCalculator.upcoming(items, today, limit = 3)
        assertEquals(3, upcoming.size)
        assertEquals(listOf("A", "B", "C"), upcoming.map { it.milestone.title })
    }

    @Test
    fun `upcoming excludes past one-off items`() {
        val today = LocalDate.of(2026, 10, 26)
        val items = listOf(
            milestone("过去", LocalDate.of(2026, 1, 1)),
            milestone("未来", LocalDate.of(2026, 12, 1))
        )
        val upcoming = MilestoneCalculator.upcoming(items, today)
        assertEquals(1, upcoming.size)
        assertEquals("未来", upcoming.first().milestone.title)
    }

    @Test
    fun `leap year detection matches gregorian rules`() {
        assertTrue(MilestoneCalculator.isLeapYear(2024))
        assertTrue(MilestoneCalculator.isLeapYear(2000))
        assertFalse(MilestoneCalculator.isLeapYear(1900))
        assertFalse(MilestoneCalculator.isLeapYear(2025))
    }

    @Test
    fun `resolveInYear keeps normal dates intact`() {
        val original = LocalDate.of(1993, 7, 14)
        assertEquals(LocalDate.of(2031, 7, 14), MilestoneCalculator.resolveInYear(original, 2031))
    }
}
