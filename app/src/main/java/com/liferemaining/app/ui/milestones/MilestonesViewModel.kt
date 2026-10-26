package com.liferemaining.app.ui.milestones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.calculator.MilestoneCalculator
import com.liferemaining.app.domain.model.Milestone
import com.liferemaining.app.domain.model.MilestoneCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MilestoneItem(
    val milestone: Milestone,
    val nextDate: LocalDate,
    val daysUntil: Long,
    val isPast: Boolean
)

data class MilestonesUiState(
    val today: LocalDate,
    val upcoming: List<MilestoneItem>,
    val past: List<MilestoneItem>
)

class MilestonesViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<MilestonesUiState> = combine(
        container.milestoneRepository.milestones,
        container.todayProvider.today
    ) { milestones, today ->
        val occurrences = milestones.map { MilestoneCalculator.nextOccurrence(it, today) }
        val upcoming = occurrences
            .filter { !it.isPast }
            .sortedWith(compareBy({ it.daysUntil }, { it.milestone.title }))
            .map { MilestoneItem(it.milestone, it.nextDate, it.daysUntil, false) }
        val past = occurrences
            .filter { it.isPast }
            .sortedByDescending { it.nextDate }
            .map { MilestoneItem(it.milestone, it.nextDate, it.daysUntil, true) }
        MilestonesUiState(today, upcoming, past)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MilestonesUiState(container.todayProvider.today.value, emptyList(), emptyList())
    )

    fun delete(milestone: Milestone) {
        viewModelScope.launch { container.milestoneRepository.delete(milestone) }
    }
}

data class MilestoneEditorState(
    val id: Long = 0L,
    val title: String = "",
    val date: LocalDate? = null,
    val category: MilestoneCategory = MilestoneCategory.OTHER,
    val note: String = "",
    val repeatsAnnually: Boolean = false,
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null
)

class MilestoneEditorViewModel(
    private val container: AppContainer,
    private val milestoneId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(MilestoneEditorState())
    val state: StateFlow<MilestoneEditorState> = _state

    init {
        if (milestoneId != null && milestoneId > 0) {
            viewModelScope.launch {
                val item = container.milestoneRepository.find(milestoneId)
                if (item == null) {
                    _state.value = _state.value.copy(loaded = true, error = "重要日期不存在或已被删除")
                } else {
                    _state.value = MilestoneEditorState(
                        id = item.id,
                        title = item.title,
                        date = item.date,
                        category = item.category,
                        note = item.note,
                        repeatsAnnually = item.repeatsAnnually,
                        loaded = true
                    )
                }
            }
        } else {
            _state.value = _state.value.copy(loaded = true)
        }
    }

    fun updateTitle(value: String) { _state.value = _state.value.copy(title = value, error = null) }
    fun updateDate(value: LocalDate) { _state.value = _state.value.copy(date = value, error = null) }
    fun updateCategory(value: MilestoneCategory) { _state.value = _state.value.copy(category = value) }
    fun updateNote(value: String) { _state.value = _state.value.copy(note = value) }
    fun updateRepeat(value: Boolean) { _state.value = _state.value.copy(repeatsAnnually = value) }

    fun save(onDone: () -> Unit) {
        val current = _state.value
        when {
            current.title.isBlank() -> {
                _state.value = current.copy(error = "标题不能为空")
                return
            }
            current.date == null -> {
                _state.value = current.copy(error = "请选择日期")
                return
            }
        }
        _state.value = current.copy(saving = true, error = null)
        viewModelScope.launch {
            runCatching {
                if (current.id > 0) {
                    val existing = container.milestoneRepository.find(current.id)
                        ?: throw IllegalStateException("重要日期已不存在")
                    container.milestoneRepository.update(
                        existing.copy(
                            title = current.title.trim(),
                            date = current.date!!,
                            category = current.category,
                            note = current.note.trim(),
                            repeatsAnnually = current.repeatsAnnually
                        )
                    )
                } else {
                    container.milestoneRepository.add(
                        Milestone(
                            title = current.title.trim(),
                            date = current.date!!,
                            category = current.category,
                            note = current.note.trim(),
                            repeatsAnnually = current.repeatsAnnually
                        )
                    )
                }
            }.onSuccess {
                _state.value = _state.value.copy(saving = false)
                onDone()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    saving = false,
                    error = error.message ?: "保存失败"
                )
            }
        }
    }
}
