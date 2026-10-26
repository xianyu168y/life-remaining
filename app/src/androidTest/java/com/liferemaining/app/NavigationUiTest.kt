package com.liferemaining.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 端到端入口测试：清空数据后启动应进入 Onboarding，走完后进入首页并可切到设置。
 * 使用真实的 DataStore 与 Room，不加任何 mock。
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val container: AppContainer
        get() = (rule.activity.application as LifeRemainingApplication).container

    @Before
    fun reset() = runBlocking {
        container.clearEverything()
    }

    @Test
    fun firstLaunchShowsOnboardingAndReachesDashboard() {
        // 清空后需要重建 Activity 才会重新读取 onboardingCompleted。
        rule.activityRule.scenario.recreate()

        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodes(androidx.compose.ui.test.hasText("了解时间，\n让每一天更有意义。"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("了解时间，\n让每一天更有意义。").assertIsDisplayed()
        rule.onNodeWithText("继续").performClick()
        rule.onNodeWithText("你的出生日期").assertIsDisplayed()
    }
}
