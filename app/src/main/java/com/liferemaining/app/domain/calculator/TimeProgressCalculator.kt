package com.liferemaining.app.domain.calculator

import com.liferemaining.app.domain.model.PeriodProgress
import com.liferemaining.app.domain.model.TimeStats
import com.liferemaining.app.domain.model.WeekDayCell
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * 年 / 月 / 周进度统计。分母一律来自真实日历：
 * - 年：lengthOfYear()（闰年 366）
 * - 月：lengthOfMonth()（2 月 28/29）
 * - 周：固定 7 天，周一为一周开始（ISO-8601，与 LocalDate.dayOfWeek 一致）
 *
 * 「已过去」采用「包含今天」的口径：今天算作已经过去的一天，
 * 因此 remain = total - elapsed 表示「今天之后还剩多少天」。这样首页的
 * 「本周还剩 / 本月还剩」与直觉一致，且 elapsed + remaining = total 恒成立。
 */
object TimeProgressCalculator {

    fun weekStart(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun yearProgress(today: LocalDate): PeriodProgress {
        val elapsed = today.dayOfYear
        val total = today.lengthOfYear()
        return PeriodProgress(elapsed.coerceIn(0, total), total)
    }

    fun monthProgress(today: LocalDate): PeriodProgress {
        val elapsed = today.dayOfMonth
        val total = today.lengthOfMonth()
        return PeriodProgress(elapsed.coerceIn(0, total), total)
    }

    fun weekProgress(today: LocalDate): PeriodProgress {
        val elapsed = today.dayOfWeek.value // 周一 = 1
        return PeriodProgress(elapsed.coerceIn(1, 7), 7)
    }

    fun weekDays(today: LocalDate, locale: Locale = Locale.SIMPLIFIED_CHINESE): List<WeekDayCell> {
        val start = weekStart(today)
        return (0..6).map { offset ->
            val date = start.plusDays(offset.toLong())
            WeekDayCell(
                date = date,
                weekdayLabel = date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                isToday = date == today,
                isPast = date.isBefore(today)
            )
        }
    }

    fun stats(today: LocalDate, birthDate: LocalDate?): TimeStats {
        val yearElapsedDays = today.dayOfYear.toLong()
        val lived = if (birthDate != null && !birthDate.isAfter(today)) {
            ChronoUnit.DAYS.between(birthDate, today)
        } else {
            0L
        }
        return TimeStats(
            today = today,
            year = yearProgress(today),
            month = monthProgress(today),
            week = weekProgress(today),
            weekDays = weekDays(today),
            yearDaysLived = minOf(yearElapsedDays, lived.coerceAtLeast(0L))
        )
    }
}
