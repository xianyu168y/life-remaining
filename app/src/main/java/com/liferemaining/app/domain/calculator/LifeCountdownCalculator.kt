package com.liferemaining.app.domain.calculator

import com.liferemaining.app.domain.model.LifeCountdown
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 生命倒计时计算。纯函数，不依赖 Android，可直接在 JVM 单元测试里验证。
 *
 * 关键约定（全 App 唯一口径，任何页面不得另立标准）：
 *
 * 1. 全部使用 [LocalDate] 的日历日差，不用毫秒差、不用固定 365 天、不把月份当 30 天。
 * 2. 「已度过天数」= DAYS.between(birthDate, today)。出生当天为 0 天。
 * 3. 「预计终点」= birthDate.plusYears(lifespan)。Java Time 对 2 月 29 日的规则是
 *    回退到 2 月 28 日（例如 1984-02-29 + 1 年 = 1985-02-28），本 App 沿用该规则。
 * 4. 「总天数」= DAYS.between(birthDate, expectedEndDate)。
 * 5. 「剩余天数」= DAYS.between(today, expectedEndDate)，结果夹到 >= 0，
 *    因此到期当天为 0，之后一直为 0，不会出现负数。
 * 6. 「生命进度」= 已度过 / 总天数，夹到 0..100，分母为 0 时返回 0。
 * 7. 预期寿命合法区间 [MIN_LIFESPAN_YEARS, MAX_LIFESPAN_YEARS]；越界视为未配置，
 *    不抛异常、不崩溃、不显示伪造数字。
 *
 * 该结果是数学估算，不是医学预测。
 */
object LifeCountdownCalculator {

    const val MIN_LIFESPAN_YEARS = 1
    const val MAX_LIFESPAN_YEARS = 120

    fun isValidLifespan(years: Int?): Boolean =
        years != null && years in MIN_LIFESPAN_YEARS..MAX_LIFESPAN_YEARS

    fun expectedEndDate(birthDate: LocalDate, lifespanYears: Int): LocalDate =
        birthDate.plusYears(lifespanYears.toLong())

    /**
     * 计算年龄的年 / 月 / 日表达。
     *
     * 采用「先加整年、再加整月、余数为天」的锚定法：
     * 1984-02-29 到 2025-02-28 得 40 年 11 月 30 天（因为 2024-02-29 到 2025-02-28
     * 还差一天才满整年），这与人类口语里的「差一天满 41 岁」一致。
     */
    fun ageParts(birthDate: LocalDate, today: LocalDate): Triple<Int, Int, Int> {
        if (today.isBefore(birthDate)) return Triple(0, 0, 0)
        var years = ChronoUnit.YEARS.between(birthDate, today).toInt()
        var anchor = birthDate.plusYears(years.toLong())
        // 处理 plusYears 造成的越界（例如 2 月 29 日出生在非闰年），保证 anchor 不回退到过去。
        while (anchor.isAfter(today) && years > 0) {
            years -= 1
            anchor = birthDate.plusYears(years.toLong())
        }
        var months = ChronoUnit.MONTHS.between(anchor, today).toInt()
        var monthAnchor = anchor.plusMonths(months.toLong())
        while (monthAnchor.isAfter(today) && months > 0) {
            months -= 1
            monthAnchor = anchor.plusMonths(months.toLong())
        }
        val days = ChronoUnit.DAYS.between(monthAnchor, today).toInt()
        return Triple(years, months, days)
    }

    /**
     * @param birthDate 用户出生日期；为空时返回未配置状态。
     * @param lifespanYears 用户设定的预期寿命；为空或越界时只给年龄与已度过天数，
     *        不编造剩余天数。
     */
    fun calculate(
        birthDate: LocalDate?,
        today: LocalDate,
        lifespanYears: Int?
    ): LifeCountdown {
        if (birthDate == null) {
            return empty(today)
        }
        // 出生日期在未来属于非法输入（Onboarding 已禁止，此处再兜底一次）。
        if (birthDate.isAfter(today)) {
            return LifeCountdown(
                birthDate = birthDate,
                today = today,
                expectedLifespanYears = lifespanYears,
                expectedEndDate = null,
                daysLived = 0,
                totalDays = null,
                remainingDays = null,
                ageYears = 0,
                ageMonths = 0,
                ageDays = 0,
                progressPercent = null,
                reachedEnd = false,
                countdownConfigured = false
            )
        }

        val daysLived = ChronoUnit.DAYS.between(birthDate, today)
        val (years, months, days) = ageParts(birthDate, today)

        if (!isValidLifespan(lifespanYears)) {
            return LifeCountdown(
                birthDate = birthDate,
                today = today,
                expectedLifespanYears = lifespanYears,
                expectedEndDate = null,
                daysLived = daysLived,
                totalDays = null,
                remainingDays = null,
                ageYears = years,
                ageMonths = months,
                ageDays = days,
                progressPercent = null,
                reachedEnd = false,
                countdownConfigured = false
            )
        }

        val end = expectedEndDate(birthDate, lifespanYears!!)
        val totalDays = ChronoUnit.DAYS.between(birthDate, end)
        val rawRemaining = ChronoUnit.DAYS.between(today, end)
        val remaining = rawRemaining.coerceAtLeast(0L)
        val reachedEnd = !today.isBefore(end)
        val progress = if (totalDays <= 0L) {
            0.0
        } else {
            (daysLived.toDouble() / totalDays.toDouble() * 100.0).coerceIn(0.0, 100.0)
        }

        return LifeCountdown(
            birthDate = birthDate,
            today = today,
            expectedLifespanYears = lifespanYears,
            expectedEndDate = end,
            daysLived = daysLived,
            totalDays = totalDays,
            remainingDays = remaining,
            ageYears = years,
            ageMonths = months,
            ageDays = days,
            progressPercent = progress,
            reachedEnd = reachedEnd,
            countdownConfigured = true
        )
    }

    /** 未设置出生日期时的空状态。不写入任何示例数字。 */
    fun empty(today: LocalDate): LifeCountdown = LifeCountdown(
        birthDate = null,
        today = today,
        expectedLifespanYears = null,
        expectedEndDate = null,
        daysLived = 0,
        totalDays = null,
        remainingDays = null,
        ageYears = null,
        ageMonths = null,
        ageDays = null,
        progressPercent = null,
        reachedEnd = false,
        countdownConfigured = false
    )
}
