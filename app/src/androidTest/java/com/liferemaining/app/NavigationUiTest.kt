package com.liferemaining.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
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
 * 关键点：Compose 规则会在 @Before 之前启动 Activity，而「首次启动是否进 Onboarding」
 * 由启动瞬间的偏好决定。因此这里用一个外层 Rule 在 Activity 启动前写入或清空数据。
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    private val seed = TestRule { base: Statement, _: Description ->
        object : Statement() {
            override fun evaluate() {
                val app = ApplicationProvider.getApplicationContext<LifeRemainingApplication>()
                runBlocking { app.container.clearEverything() }
                if (seedOnboarding) {
                    runBlocking {
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

    private var seedOnboarding = false

    @Test
    fun firstLaunchShowsOnboarding() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("了解时间，\n让每一天更有意义。"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("了解时间，\n让每一天更有意义。").assertIsDisplayed()
        composeRule.onNodeWithText("继续").performClick()
        composeRule.onNodeWithText("你的出生日期").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesBetweenTabs() {
        seedOnboarding = true

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("LIFE REMAINING"))
                .fetchSemanticsNodes().isNotEmpty()
        }

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
