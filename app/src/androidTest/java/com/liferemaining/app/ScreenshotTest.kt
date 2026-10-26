package com.liferemaining.app

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

/**
 * 在真实设备/模拟器上渲染首页并导出 PNG。
 *
 * 这不是断言型测试：它的价值是把「真机渲染结果」留成构建产物，
 * 便于在无法直接看设备屏幕的环境里核对排版。
 * CI 会在 connectedDebugAndroidTest 之后 adb pull 这些文件并上传 artifact。
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotTest {

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
    fun captureHomeVariants() {
        capture(ThemeMode.DARK, AccentKey.TEAL, "home_dark_teal.png")
    }

    @Test
    fun captureLightHome() {
        capture(ThemeMode.LIGHT, AccentKey.TEAL, "home_light_teal.png")
    }

    @Test
    fun captureLightMoss() {
        capture(ThemeMode.LIGHT, AccentKey.MOSS, "home_light_moss.png")
    }

    @Test
    fun captureEmptyState() {
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
        save("home_empty_state.png")
    }

    private fun capture(theme: ThemeMode, accent: AccentKey, name: String) {
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
        save(name)
    }

    private fun save(name: String) {
        rule.waitForIdle()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        // 写到应用内部 files/，CI 用 `adb exec-out run-as <debug包> cat` 拉取；
        // Android 11+ 对 /sdcard/Android/data 的限制会让 adb pull 不稳定。
        val dir = File(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "screenshots"
        )
        dir.mkdirs()
        FileOutputStream(File(dir, name)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
