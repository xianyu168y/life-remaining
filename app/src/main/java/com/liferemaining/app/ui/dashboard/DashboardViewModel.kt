package com.liferemaining.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.calculator.MilestoneCalculator
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import com.liferemaining.app.domain.model.LifeCountdown
import com.liferemaining.app.domain.model.LifeGoal
import com.liferemaining.app.domain.model.Milestone
import com.liferemaining.app.domain.model.TimeStats
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.util.ActionPrompts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

enum class CountdownCardState { NOT_CONFIGURED, HIDDEN, ACTIVE, REACHED_END }

data class DashboardUiState(
    val today: LocalDate,
    val profile: UserProfile,
    val countdown: LifeCountdown,
    val cardState: CountdownCardState,
    val stats: TimeStats,
    val activeGoals: List<LifeGoal>,
    val upcomingMilestones: List<MilestoneCalculator.Occurrence>,
    val actionPrompt: String
)

class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    private val shuffle = MutableStateFlow(0)

    val uiState: StateFlow<DashboardUiState> = combine(
        container.profileStore.profile,
        container.todayProvider.today,
        container.goalRepository.goals,
        container.milestoneRepository.milestones,
        shuffle
    ) { profile, today, goals, milestones, shuffleCount ->
        build(profile, today, goals, milestones, shuffleCount)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        build(
            UserProfile(),
            container.todayProvider.today.value,
            emptyList(),
            emptyList(),
            0
        )
    )

    fun nextPrompt() {
        shuffle.value = shuffle.value + 1
    }

    fun refresh() {
        container.todayProvider.refresh()
    }

    private fun build(
        profile: UserProfile,
        today: LocalDate,
        goals: List<LifeGoal>,
        milestones: List<Milestone>,
        shuffleCount: Int
    ): DashboardUiState {
        val countdown = LifeCountdownCalculator.calculate(
            birthDate = profile.birthDate,
            today = today,
            lifespanYears = profile.expectedLifespanYears
        )

        val cardState = when {
            !profile.hasBirthDate || !profile.hasLifespan -> CountdownCardState.NOT_CONFIGURED
            !profile.countdownEnabled -> CountdownCardState.HIDDEN
            countdown.reachedEnd -> CountdownCardState.REACHED_END
            else -> CountdownCardState.ACTIVE
        }

        return DashboardUiState(
            today = today,
            profile = profile,
            countdown = countdown,
            cardState = cardState,
            stats = TimeProgressCalculator.stats(today, profile.birthDate),
            activeGoals = goals.filter { !it.isCompleted },
            upcomingMilestones = MilestoneCalculator.upcoming(milestones, today, limit = 3),
            actionPrompt = ActionPrompts.promptFor(today.dayOfYear, shuffleCount)
        )
    }
}
