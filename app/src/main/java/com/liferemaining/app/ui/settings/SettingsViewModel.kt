package com.liferemaining.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.data.backup.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 导入/导出的结果提示。null 表示无提示。 */
data class BackupMessage(val text: String, val isError: Boolean)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val _message = MutableStateFlow<BackupMessage?>(null)
    val message: StateFlow<BackupMessage?> = _message.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun hasNotificationPermission(): Boolean = container.hasNotificationPermission()

    /** 权限授予后重新排程；被拦下时给出明确提示。 */
    fun resyncReminder() {
        viewModelScope.launch {
            val profile = container.profileStore.profile.first()
            val ok = container.scheduleReminder(profile)
            if (profile.dailyReminderEnabled && !ok) {
                _message.value = BackupMessage(
                    "通知权限未开启，提醒不会送达。请在系统设置中允许通知。",
                    isError = true
                )
            }
        }
    }

    fun cancelReminder() {
        container.cancelReminder()
    }

    suspend fun buildExportJson(): String = with(Dispatchers.IO) {
        container.backupService.exportToJson()
    }

    fun import(raw: String) {
        _busy.value = true
        viewModelScope.launch {
            val result = with(Dispatchers.IO) { container.backupService.importFromJson(raw) }
            _busy.value = false
            _message.value = when (result) {
                is ImportResult.Success -> BackupMessage(
                    "导入完成：${result.goals} 个目标、${result.milestones} 个日期、${result.reflections} 条记录、${result.yearNotes} 条年度回忆。",
                    isError = false
                )
                is ImportResult.Failure -> BackupMessage(result.reason, isError = true)
            }
        }
    }

    fun clearAll(onDone: () -> Unit) {
        _busy.value = true
        viewModelScope.launch {
            container.clearEverything()
            container.cancelReminder()
            _busy.value = false
            _message.value = BackupMessage("本机数据已全部清除。", isError = false)
            onDone()
        }
    }

    fun reportExportSaved(fileName: String) {
        _message.value = BackupMessage("已导出到 $fileName", isError = false)
    }

    fun reportError(text: String) {
        _message.value = BackupMessage(text, isError = true)
    }
}
