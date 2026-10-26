package com.liferemaining.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liferemaining.app.AppContainer
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.navigation.Routes
import com.liferemaining.app.ui.dashboard.DashboardScreen
import com.liferemaining.app.ui.dashboard.DashboardViewModel
import com.liferemaining.app.ui.dashboard.PeriodDetailScreen
import com.liferemaining.app.ui.goals.GoalDetailScreen
import com.liferemaining.app.ui.goals.GoalDetailViewModel
import com.liferemaining.app.ui.goals.GoalEditorScreen
import com.liferemaining.app.ui.goals.GoalEditorViewModel
import com.liferemaining.app.ui.goals.GoalsScreen
import com.liferemaining.app.ui.goals.GoalsViewModel
import com.liferemaining.app.ui.milestones.MilestoneEditorScreen
import com.liferemaining.app.ui.milestones.MilestoneEditorViewModel
import com.liferemaining.app.ui.milestones.MilestonesScreen
import com.liferemaining.app.ui.milestones.MilestonesViewModel
import com.liferemaining.app.ui.onboarding.OnboardingScreen
import com.liferemaining.app.ui.onboarding.OnboardingViewModel
import com.liferemaining.app.ui.reflection.ReflectionEditorViewModel
import com.liferemaining.app.ui.reflection.ReflectionHistoryScreen
import com.liferemaining.app.ui.reflection.ReflectionHistoryViewModel
import com.liferemaining.app.ui.reflection.ReflectionScreen
import com.liferemaining.app.ui.settings.PrivacyScreen
import com.liferemaining.app.ui.settings.ProfileDetailScreen
import com.liferemaining.app.ui.settings.SettingsScreen
import com.liferemaining.app.ui.settings.SettingsViewModel
import com.liferemaining.app.ui.theme.LifeRemainingTheme
import com.liferemaining.app.ui.timeline.TimelineScreen
import com.liferemaining.app.ui.timeline.TimelineViewModel
import com.liferemaining.app.ui.timeline.YearDetailScreen
import com.liferemaining.app.ui.timeline.YearDetailViewModel
import java.time.LocalDate

@Composable
fun LifeRemainingRoot(container: AppContainer) {
    val appViewModel: AppViewModel = viewModel(factory = viewModelFactory { AppViewModel(container) })
    val profileState by appViewModel.profile.collectAsStateWithLifecycle()
    val today by appViewModel.today.collectAsStateWithLifecycle()

    val profile = profileState
    if (profile == null) {
        // DataStore 首帧到达前不渲染导航，避免 Onboarding 闪一下再跳首页。
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {}
        return
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                appViewModel.refreshToday()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LifeRemainingTheme(
        themeMode = profile.themeMode,
        accentKey = profile.accentKey,
        dynamicColor = profile.dynamicColor
    ) {
        val navController = rememberNavController()
        // 只在首次进入时决定起点：若跟随 profile 变化，NavHost 会重建图并打断当前导航。
        val startDestination = remember {
            if (profile.onboardingCompleted) Routes.DASHBOARD else Routes.ONBOARDING
        }

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Routes.ONBOARDING) {
                val vm: OnboardingViewModel = viewModel(
                    factory = viewModelFactory { OnboardingViewModel(container) }
                )
                SecondaryPage {
                    OnboardingScreen(
                        vm = vm,
                        onFinished = {
                            appViewModel.refreshToday()
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(
                    factory = viewModelFactory { DashboardViewModel(container) }
                )
                MainScaffold(
                    selectedTab = BottomTab.DASHBOARD,
                    onSelectTab = { navController.navigateToTab(it) }
                ) { padding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                        DashboardScreen(
                            vm = vm,
                            onOpenSettings = { navController.navigateToTab(BottomTab.SETTINGS) },
                            onOpenProfileSetup = { navController.navigate(Routes.PROFILE_DETAIL) },
                            onOpenPeriod = { navController.navigate(Routes.period(it)) },
                            onOpenGoal = { navController.navigate(Routes.goalDetail(it)) },
                            onOpenMilestone = { navController.navigate(Routes.milestoneEdit(it)) },
                            onOpenMilestones = { navController.navigate(Routes.MILESTONES) },
                            onOpenGoals = { navController.navigateToTab(BottomTab.GOALS) },
                            onRecordToday = { date -> navController.navigate(Routes.reflection(date.toString())) }
                        )
                    }
                }
            }

            composable(Routes.TIMELINE) {
                val vm: TimelineViewModel = viewModel(
                    factory = viewModelFactory { TimelineViewModel(container) }
                )
                MainScaffold(
                    selectedTab = BottomTab.TIMELINE,
                    onSelectTab = { navController.navigateToTab(it) }
                ) { padding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                        TimelineScreen(
                            vm = vm,
                            onOpenYear = { year -> navController.navigate(Routes.yearDetail(year)) },
                            onOpenProfileSetup = { navController.navigate(Routes.PROFILE_DETAIL) }
                        )
                    }
                }
            }

            composable(Routes.GOALS) {
                val vm: GoalsViewModel = viewModel(
                    factory = viewModelFactory { GoalsViewModel(container) }
                )
                MainScaffold(
                    selectedTab = BottomTab.GOALS,
                    onSelectTab = { navController.navigateToTab(it) }
                ) { padding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                        GoalsScreen(
                            vm = vm,
                            onOpenGoal = { id -> navController.navigate(Routes.goalDetail(id)) },
                            onCreate = { navController.navigate(Routes.goalEdit(null)) }
                        )
                    }
                }
            }

            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(
                    factory = viewModelFactory { SettingsViewModel(container) }
                )
                MainScaffold(
                    selectedTab = BottomTab.SETTINGS,
                    onSelectTab = { navController.navigateToTab(it) }
                ) { padding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
                        SettingsScreen(
                            vm = vm,
                            profile = profile,
                            onOpenProfile = { navController.navigate(Routes.PROFILE_DETAIL) },
                            onOpenMilestones = { navController.navigate(Routes.MILESTONES) },
                            onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                            onOpenReflections = { navController.navigate(Routes.REFLECTIONS) },
                            onThemeChange = appViewModel::setThemeMode,
                            onAccentChange = appViewModel::setAccent,
                            onDynamicColorChange = appViewModel::setDynamicColor,
                            onShowExactDaysChange = appViewModel::setShowExactDays,
                            onReminderEnabledChange = appViewModel::setReminderEnabled,
                            onReminderTimeChange = appViewModel::setReminderTime,
                            onReminderToneChange = appViewModel::setReminderTone,
                            onCountdownEnabledChange = appViewModel::setCountdownEnabled,
                            onDataCleared = {
                                navController.navigate(Routes.ONBOARDING) {
                                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            composable(Routes.MILESTONES) {
                val vm: MilestonesViewModel = viewModel(
                    factory = viewModelFactory { MilestonesViewModel(container) }
                )
                MilestonesScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onCreate = { navController.navigate(Routes.milestoneEdit(null)) },
                    onEdit = { id -> navController.navigate(Routes.milestoneEdit(id)) }
                )
            }

            composable(
                route = Routes.MILESTONE_EDIT,
                arguments = listOf(navArgument("milestoneId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("milestoneId")?.takeIf { it > 0 }
                val vm: MilestoneEditorViewModel = viewModel(
                    key = "milestone-edit-${id ?: 0}",
                    factory = viewModelFactory { MilestoneEditorViewModel(container, id) }
                )
                SecondaryPage {
                    MilestoneEditorScreen(vm = vm, onBack = { navController.popBackStack() })
                }
            }

            composable(
                route = Routes.GOAL_DETAIL,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("goalId") ?: -1L
                val vm: GoalDetailViewModel = viewModel(
                    key = "goal-detail-$id",
                    factory = viewModelFactory { GoalDetailViewModel(container, id) }
                )
                SecondaryPage {
                    GoalDetailScreen(
                        vm = vm,
                        onBack = { navController.popBackStack() },
                        onEdit = { goalId -> navController.navigate(Routes.goalEdit(goalId)) }
                    )
                }
            }

            composable(
                route = Routes.GOAL_EDIT,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                val id = entry.arguments?.getLong("goalId")?.takeIf { it > 0 }
                val vm: GoalEditorViewModel = viewModel(
                    key = "goal-edit-${id ?: 0}",
                    factory = viewModelFactory { GoalEditorViewModel(container, id) }
                )
                GoalEditorScreen(vm = vm, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.REFLECTION,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { entry ->
                val date = entry.arguments?.getString("date")?.let(LocalDate::parse) ?: today
                val vm: ReflectionEditorViewModel = viewModel(
                    key = "reflection-$date",
                    factory = viewModelFactory { ReflectionEditorViewModel(container, date) }
                )
                SecondaryPage {
                    ReflectionScreen(vm = vm, onBack = { navController.popBackStack() })
                }
            }

            composable(Routes.REFLECTIONS) {
                val vm: ReflectionHistoryViewModel = viewModel(
                    factory = viewModelFactory { ReflectionHistoryViewModel(container) }
                )
                SecondaryPage {
                    ReflectionHistoryScreen(
                        vm = vm,
                        onBack = { navController.popBackStack() },
                        onOpenDate = { date -> navController.navigate(Routes.reflection(date.toString())) }
                    )
                }
            }

            composable(
                route = Routes.PERIOD,
                arguments = listOf(navArgument("period") { type = NavType.StringType })
            ) { entry ->
                val period = entry.arguments?.getString("period") ?: "year"
                val countdown = LifeCountdownCalculator.calculate(
                    profile.birthDate, today, profile.expectedLifespanYears
                )
                SecondaryPage {
                    PeriodDetailScreen(
                        period = period,
                        today = today,
                        daysLived = countdown.daysLived,
                        birthDate = profile.birthDate,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Routes.YEAR_DETAIL,
                arguments = listOf(navArgument("year") { type = NavType.IntType })
            ) { entry ->
                val year = entry.arguments?.getInt("year") ?: today.year
                val vm: YearDetailViewModel = viewModel(
                    key = "year-$year",
                    factory = viewModelFactory { YearDetailViewModel(container, year) }
                )
                SecondaryPage {
                    YearDetailScreen(vm = vm, year = year, onBack = { navController.popBackStack() })
                }
            }

            composable(Routes.PROFILE_DETAIL) {
                SecondaryPage {
                    ProfileDetailScreen(
                        profile = profile,
                        today = today,
                        onBack = { navController.popBackStack() },
                        onBirthDateChange = appViewModel::setBirthDate,
                        onLifespanChange = appViewModel::setLifespan
                    )
                }
            }

            composable(Routes.PRIVACY) {
                SecondaryPage {
                    PrivacyScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateToTab(tab: BottomTab) {
    if (currentDestination?.route == tab.route) return
    navigate(tab.route) {
        popUpTo(Routes.DASHBOARD) {
            saveState = true
            inclusive = false
        }
        launchSingleTop = true
        restoreState = true
    }
}
