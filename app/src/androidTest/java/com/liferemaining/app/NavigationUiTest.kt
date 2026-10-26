package com.liferemaining.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.time.LocalDate

/**
 * 端到端导航测试。
 *
 * 起点（Onboarding 还是首页）由启动瞬间的偏好决定，而 Compose 规则会先启动 Activity、
 * 再进入测试体。因此每个用例都自己写入或清空偏好，然后 recreate Activity，
 * 不依赖 JUnit 方法顺序（此前验证过此方案稳定通过）。
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    /** 在 Activity 启动前（@Before 之前）决定数据形态。 */
    private val seed = TestRule { base: Statement, _: Description ->
        object : Statement() {
            override fun evaluate() {
                val app = composeRule.activity.application as LifeRemainingApplication
                runBlocking {
                    app.container.clearEverything()
                    if (seedProfile) {
                        app.container.profileStore.setBirthDate(LocalDate.of(1990, 5, 15))
                        app.container.profileStore.setLifespan(80)
                        app.container.profileStore.setOnboardingCompleted(true)
                    }
                }
                base.evaluate()
            }
        }
    }

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(seed).around(composeRule)

    private var seedProfile = false

    private fun waitFor(text: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun firstLaunchShowsOnboarding() {
        // seedProfile = false：数据为空，启动即 Onboarding。
        composeRule.activityRule.scenario.recreate()
        waitFor("了解时间，\n让每一天更有意义。")
        composeRule.onNodeWithText("了解时间，\n让每一天更有意义。").assertIsDisplayed()
        composeRule.onNodeWithText("继续").performClick()
        composeRule.onNodeWithText("你的出生日期").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesBetweenTabs() {
        seedProfile = true
        composeRule.activityRule.scenario.recreate()
        waitFor("LIFE REMAINING")

        composeRule.onNodeWithText("时间轴").performClick()
        composeRule.onNodeWithText("人生时间轴").assertIsDisplayed()

        composeRule.onNodeWithText("目标").performClick()
        composeRule.onNodeWithText("目标与愿望").assertIsDisplayed()

        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("生命参数").assertIsDisplayed()

        composeRule.onNodeWithText("首页").performClick()
        composeRule.onNodeWithText("LIFE REMAINING").assertIsDisplayed()
    }
}
