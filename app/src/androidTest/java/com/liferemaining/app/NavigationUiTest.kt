package com.liferemaining.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * 端到端导航测试。
 *
 * 用 createEmptyComposeRule + 手动 ActivityScenario：每个用例都能在 Activity 启动前
 * 写好 DataStore，让 startDestination 完全确定，不受 ViewModel 跨 recreate 保留旧值的影响。
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val rule = createEmptyComposeRule()

    private val app: LifeRemainingApplication
        get() = ApplicationProvider.getApplicationContext()

    private var scenario: ActivityScenario<MainActivity>? = null

    @After
    fun tearDown() {
        scenario?.close()
    }

    private fun resetData() = runBlocking {
        app.container.clearEverything()
    }

    private fun seedCompletedProfile() = runBlocking {
        app.container.clearEverything()
        app.container.profileStore.setBirthDate(LocalDate.of(1990, 5, 15))
        app.container.profileStore.setLifespan(80)
        app.container.profileStore.setOnboardingCompleted(true)
    }

    private fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        rule.waitForIdle()
    }

    private fun waitFor(text: String) {
        rule.waitUntil(timeoutMillis = 15_000) {
            rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun firstLaunchShowsOnboarding() {
        resetData()
        launch()

        waitFor("了解时间，\n让每一天更有意义。")
        rule.onNodeWithText("了解时间，\n让每一天更有意义。").assertIsDisplayed()
        rule.onNodeWithText("继续").performClick()
        rule.onNodeWithText("你的出生日期").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesBetweenTabs() {
        seedCompletedProfile()
        launch()
        waitFor("LIFE REMAINING")

        rule.onNodeWithText("时间轴").performClick()
        rule.onNodeWithText("人生时间轴").assertIsDisplayed()

        rule.onNodeWithText("目标").performClick()
        rule.onNodeWithText("目标与愿望").assertIsDisplayed()

        rule.onNodeWithText("设置").performClick()
        rule.onNodeWithText("生命参数").assertIsDisplayed()

        rule.onNodeWithText("首页").performClick()
        rule.onNodeWithText("LIFE REMAINING").assertIsDisplayed()
    }
}
