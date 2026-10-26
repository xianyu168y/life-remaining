package com.liferemaining.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.ThemeMode
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

/**
 * 主题 × 强调色 × 屏幕宽度的排版回归：只断言关键文案仍可见（未因溢出被裁掉），
 * 不比较像素，避免不同 AVD 字体渲染差异导致误报。
 */
@RunWith(AndroidJUnit4::class)
class LocalizationTest {

    @get:Rule
    val rule = createComposeRule()

    private val today = LocalDate.of(2026, 10, 26)
    private val profile = UserProfile(
        birthDate = LocalDate.of(1990, 5, 15),
        expectedLifespanYears = 80,
        onboardingCompleted = true
    )

    private fun state(): DashboardUiState {
        val countdown = LifeCountdownCalculator.calculate(
            profile.birthDate, today, profile.expectedLifespanYears
        )
        return DashboardUiState(
            today = today,
            profile = profile,
            countdown = countdown,
            cardState = CountdownCardState.ACTIVE,
            stats = TimeProgressCalculator.stats(today, profile.birthDate),
            activeGoals = emptyList(),
            upcomingMilestones = emptyList(),
            actionPrompt = "给重要的人打一个电话。"
        )
    }

    private fun assertRenders(theme: ThemeMode, accent: AccentKey) {
        var settingsClicked = false
        rule.setContent {
            LifeRemainingTheme(themeMode = theme, accentKey = accent) {
                DashboardContent(
                    state = state(),
                    onRefresh = {},
                    onNextPrompt = {},
                    onOpenSettings = { settingsClicked = true },
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

        rule.onNodeWithText("预计剩余天数").assertIsDisplayed()
        rule.onNodeWithText("时间统计").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("今天值得做的事").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("换一句").performScrollTo().assertIsDisplayed()
        assertTrue(!settingsClicked)
    }

    @Test
    fun darkThemeWithTeal() = assertRenders(ThemeMode.DARK, AccentKey.TEAL)

    @Test
    fun lightThemeWithMoss() = assertRenders(ThemeMode.LIGHT, AccentKey.MOSS)

    @Test
    fun lightThemeWithBlue() = assertRenders(ThemeMode.LIGHT, AccentKey.BLUE)

    @Test
    fun darkThemeWithAmber() = assertRenders(ThemeMode.DARK, AccentKey.AMBER)
}
