package com.liferemaining.app.domain.calculator

import com.liferemaining.app.domain.model.Milestone
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit

/**
 * 重要日期的「下一次发生」计算。
 *
 * 年度重复不能简单 +365：
 * - 2 月 29 日的年度事件，在非闰年落到 2 月 28 日（Java Time 的 plusYears 规则）。
 * - 跨年时先看今年是否还没过，否则看明年。
 * - 一次性事件如果已经过去，仍返回原日期，并由 isPast 标记，不伪造「下一次」。
 */
object MilestoneCalculator {

    data class Occurrence(
        val milestone: Milestone,
        val nextDate: LocalDate,
        val daysUntil: Long,
        val isPast: Boolean
    )

    fun nextOccurrence(milestone: Milestone, today: LocalDate): Occurrence {
        if (!milestone.repeatsAnnually) {
            val days = ChronoUnit.DAYS.between(today, milestone.date)
            return Occurrence(milestone, milestone.date, days, days < 0)
        }
        val thisYear = resolveInYear(milestone.date, today.year)
        val next = if (thisYear.isBefore(today)) {
            resolveInYear(milestone.date, today.year + 1)
        } else {
            thisYear
        }
        return Occurrence(milestone, next, ChronoUnit.DAYS.between(today, next), false)
    }

    /**
     * 把原始日期投射到指定年份：
     * - 2 月 29 日在平年取 2 月 28 日；
     * - 其它日期保持月日不变。
     */
    fun resolveInYear(original: LocalDate, year: Int): LocalDate {
        val month = original.month
        val day = original.dayOfMonth
        if (month == Month.FEBRUARY && day == 29 && !isLeapYear(year)) {
            return LocalDate.of(year, 2, 28)
        }
        return LocalDate.of(year, month, day)
    }

    fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

    /** 首页「接下来值得期待」：只取未来 3 条，按天数升序。 */
    fun upcoming(
        milestones: List<Milestone>,
        today: LocalDate,
        limit: Int = 3
    ): List<Occurrence> = milestones
        .map { nextOccurrence(it, today) }
        .filter { !it.isPast }
        .sortedWith(compareBy({ it.daysUntil }, { it.milestone.title }))
        .take(limit)
}
