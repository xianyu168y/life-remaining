package com.liferemaining.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.GoalCategory
import com.liferemaining.app.domain.model.GoalPriority
import com.liferemaining.app.domain.model.LifeGoal
import com.liferemaining.app.domain.model.Milestone
import com.liferemaining.app.domain.model.MilestoneCategory
import com.liferemaining.app.domain.model.ThemeMode
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.domain.calculator.MilestoneCalculator
import com.liferemaining.app.ui.dashboard.CountdownCardState
import com.liferemaining.app.ui.dashboard.DashboardContent
import com.liferemaining.app.ui.dashboard.DashboardUiState
import com.liferemaining.app.ui.theme.LifeRemainingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * 在真实设备/模拟器上渲染首页并验证能成功产生画面。
 *
 * 只断言「captureToImage 返回非空位图」，不做像素比较（不同 AVD 字体渲染有差异）。
 * 它覆盖 Compose 在真机上的实际绘制路径，能抓到纯 JVM 测试发现不了的问题。
 */
@RunWith(AndroidJUnit4::class)
class RenderSmokeTest {

    @get:Rule
    val rule = createComposeRule()

    private val today = LocalDate.of(2026, 10, 26)
    private val birth = LocalDate.of(1990, 5, 15)

    private val profile = UserProfile(
        birthDate = birth,
        expectedLifespanYears = 80,
        onboardingCompleted = true
    )

    private fun state(): DashboardUiState {
        val countdown = LifeCountdownCalculator.calculate(birth, today, 80)
        val goals = listOf(
            LifeGoal(
                id = 1,
                title = "写完一本书",
                description = "哪怕只有三万字",
                category = GoalCategory.CAREER,
                createdAt = LocalDate.of(2026, 3, 1),
                dueDate = LocalDate.of(2027, 1, 2),
                priority = GoalPriority.HIGH,
                isImportant = true
            ),
            LifeGoal(
                id = 2,
                title = "陪父母去一次海边",
                category = GoalCategory.FAMILY,
                createdAt = LocalDate.of(2026, 5, 1),
                dueDate = LocalDate.of(2026, 12, 20)
            )
        )
        val milestones = listOf(
            Milestone(
                id = 1,
                title = "给朋友的生日",
                date = LocalDate.of(1990, 11, 8),
                category = MilestoneCategory.BIRTHDAY,
                repeatsAnnually = true
            ),
            Milestone(
                id = 2,
                title = "冬天回家",
                date = LocalDate.of(2026, 12, 24),
                category = MilestoneCategory.TRAVEL
            )
        )
        return DashboardUiState(
            today = today,
            profile = profile,
            countdown = countdown,
            cardState = CountdownCardState.ACTIVE,
            stats = TimeProgressCalculator.stats(today, birth),
            activeGoals = goals,
            upcomingMilestones = MilestoneCalculator.upcoming(milestones, today, limit = 3),
            actionPrompt = "给重要的人打一个电话。"
        )
    }

    @Test
    fun rendersDarkHome() = render(ThemeMode.DARK, AccentKey.TEAL)

    @Test
    fun rendersLightHome() = render(ThemeMode.LIGHT, AccentKey.TEAL)

    @Test
    fun rendersLightMoss() = render(ThemeMode.LIGHT, AccentKey.MOSS)

    @Test
    fun rendersEmptyState() {
        val empty = UserProfile(onboardingCompleted = true)
        val countdown = LifeCountdownCalculator.calculate(null, today, null)
        val state = DashboardUiState(
            today = today,
            profile = empty,
            countdown = countdown,
            cardState = CountdownCardState.NOT_CONFIGURED,
            stats = TimeProgressCalculator.stats(today, null),
            activeGoals = emptyList(),
            upcomingMilestones = emptyList(),
            actionPrompt = "今天结束前，记录一件值得记住的事。"
        )
        rule.setContent {
            LifeRemainingTheme(themeMode = ThemeMode.DARK, accentKey = AccentKey.TEAL) {
                Box(Modifier.fillMaxSize()) {
                    DashboardContent(
                        state = state,
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
        assertRendered()
    }

    private fun render(theme: ThemeMode, accent: AccentKey) {
        val uiState = state()
        rule.setContent {
            LifeRemainingTheme(themeMode = theme, accentKey = accent) {
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
        assertRendered()
    }

    private fun assertRendered() {
        rule.waitForIdle()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        org.junit.Assert.assertTrue("render width > 0", bitmap.width > 0)
        org.junit.Assert.assertTrue("render height > 0", bitmap.height > 0)
    }
}
