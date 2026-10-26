package com.liferemaining.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * 用户的核心生命参数与偏好。所有字段都有明确默认值，便于 DataStore 逐步升级。
 *
 * expectedLifespanYears 允许为空：用户在 Onboarding 里选择「暂不启用倒计时」时，
 * 不写入一个看似真实的寿命数字；之后可在设置里补填。
 */
data class UserProfile(
    val birthDate: LocalDate? = null,
    val expectedLifespanYears: Int? = null,
    val countdownEnabled: Boolean = true,
    val showExactDays: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentKey: AccentKey = AccentKey.TEAL,
    val dynamicColor: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(8, 30),
    val reminderTone: ReminderTone = ReminderTone.GENTLE,
    val onboardingCompleted: Boolean = false
) {
    val hasBirthDate: Boolean get() = birthDate != null
    val hasLifespan: Boolean get() = expectedLifespanYears != null
    val countdownConfigured: Boolean
        get() = countdownEnabled && birthDate != null && expectedLifespanYears != null
}
