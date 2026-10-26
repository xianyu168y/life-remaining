package com.liferemaining.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.ReminderTone
import com.liferemaining.app.domain.model.ThemeMode
import com.liferemaining.app.domain.model.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * 根级 ViewModel：只负责用户参数与偏好，以及「今天」。
 * 各业务页面自己持有列表状态，避免一个巨型 ViewModel。
 */
class AppViewModel(private val container: AppContainer) : ViewModel() {

    /** null 表示 DataStore 尚未读出第一帧，用于避免 Onboarding 与首页闪烁。 */
    val profile: StateFlow<UserProfile?> = container.profileStore.profile
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val today: StateFlow<LocalDate> = container.todayProvider.today

    fun refreshToday() = container.todayProvider.refresh()

    fun setBirthDate(date: LocalDate) = viewModelScope.launch {
        container.profileStore.setBirthDate(date)
    }

    fun setLifespan(years: Int?) = viewModelScope.launch {
        container.profileStore.setLifespan(years)
    }

    fun setCountdownEnabled(enabled: Boolean) = viewModelScope.launch {
        container.profileStore.setCountdownEnabled(enabled)
    }

    fun setShowExactDays(show: Boolean) = viewModelScope.launch {
        container.profileStore.setShowExactDays(show)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        container.profileStore.setThemeMode(mode)
    }

    fun setAccent(key: AccentKey) = viewModelScope.launch {
        container.profileStore.setAccent(key)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        container.profileStore.setDynamicColor(enabled)
    }

    fun setReminderEnabled(enabled: Boolean) = viewModelScope.launch {
        container.profileStore.setReminderEnabled(enabled)
        syncReminder()
    }

    fun setReminderTime(time: LocalTime) = viewModelScope.launch {
        container.profileStore.setReminderTime(time)
        syncReminder()
    }

    fun setReminderTone(tone: ReminderTone) = viewModelScope.launch {
        container.profileStore.setReminderTone(tone)
        syncReminder()
    }

    fun resyncReminder() = viewModelScope.launch {
        val updated = container.profileStore.profile.first()
        container.scheduleReminder(updated)
    }

    private suspend fun syncReminder() {
        val updated = container.profileStore.profile.first()
        container.scheduleReminder(updated)
    }

    fun completeOnboarding() = viewModelScope.launch {
        container.profileStore.setOnboardingCompleted(true)
    }

    fun clearAllData(onDone: () -> Unit = {}) = viewModelScope.launch {
        container.clearEverything()
        container.cancelReminder()
        onDone()
    }
}
