package com.liferemaining.app.util

import java.time.LocalDate

/**
 * 全 App 唯一的「今天」来源。
 * 默认取系统日期；测试可通过 [override] 固定日期，避免跨天导致断言抖动。
 */
object AppClock {
    @Volatile
    private var override: LocalDate? = null

    fun today(): LocalDate = override ?: LocalDate.now()

    /** 仅供测试与预览使用；生产代码不要调用。 */
    fun setOverride(date: LocalDate?) {
        override = date
    }
}
