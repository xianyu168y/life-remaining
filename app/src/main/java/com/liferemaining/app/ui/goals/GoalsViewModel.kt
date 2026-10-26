package com.liferemaining.app.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.model.GoalCategory
import com.liferemaining.app.domain.model.GoalPriority
import com.liferemaining.app.domain.model.GoalStatus
import com.liferemaining.app.domain.model.LifeGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class GoalFilter(val label: String) {
    ALL("全部"),
    ACTIVE("进行中"),
    COMPLETED("已完成")
}

enum class GoalSort(val label: String) {
    DUE_DATE("按截止日期"),
    CREATED("按创建时间")
}

data class GoalsUiState(
    val goals: List<LifeGoal>,
    val filter: GoalFilter,
    val sort: GoalSort,
    val today: LocalDate
)

class GoalsViewModel(private val container: AppContainer) : ViewModel() {

    private val filter = MutableStateFlow(GoalFilter.ALL)
    private val sort = MutableStateFlow(GoalSort.CREATED)

    val uiState: StateFlow<GoalsUiState> = combine(
        container.goalRepository.goals,
        container.todayProvider.today,
        filter,
        sort
    ) { goals, today, currentFilter, currentSort ->
        val filtered = when (currentFilter) {
            GoalFilter.ALL -> goals
            GoalFilter.ACTIVE -> goals.filter { !it.isCompleted }
            GoalFilter.COMPLETED -> goals.filter { it.isCompleted }
        }
        val sorted = when (currentSort) {
            GoalSort.CREATED -> filtered.sortedByDescending { it.createdAt }
            GoalSort.DUE_DATE -> filtered.sortedWith(
                compareBy<LifeGoal> { it.dueDate ?: LocalDate.MAX }
                    .thenByDescending { it.createdAt }
            )
        }
        GoalsUiState(sorted, currentFilter, currentSort, today)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        GoalsUiState(emptyList(), GoalFilter.ALL, GoalSort.CREATED, container.todayProvider.today.value)
    )

    fun setFilter(value: GoalFilter) { filter.value = value }

    fun setSort(value: GoalSort) { sort.value = value }

    fun toggleCompleted(goal: LifeGoal) {
        viewModelScope.launch {
            val next = if (goal.isCompleted) GoalStatus.ACTIVE else GoalStatus.COMPLETED
            container.goalRepository.update(goal.copy(status = next))
        }
    }

    fun delete(goal: LifeGoal) {
        viewModelScope.launch { container.goalRepository.delete(goal) }
    }
}

data class GoalEditorState(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val category: GoalCategory = GoalCategory.OTHER,
    val dueDate: LocalDate? = null,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val isImportant: Boolean = false,
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null
)

class GoalEditorViewModel(
    private val container: AppContainer,
    private val goalId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(GoalEditorState())
    val state: StateFlow<GoalEditorState> = _state

    init {
        if (goalId != null && goalId > 0) {
            viewModelScope.launch {
                val goal = container.goalRepository.find(goalId)
                if (goal == null) {
                    _state.value = _state.value.copy(loaded = true, error = "目标不存在或已被删除")
                } else {
                    _state.value = GoalEditorState(
                        id = goal.id,
                        title = goal.title,
                        description = goal.description,
                        category = goal.category,
                        dueDate = goal.dueDate,
                        priority = goal.priority,
                        isImportant = goal.isImportant,
                        loaded = true
                    )
                }
            }
        } else {
            _state.value = _state.value.copy(loaded = true)
        }
    }

    fun updateTitle(value: String) { _state.value = _state.value.copy(title = value, error = null) }
    fun updateDescription(value: String) { _state.value = _state.value.copy(description = value) }
    fun updateCategory(value: GoalCategory) { _state.value = _state.value.copy(category = value) }
    fun updateDueDate(value: LocalDate?) { _state.value = _state.value.copy(dueDate = value) }
    fun updatePriority(value: GoalPriority) { _state.value = _state.value.copy(priority = value) }
    fun updateImportant(value: Boolean) { _state.value = _state.value.copy(isImportant = value) }

    fun save(onDone: () -> Unit) {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "标题不能为空")
            return
        }
        _state.value = current.copy(saving = true, error = null)
        viewModelScope.launch {
            runCatching {
                if (current.id > 0) {
                    val existing = container.goalRepository.find(current.id)
                        ?: throw IllegalStateException("目标已不存在")
                    container.goalRepository.update(
                        existing.copy(
                            title = current.title.trim(),
                            description = current.description.trim(),
                            category = current.category,
                            dueDate = current.dueDate,
                            priority = current.priority,
                            isImportant = current.isImportant
                        )
                    )
                } else {
                    container.goalRepository.add(
                        LifeGoal(
                            title = current.title.trim(),
                            description = current.description.trim(),
                            category = current.category,
                            createdAt = container.todayProvider.today.value,
                            dueDate = current.dueDate,
                            priority = current.priority,
                            isImportant = current.isImportant
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

data class GoalDetailUiState(
    val goal: LifeGoal?,
    val today: LocalDate,
    val loading: Boolean,
    val missing: Boolean
)

class GoalDetailViewModel(
    private val container: AppContainer,
    goalId: Long
) : ViewModel() {

    private val id = goalId
    private val _state = MutableStateFlow(
        GoalDetailUiState(null, container.todayProvider.today.value, true, false)
    )
    val state: StateFlow<GoalDetailUiState> = _state

    init {
        viewModelScope.launch {
            container.goalRepository.goals.collect { goals ->
                val goal = goals.firstOrNull { it.id == id }
                _state.value = GoalDetailUiState(
                    goal = goal,
                    today = container.todayProvider.today.value,
                    loading = false,
                    missing = goal == null
                )
            }
        }
    }

    fun toggleCompleted() {
        val goal = _state.value.goal ?: return
        viewModelScope.launch {
            val next = if (goal.isCompleted) GoalStatus.ACTIVE else GoalStatus.COMPLETED
            container.goalRepository.update(goal.copy(status = next))
        }
    }

    fun delete(onDone: () -> Unit) {
        val goal = _state.value.goal ?: return
        viewModelScope.launch {
            container.goalRepository.delete(goal)
            onDone()
        }
    }
}
