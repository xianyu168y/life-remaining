package com.liferemaining.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.ReminderTone
import com.liferemaining.app.domain.model.ThemeMode
import com.liferemaining.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

/**
 * 偏好设置存储。只放轻量配置，不放目标/记录等业务数据。
 * 出生日期为空表示「还没设置」，不会用默认生日冒充真实用户。
 */
class ProfileStore(private val context: Context) {

    private object Keys {
        val BIRTH_DATE = stringPreferencesKey("birth_date")
        val LIFESPAN = intPreferencesKey("lifespan_years")
        val COUNTDOWN_ENABLED = booleanPreferencesKey("countdown_enabled")
        val SHOW_EXACT_DAYS = booleanPreferencesKey("show_exact_days")
        val THEME = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent_key")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val REMINDER_TONE = stringPreferencesKey("reminder_tone")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
    }

    val profile: Flow<UserProfile> = context.profileDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { prefs -> prefs.toProfile() }

    private fun Preferences.toProfile(): UserProfile {
        val birth = this[Keys.BIRTH_DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val lifespan = this[Keys.LIFESPAN]?.takeIf { it in 1..120 }
        val time = this[Keys.REMINDER_TIME]?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        return UserProfile(
            birthDate = birth,
            expectedLifespanYears = lifespan,
            countdownEnabled = this[Keys.COUNTDOWN_ENABLED] ?: true,
            showExactDays = this[Keys.SHOW_EXACT_DAYS] ?: true,
            themeMode = ThemeMode.fromStorage(this[Keys.THEME]),
            accentKey = AccentKey.fromStorage(this[Keys.ACCENT]),
            dynamicColor = this[Keys.DYNAMIC_COLOR] ?: false,
            dailyReminderEnabled = this[Keys.REMINDER_ENABLED] ?: false,
            reminderTime = time ?: LocalTime.of(8, 30),
            reminderTone = this[Keys.REMINDER_TONE]?.let { raw ->
                ReminderTone.entries.firstOrNull { it.name == raw }
            } ?: ReminderTone.GENTLE,
            onboardingCompleted = this[Keys.ONBOARDING_DONE] ?: false
        )
    }

    suspend fun setBirthDate(date: LocalDate) {
        context.profileDataStore.edit { it[Keys.BIRTH_DATE] = date.toString() }
    }

    suspend fun setLifespan(years: Int?) {
        context.profileDataStore.edit { prefs ->
            if (years == null) prefs.remove(Keys.LIFESPAN) else prefs[Keys.LIFESPAN] = years
        }
    }

    suspend fun setCountdownEnabled(enabled: Boolean) {
        context.profileDataStore.edit { it[Keys.COUNTDOWN_ENABLED] = enabled }
    }

    suspend fun setShowExactDays(show: Boolean) {
        context.profileDataStore.edit { it[Keys.SHOW_EXACT_DAYS] = show }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.profileDataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setAccent(key: AccentKey) {
        context.profileDataStore.edit { it[Keys.ACCENT] = key.storageKey }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.profileDataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.profileDataStore.edit { it[Keys.REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(time: LocalTime) {
        context.profileDataStore.edit { it[Keys.REMINDER_TIME] = time.toString() }
    }

    suspend fun setReminderTone(tone: ReminderTone) {
        context.profileDataStore.edit { it[Keys.REMINDER_TONE] = tone.name }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.profileDataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    /** 清除全部偏好。调用方负责清理业务数据与通知调度。 */
    suspend fun clearAll() {
        context.profileDataStore.edit { it.clear() }
    }

    /** 导入备份时整体覆盖偏好。 */
    suspend fun overwrite(profile: UserProfile) {
        context.profileDataStore.edit { prefs ->
            prefs.clear()
            profile.birthDate?.let { prefs[Keys.BIRTH_DATE] = it.toString() }
            profile.expectedLifespanYears?.let { prefs[Keys.LIFESPAN] = it }
            prefs[Keys.COUNTDOWN_ENABLED] = profile.countdownEnabled
            prefs[Keys.SHOW_EXACT_DAYS] = profile.showExactDays
            prefs[Keys.THEME] = profile.themeMode.name
            prefs[Keys.ACCENT] = profile.accentKey.storageKey
            prefs[Keys.DYNAMIC_COLOR] = profile.dynamicColor
            prefs[Keys.REMINDER_ENABLED] = profile.dailyReminderEnabled
            prefs[Keys.REMINDER_TIME] = profile.reminderTime.toString()
            prefs[Keys.REMINDER_TONE] = profile.reminderTone.name
            prefs[Keys.ONBOARDING_DONE] = profile.onboardingCompleted
        }
    }
}
