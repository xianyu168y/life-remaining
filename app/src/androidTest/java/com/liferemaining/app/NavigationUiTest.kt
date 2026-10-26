package com.liferemaining.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 端到端导航测试。
 *
 * 起点（Onboarding 还是首页）由 MainActivity 启动时读取的 instrumentation
 * argument `-e app_test_start` 决定：ONBOARDING 强制首次配置页；DASHBOARD 强制首页。
 * 与 DataStore 状态解耦，并发用例互不影响，也不依赖 JUnit 方法顺序。
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingEntryShowsWelcome() {
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodes(hasText("了解时间，\n让每一天更有意义。"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("了解时间，\n让每一天更有意义。").assertIsDisplayed()
        rule.onNodeWithText("继续").performClick()
        rule.onNodeWithText("你的出生日期").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesBetweenTabs() {
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodes(hasText("LIFE REMAINING"))
                .fetchSemanticsNodes().isNotEmpty()
        }

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
