package com.liferemaining.app.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import com.liferemaining.app.domain.model.PeriodProgress
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.domain.model.WeekDayCell
import com.liferemaining.app.domain.model.YearNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TimelineMode(val label: String) {
    YEAR("年"),
    MONTH("月"),
    WEEK("周")
}

sealed interface TimelineRow {
    data class YearEntry(
        val year: Int,
        val state: YearState,
        val ageAtYear: Int?,
        val isCurrent: Boolean,
        val noteCount: Int
    ) : TimelineRow

    data class MonthEntry(val month: Int, val elapsed: Int, val total: Int) : TimelineRow

    data class WeekEntry(val cell: WeekDayCell) : TimelineRow
}

enum class YearState { PAST, CURRENT, FUTURE, BEYOND_LIFESPAN }

data class TimelineUiState(
    val today: LocalDate,
    val mode: TimelineMode,
    val birthDate: LocalDate?,
    val expectedLifespanYears: Int?,
    val birthYear: Int?,
    val currentAge: Int?,
    val years: List<TimelineRow.YearEntry>,
    val months: List<TimelineRow.MonthEntry>,
    val weekCells: List<WeekDayCell>,
    val yearProgress: PeriodProgress,
    val monthProgress: PeriodProgress,
    val weekProgress: PeriodProgress,
    val notesByYear: Map<Int, List<YearNote>>
)

class TimelineViewModel(private val container: AppContainer) : ViewModel() {

    private val mode = MutableStateFlow(TimelineMode.YEAR)

    val uiState: StateFlow<TimelineUiState> = combine(
        container.profileStore.profile,
        container.todayProvider.today,
        container.yearNoteRepository.notes,
        mode
    ) { profile, today, notes, currentMode ->
        build(profile, today, notes, currentMode)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        build(UserProfile(), container.todayProvider.today.value, emptyList(), TimelineMode.YEAR)
    )

    fun setMode(value: TimelineMode) {
        mode.value = value
    }

    fun addNote(year: Int, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { container.yearNoteRepository.add(year, content.trim()) }
    }

    fun deleteNote(note: YearNote) {
        viewModelScope.launch { container.yearNoteRepository.delete(note) }
    }

    private fun build(
        profile: UserProfile,
        today: LocalDate,
        notes: List<YearNote>,
        currentMode: TimelineMode
    ): TimelineUiState {
        val birth = profile.birthDate
        val lifespan = profile.expectedLifespanYears
        val birthYear = birth?.year
        val endYear = if (birth != null && lifespan != null) birth.year + lifespan else null
        val currentYear = today.year

        val years = if (birthYear == null) {
            emptyList()
        } else {
            val lastYear = maxOf(currentYear, endYear ?: currentYear)
            (birthYear..lastYear).map { year ->
                val state = when {
                    endYear != null && year > endYear -> YearState.BEYOND_LIFESPAN
                    year < currentYear -> YearState.PAST
                    year == currentYear -> YearState.CURRENT
                    else -> YearState.FUTURE
                }
                TimelineRow.YearEntry(
                    year = year,
                    state = state,
                    ageAtYear = year - birthYear,
                    isCurrent = year == currentYear,
                    noteCount = notes.count { it.year == year }
                )
            }
        }

        val notesMap = notes.groupBy { it.year }
        val monthRows = (1..12).map { month ->
            val total = today.withMonth(month).lengthOfMonth()
            val elapsed = when {
                month < today.monthValue -> total
                month > today.monthValue -> 0
                else -> today.dayOfMonth
            }
            TimelineRow.MonthEntry(month, elapsed, total)
        }

        return TimelineUiState(
            today = today,
            mode = currentMode,
            birthDate = birth,
            expectedLifespanYears = lifespan,
            birthYear = birthYear,
            currentAge = if (birth != null) {
                com.liferemaining.app.domain.calculator.LifeCountdownCalculator
                    .ageParts(birth, today).first
            } else null,
            years = years,
            months = monthRows,
            weekCells = TimeProgressCalculator.weekDays(today),
            yearProgress = TimeProgressCalculator.yearProgress(today),
            monthProgress = TimeProgressCalculator.monthProgress(today),
            weekProgress = TimeProgressCalculator.weekProgress(today),
            notesByYear = notesMap
        )
    }
}
