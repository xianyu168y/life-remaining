package com.liferemaining.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.ui.dashboard.CountdownCardState
import com.liferemaining.app.ui.dashboard.DashboardContent
import com.liferemaining.app.ui.dashboard.DashboardUiState
import com.liferemaining.app.ui.theme.LifeRemainingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DashboardUiTest {

    @get:Rule
    val rule = createComposeRule()

    private val today = LocalDate.of(2026, 10, 26)
    private val birth = LocalDate.of(1990, 5, 15)

    private fun state(profile: UserProfile): DashboardUiState {
        val countdown = LifeCountdownCalculator.calculate(
            profile.birthDate, today, profile.expectedLifespanYears
        )
        return DashboardUiState(
            today = today,
            profile = profile,
            countdown = countdown,
            cardState = when {
                !profile.hasBirthDate || !profile.hasLifespan -> CountdownCardState.NOT_CONFIGURED
                !profile.countdownEnabled -> CountdownCardState.HIDDEN
                countdown.reachedEnd -> CountdownCardState.REACHED_END
                else -> CountdownCardState.ACTIVE
            },
            stats = TimeProgressCalculator.stats(today, profile.birthDate),
            activeGoals = emptyList(),
            upcomingMilestones = emptyList(),
            actionPrompt = "给重要的人打一个电话。"
        )
    }

    @Test
    fun dashboardShowsRemainingDaysAndProgress() {
        val profile = UserProfile(
            birthDate = birth,
            expectedLifespanYears = 80,
            onboardingCompleted = true,
            accentKey = AccentKey.TEAL
        )
        val uiState = state(profile)
        rule.setContent {
            LifeRemainingTheme {
                Box(Modifier.fillMaxSize()) {
                    DashboardContent(
                        state = uiState,
                        onRefresh = {},
                        onNextPrompt = {},
                        onOpenSettings = {},
                        onOpenProfileSetup = {},
                        onOpenPeriod = {},
                        onOpenGoal = {},
                        onOpenMilestone = {},
                        onOpenMilestones = {},
                        onOpenGoals = {},
                        onRecordToday = {}
                    )
                }
            }
        }

        rule.onNodeWithText("LIFE REMAINING").assertIsDisplayed()
        rule.onNodeWithText("预计剩余天数").assertIsDisplayed()
        rule.onNodeWithText("生命进度").assertIsDisplayed()
        rule.onNodeWithText("预计终点").assertIsDisplayed()
    }

    @Test
    fun dashboardScrollsToBottomSections() {
        val profile = UserProfile(
            birthDate = birth,
            expectedLifespanYears = 80,
            onboardingCompleted = true
        )
        rule.setContent {
            LifeRemainingTheme {
                Box(Modifier.fillMaxSize()) {
                    DashboardContent(
                        state = state(profile),
                        onRefresh = {},
                        onNextPrompt = {},
                        onOpenSettings = {},
                        onOpenProfileSetup = {},
                        onOpenPeriod = {},
                        onOpenGoal = {},
                        onOpenMilestone = {},
                        onOpenMilestones = {},
                        onOpenGoals = {},
                        onRecordToday = {}
                    )
                }
            }
        }

        rule.onNodeWithText("接下来值得期待").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("进行中的目标").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun emptyStateGuidesToSetupAndClickWorks() {
        var clicked = false
        rule.setContent {
            LifeRemainingTheme {
                Box(Modifier.fillMaxSize()) {
                    DashboardContent(
                        state = state(UserProfile(onboardingCompleted = true)),
                        onRefresh = {},
                        onNextPrompt = {},
                        onOpenSettings = {},
                        onOpenProfileSetup = { clicked = true },
                        onOpenPeriod = {},
                        onOpenGoal = {},
                        onOpenMilestone = {},
                        onOpenMilestones = {},
                        onOpenGoals = {},
                        onRecordToday = {}
                    )
                }
            }
        }

        rule.onNodeWithText("还没有设置生命参数").assertIsDisplayed()
        rule.onNodeWithText("设置生命参数").performScrollTo().performClick()
        assertTrue(clicked)
    }

    @Test
    fun hiddenCountdownShowsMaskedValue() {
        val profile = UserProfile(
            birthDate = birth,
            expectedLifespanYears = 80,
            showExactDays = false,
            onboardingCompleted = true
        )
        rule.setContent {
            LifeRemainingTheme {
                Box(Modifier.fillMaxSize()) {
                    DashboardContent(
                        state = state(profile),
                        onRefresh = {},
                        onNextPrompt = {},
                        onOpenSettings = {},
                        onOpenProfileSetup = {},
                        onOpenPeriod = {},
                        onOpenGoal = {},
                        onOpenMilestone = {},
                        onOpenMilestones = {},
                        onOpenGoals = {},
                        onRecordToday = {}
                    )
                }
            }
        }

        rule.onNodeWithText("精确天数已隐藏").assertIsDisplayed()
    }
}
