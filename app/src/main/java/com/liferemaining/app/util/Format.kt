package com.liferemaining.app.util

import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Format {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日", Locale.SIMPLIFIED_CHINESE)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("M 月 d 日", Locale.SIMPLIFIED_CHINESE)
    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun longDate(date: LocalDate): String = date.format(dateFormatter)
    fun shortDate(date: LocalDate): String = date.format(shortDateFormatter)
    fun isoDate(date: LocalDate): String = date.format(isoDateFormatter)
    fun time(time: LocalTime): String = time.format(timeFormatter)

    /** 4200 -> "4,200"。数字分组固定用逗号，避免地区差异。 */
    fun grouped(value: Long): String =
        NumberFormat.getIntegerInstance(Locale.US).format(value)

    fun percent(value: Double): String = String.format(Locale.US, "%.1f%%", value)

    /** 距离天数的人类表达，不出现负数。 */
    fun daysUntil(days: Long): String = when {
        days == 0L -> "就是今天"
        days == 1L -> "明天"
        days > 1L -> "还有 $days 天"
        else -> "已过去 ${-days} 天"
    }

    /** 用于倒计时长度展示：分钟 < 1 小时时四舍五入到分钟。 */
    fun durationShort(duration: Duration): String {
        val minutes = duration.toMinutes().coerceAtLeast(0)
        return if (minutes < 60) "$minutes 分钟" else "${minutes / 60} 小时 ${minutes % 60} 分"
    }
}
