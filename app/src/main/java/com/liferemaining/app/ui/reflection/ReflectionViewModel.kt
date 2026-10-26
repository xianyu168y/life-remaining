package com.liferemaining.app.ui.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.model.DailyReflection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ReflectionEditorState(
    val date: LocalDate,
    val draft: String,
    val existing: DailyReflection?,
    val saving: Boolean,
    val savedAt: String?,
    val error: String?
)

data class ReflectionHistoryState(
    val entries: List<DailyReflection>,
    val today: LocalDate
)

class ReflectionEditorViewModel(
    private val container: AppContainer,
    private val date: LocalDate
) : ViewModel() {

    private val draft = MutableStateFlow("")
    private val saving = MutableStateFlow(false)
    private val savedAt = MutableStateFlow<String?>(null)
    private val error = MutableStateFlow<String?>(null)

    private val existing: StateFlow<DailyReflection?> = container.reflectionRepository
        .observe(date)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val state: StateFlow<ReflectionEditorState> = combine(
        existing,
        draft,
        saving,
        combine(savedAt, error) { s, e -> s to e }
    ) { current, draftValue, savingValue, extra ->
        ReflectionEditorState(
            date = date,
            draft = draftValue,
            existing = current,
            saving = savingValue,
            savedAt = extra.first,
            error = extra.second
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReflectionEditorState(date, "", null, false, null, null)
    )

    fun updateDraft(value: String) {
        draft.value = value
        error.value = null
        savedAt.value = null
    }

    fun loadExistingIntoDraft(reflection: DailyReflection?) {
        if (reflection != null && draft.value.isEmpty()) {
            draft.value = reflection.content
        }
    }

    fun save() {
        if (draft.value.isBlank()) {
            error.value = "内容不能为空"
            return
        }
        saving.value = true
        viewModelScope.launch {
            runCatching {
                container.reflectionRepository.save(date, draft.value.trim())
            }.onSuccess {
                saving.value = false
                savedAt.value = "已保存在本机"
            }.onFailure {
                saving.value = false
                error.value = it.message ?: "保存失败"
            }
        }
    }

    fun delete() {
        val target = existing.value ?: return
        viewModelScope.launch {
            container.reflectionRepository.delete(target)
            draft.value = ""
            savedAt.value = "已删除"
        }
    }
}

class ReflectionHistoryViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<ReflectionHistoryState> = combine(
        container.reflectionRepository.reflections,
        container.todayProvider.today
    ) { entries, today ->
        ReflectionHistoryState(entries, today)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReflectionHistoryState(emptyList(), container.todayProvider.today.value)
    )
}
