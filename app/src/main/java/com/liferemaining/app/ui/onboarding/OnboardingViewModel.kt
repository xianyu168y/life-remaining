package com.liferemaining.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.model.ReminderTone
import com.liferemaining.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class OnboardingState(
    val step: Int = 0,
    val birthDate: LocalDate? = null,
    val lifespanText: String = "80",
    val lifespanEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showExactDays: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(8, 30),
    val reminderTone: ReminderTone = ReminderTone.GENTLE,
    val error: String? = null
) {
    val lifespanValue: Int? get() = lifespanText.toIntOrNull()
    val birthDateValid: Boolean get() = birthDate != null && !birthDate.isAfter(LocalDate.now())
    val lifespanValid: Boolean
        get() = !lifespanEnabled || LifeCountdownCalculator.isValidLifespan(lifespanValue)
    val estimatedEndDate: LocalDate?
        get() = if (birthDate != null && lifespanValid && lifespanValue != null) {
            LifeCountdownCalculator.expectedEndDate(birthDate!!, lifespanValue!!)
        } else null
}

class OnboardingViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun setBirthDate(date: LocalDate) {
        _state.value = _state.value.copy(birthDate = date, error = null)
    }

    fun setLifespanText(value: String) {
        // 只保留数字，长度最多 3 位，避免非法输入进入计算。
        val digits = value.filter { it.isDigit() }.take(3)
        _state.value = _state.value.copy(lifespanText = digits, error = null)
    }

    fun setLifespanEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(lifespanEnabled = enabled, error = null)
    }

    fun setTheme(mode: ThemeMode) {
        _state.value = _state.value.copy(themeMode = mode)
    }

    fun setShowExactDays(show: Boolean) {
        _state.value = _state.value.copy(showExactDays = show)
    }

    fun setReminderEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(reminderEnabled = enabled)
    }

    fun setReminderTime(time: LocalTime) {
        _state.value = _state.value.copy(reminderTime = time)
    }

    fun setReminderTone(tone: ReminderTone) {
        _state.value = _state.value.copy(reminderTone = tone)
    }

    fun next() {
        val current = _state.value
        when (current.step) {
            1 -> if (!current.birthDateValid) {
                _state.value = current.copy(error = "请选择一个不晚于今天的出生日期")
                return
            }
            2 -> if (!current.lifespanValid) {
                _state.value = current.copy(
                    error = "预期寿命需在 ${LifeCountdownCalculator.MIN_LIFESPAN_YEARS}..${LifeCountdownCalculator.MAX_LIFESPAN_YEARS} 岁之间"
                )
                return
            }
        }
        _state.value = current.copy(step = (current.step + 1).coerceAtMost(LAST_STEP), error = null)
    }

    fun back() {
        _state.value = _state.value.copy(step = (_state.value.step - 1).coerceAtLeast(0), error = null)
    }

    fun finish(onDone: () -> Unit) {
        val current = _state.value
        if (!current.birthDateValid) {
            _state.value = current.copy(step = 1, error = "请选择一个不晚于今天的出生日期")
            return
        }
        viewModelScope.launch {
            container.profileStore.setBirthDate(current.birthDate!!)
            container.profileStore.setLifespan(
                if (current.lifespanEnabled) current.lifespanValue else null
            )
            container.profileStore.setCountdownEnabled(current.lifespanEnabled)
            container.profileStore.setShowExactDays(current.showExactDays)
            container.profileStore.setThemeMode(current.themeMode)
            container.profileStore.setOnboardingCompleted(true)
            container.profileStore.setReminderEnabled(current.reminderEnabled)
            container.profileStore.setReminderTime(current.reminderTime)
            container.profileStore.setReminderTone(current.reminderTone)
            if (current.reminderEnabled) {
                container.scheduleReminder(
                    com.liferemaining.app.domain.model.UserProfile(
                        birthDate = current.birthDate,
                        expectedLifespanYears = if (current.lifespanEnabled) current.lifespanValue else null,
                        countdownEnabled = current.lifespanEnabled,
                        showExactDays = current.showExactDays,
                        themeMode = current.themeMode,
                        reminderTone = current.reminderTone,
                        dailyReminderEnabled = true,
                        reminderTime = current.reminderTime,
                        onboardingCompleted = true
                    )
                )
            }
            onDone()
        }
    }

    companion object {
        const val LAST_STEP = 4
    }
}
