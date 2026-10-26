package com.liferemaining.app.notifications

import com.liferemaining.app.domain.model.ReminderTone
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 提醒的纯逻辑部分：不依赖 Android，便于 JVM 单元测试。
 * 只负责「下一次提醒还有多久」与「用哪一句文案」。
 */
object ReminderPolicy {

    private val TONE_MESSAGES: Map<ReminderTone, List<String>> = mapOf(
        ReminderTone.GENTLE to listOf(
            "今天也值得认真度过。",
            "留一点时间给真正在意的事。",
            "时间正在流动，慢一点也没关系。"
        ),
        ReminderTone.NEUTRAL to listOf(
            "今天的时间已经开始计时。",
            "看看今天的进度，再决定先做哪一件事。",
            "一天一格，今天这一格还没填。"
        ),
        ReminderTone.DIRECT to listOf(
            "今天还剩多少时间？打开看一眼。",
            "目标没有推进，今天就会过去。",
            "先做一件对你有长期价值的事。"
        )
    )

    fun messageFor(tone: ReminderTone, seed: Int): String {
        val list = TONE_MESSAGES.getValue(tone)
        return list[Math.floorMod(seed, list.size)]
    }

    /** 距离下一次提醒时间的时长；今天的时间点已过则顺延到明天。 */
    fun initialDelayUntil(time: LocalTime, now: LocalDateTime): Duration {
        val todayTarget = now.toLocalDate().atTime(time)
        val target = if (todayTarget.isAfter(now)) todayTarget else todayTarget.plusDays(1)
        return Duration.between(now, target)
    }
}
