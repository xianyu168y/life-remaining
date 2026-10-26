package com.liferemaining.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * 端到端导航测试。
 *
 * 起点由启动瞬间的 DataStore 决定，而 Compose 规则先启动 Activity、再执行测试体。
 * 因此：@Before 清空数据；需要「已配置」状态的用例在体内写入参数后 recreate 一次。
 * 不依赖 JUnit 方法顺序（此结构在 run 7 已验证通过）。
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val container: AppContainer
        get() = (rule.activity.application as LifeRemainingApplication).container

    @Before
    fun clearData() {
        runBlocking { container.clearEverything() }
    }

    private fun waitFor(text: String) {
        rule.waitUntil(timeoutMillis = 15_000) {
            rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun seedCompletedProfile() {
        runBlocking {
            container.profileStore.setBirthDate(LocalDate.of(1990, 5, 15))
            container.profileStore.setLifespan(80)
            container.profileStore.setOnboardingCompleted(true)
        }
        rule.activityRule.scenario.recreate()
        waitFor("LIFE REMAINING")
    }

    @Test
    fun firstLaunchShowsOnboarding() {
        rule.activityRule.scenario.recreate()
        waitFor("了解时间，\n让每一天更有意义。")
        rule.onNodeWithText("了解时间，\n让每一天更有意义。").assertIsDisplayed()
        rule.onNodeWithText("继续").performClick()
        rule.onNodeWithText("你的出生日期").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesBetweenTabs() {
        seedCompletedProfile()

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
