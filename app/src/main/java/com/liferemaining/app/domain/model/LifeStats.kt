package com.liferemaining.app.domain.model

import java.time.LocalDate

/**
 * 生命倒计时结果。
 *
 * 日期边界定义（全 App 唯一口径）：
 * - 已度过天数 = ChronoUnit.DAYS.between(birthDate, today)，出生当天计 0 天。
 * - 预计终点 = birthDate.plusYears(expectedLifespanYears)（Java Time 明确规则：
 *   2 月 29 日出生者在非闰年的周年日按 2 月 28 日计算）。
 * - 总天数 = DAYS.between(birthDate, expectedEndDate)。
 * - 剩余天数 = DAYS.between(today, expectedEndDate)，不小于 0。
 * - 生命进度 = 已度过 / 总天数，限制在 0..100。
 */
data class LifeCountdown(
    val birthDate: LocalDate?,
    val today: LocalDate,
    val expectedLifespanYears: Int?,
    val expectedEndDate: LocalDate?,
    val daysLived: Long,
    val totalDays: Long?,
    val remainingDays: Long?,
    val ageYears: Int?,
    val ageMonths: Int?,
    val ageDays: Int?,
    val progressPercent: Double?,
    val reachedEnd: Boolean,
    val countdownConfigured: Boolean
) {
    val hasAge: Boolean get() = ageYears != null
}

/** 单个时间段（年 / 月 / 周）的进度。elapsed + remaining = total 恒成立。 */
data class PeriodProgress(
    val elapsed: Int,
    val total: Int
) {
    val remaining: Int get() = (total - elapsed).coerceAtLeast(0)
    val fraction: Float
        get() = if (total <= 0) 0f else (elapsed.toFloat() / total).coerceIn(0f, 1f)
    val percent: Double get() = fraction * 100.0
}

data class WeekDayCell(
    val date: LocalDate,
    val weekdayLabel: String,
    val isToday: Boolean,
    val isPast: Boolean
)

data class TimeStats(
    val today: LocalDate,
    val year: PeriodProgress,
    val month: PeriodProgress,
    val week: PeriodProgress,
    val weekDays: List<WeekDayCell>,
    val yearDaysLived: Long
)
