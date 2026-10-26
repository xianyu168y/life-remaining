package com.liferemaining.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.model.LifeCountdown
import com.liferemaining.app.ui.components.EmptyState
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.LifeProgressRing
import com.liferemaining.app.ui.components.ListRow
import com.liferemaining.app.ui.components.PeriodProgressRow
import com.liferemaining.app.ui.components.Pill
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.components.StatTile
import com.liferemaining.app.ui.components.WeekStrip
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    onOpenSettings: () -> Unit,
    onOpenProfileSetup: () -> Unit,
    onOpenPeriod: (String) -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenMilestone: (Long) -> Unit,
    onOpenMilestones: () -> Unit,
    onOpenGoals: () -> Unit,
    onRecordToday: (LocalDate) -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        state = state,
        onRefresh = vm::refresh,
        onNextPrompt = vm::nextPrompt,
        onOpenSettings = onOpenSettings,
        onOpenProfileSetup = onOpenProfileSetup,
        onOpenPeriod = onOpenPeriod,
        onOpenGoal = onOpenGoal,
        onOpenMilestone = onOpenMilestone,
        onOpenMilestones = onOpenMilestones,
        onOpenGoals = onOpenGoals,
        onRecordToday = onRecordToday
    )
}

/** 无状态版本：方便 Compose UI 测试直接注入构造好的状态。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onNextPrompt: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfileSetup: () -> Unit,
    onOpenPeriod: (String) -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenMilestone: (Long) -> Unit,
    onOpenMilestones: () -> Unit,
    onOpenGoals: () -> Unit,
    onRecordToday: (LocalDate) -> Unit
) {
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            scope.launch {
                onRefresh()
                delay(320)
                refreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .padding(top = Spacing.md, bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            DashboardTopBar(
                today = state.today,
                onRefresh = onRefresh,
                onOpenSettings = onOpenSettings
            )

            CountdownCard(
                state = state,
                onConfigure = onOpenProfileSetup,
                onOpenSettings = onOpenSettings
            )

            if (state.profile.countdownConfigured) {
                ProgressSection(state = state, onOpenPeriod = onOpenPeriod)
            }

            TimeStatsSection(state = state, onOpenPeriod = onOpenPeriod)

            ActionCard(
                prompt = state.actionPrompt,
                onShuffle = onNextPrompt,
                onRecordToday = { onRecordToday(state.today) }
            )

            GoalsPreview(
                goals = state.activeGoals,
                onOpenGoal = onOpenGoal,
                onOpenGoals = onOpenGoals
            )

            MilestonesPreview(
                state = state,
                onOpenMilestone = onOpenMilestone,
                onOpenMilestones = onOpenMilestones
            )

            Text(
                text = "所有天数与进度均基于你填写的出生日期和预期寿命做日历运算，属于数学估算，不是医学预测。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.xs)
            )
        }
    }
}

@Composable
private fun DashboardTopBar(
    today: LocalDate,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm, bottom = Spacing.xs),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "余生",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.size(Spacing.sm))
                Pill(
                    text = "LIFE REMAINING",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "${Format.longDate(today)} · ${greeting(LocalTime.now())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "时间正在流动。今天，也值得认真度过。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "刷新今天的统计",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun greeting(time: LocalTime): String = when (time.hour) {
    in 5..10 -> "早上好"
    in 11..13 -> "中午好"
    in 14..17 -> "下午好"
    in 18..22 -> "晚上好"
    else -> "夜深了"
}

@Composable
private fun CountdownCard(
    state: DashboardUiState,
    onConfigure: () -> Unit,
    onOpenSettings: () -> Unit
) {
    LifeCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "LIFE REMAINING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            when (state.cardState) {
                CountdownCardState.ACTIVE -> Pill(
                    text = if (state.profile.showExactDays) "剩余" else "已隐藏数字",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
                CountdownCardState.HIDDEN -> Pill(
                    text = "已关闭倒计时",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CountdownCardState.REACHED_END -> Pill(
                    text = "已到设定终点",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CountdownCardState.NOT_CONFIGURED -> Pill(
                    text = "未设置",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        when (state.cardState) {
            CountdownCardState.NOT_CONFIGURED -> NotConfiguredBody(onConfigure)
            CountdownCardState.HIDDEN -> HiddenBody(state.countdown, onOpenSettings)
            CountdownCardState.REACHED_END -> ReachedEndBody(state.countdown)
            CountdownCardState.ACTIVE -> ActiveBody(state)
        }
    }
}

@Composable
private fun ActiveBody(state: DashboardUiState) {
    val countdown = state.countdown
    val remaining = countdown.remainingDays ?: 0L
    val progress = (countdown.progressPercent ?: 0.0).toFloat() / 100f
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            if (state.profile.showExactDays) {
                Text(
                    text = Format.grouped(remaining),
                    style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "预计剩余天数",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "••••",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "精确天数已隐藏",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = ageLine(countdown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "已走过 ${Format.grouped(countdown.daysLived)} 天",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(Spacing.lg))
        LifeProgressRing(
            progress = progress,
            ringSize = 124.dp,
            strokeWidth = 9.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "生命进度",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(Modifier.height(Spacing.lg))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(Spacing.md))

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "预计终点",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = countdown.expectedEndDate?.let(Format::isoDate) ?: "—",
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "总天数",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = countdown.totalDays?.let(Format::grouped) ?: "—",
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "预期寿命",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = state.profile.expectedLifespanYears?.let { "$it 岁" } ?: "—",
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    Spacer(Modifier.height(Spacing.md))
    Text(
        text = "基于你设定的预期寿命估算，不是医学预测。可在设置中随时修改。",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun ageLine(countdown: LifeCountdown): String {
    val y = countdown.ageYears ?: return "年龄未知"
    val m = countdown.ageMonths ?: 0
    val d = countdown.ageDays ?: 0
    return "已满 $y 岁 $m 个月 $d 天"
}

@Composable
private fun HiddenBody(countdown: LifeCountdown, onOpenSettings: () -> Unit) {
    Text(
        text = "倒计时显示已关闭",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(Spacing.sm))
    Text(
        text = "你仍然可以在时间轴和目标里查看自己的时间安排。随时可以重新开启。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(Spacing.lg))
    TextButton(onClick = onOpenSettings) { Text("去设置中开启") }
}

@Composable
private fun ReachedEndBody(countdown: LifeCountdown) {
    Text(
        text = "已达到设定的时间范围",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(Spacing.sm))
    Text(
        text = "你设定的预期终点是 ${countdown.expectedEndDate?.let(Format::isoDate) ?: "—"}。这只是你给自己的一个数字，不是任何形式的期限。可以在设置里调整预期寿命，继续观察新的时间范围。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NotConfiguredBody(onConfigure: () -> Unit) {
    Text(
        text = "还没有设置生命参数",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(Spacing.sm))
    Text(
        text = "填写出生日期与预期寿命后，这里会显示剩余天数、年龄与生命进度。所有数据只保存在本机。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(Spacing.lg))
    Button(
        onClick = onConfigure,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("设置生命参数")
    }
}

@Composable
private fun ProgressSection(state: DashboardUiState, onOpenPeriod: (String) -> Unit) {
    Column {
        SectionHeader(title = "时间进度")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            PeriodProgressRow(
                label = "今年",
                progress = state.stats.year,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.lg))
            PeriodProgressRow(
                label = "本月",
                progress = state.stats.month,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(Spacing.lg))
            PeriodProgressRow(
                label = "本周",
                progress = state.stats.week,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.height(Spacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.lg))
            WeekStrip(days = state.stats.weekDays)
            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                TextButton(onClick = { onOpenPeriod("year") }, modifier = Modifier.weight(1f)) {
                    Text("年度详情")
                }
                TextButton(onClick = { onOpenPeriod("month") }, modifier = Modifier.weight(1f)) {
                    Text("月度详情")
                }
                TextButton(onClick = { onOpenPeriod("week") }, modifier = Modifier.weight(1f)) {
                    Text("本周详情")
                }
            }
        }
    }
}

@Composable
private fun TimeStatsSection(state: DashboardUiState, onOpenPeriod: (String) -> Unit) {
    val stats = state.stats
    val livedDays = if (state.profile.hasBirthDate) state.countdown.daysLived else stats.yearDaysLived
    val livedSupporting = state.countdown.ageYears?.let { y ->
        val m = state.countdown.ageMonths ?: 0
        "$y 年 $m 个月"
    } ?: "设置出生日期后可查看"
    Column {
        SectionHeader(title = "时间统计")
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                label = "已经度过",
                value = Format.grouped(livedDays),
                unit = "天",
                supporting = livedSupporting,
                modifier = Modifier.weight(1f),
                onClick = { onOpenPeriod("life") }
            )
            StatTile(
                label = "本年度",
                value = stats.year.remaining.toString(),
                unit = "天",
                supporting = "已过去 ${stats.year.percent.roundToInt()}%",
                modifier = Modifier.weight(1f),
                onClick = { onOpenPeriod("year") }
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                label = "本月",
                value = stats.month.remaining.toString(),
                unit = "天",
                supporting = "已过去 ${stats.month.elapsed}/${stats.month.total} 天",
                modifier = Modifier.weight(1f),
                onClick = { onOpenPeriod("month") }
            )
            StatTile(
                label = "本周",
                value = stats.week.remaining.toString(),
                unit = "天",
                supporting = "今天是第 ${stats.week.elapsed}/7 天",
                modifier = Modifier.weight(1f),
                onClick = { onOpenPeriod("week") }
            )
        }
    }
}

@Composable
private fun ActionCard(prompt: String, onShuffle: () -> Unit, onRecordToday: () -> Unit) {
    Column {
        SectionHeader(title = "今天值得做的事")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = prompt,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TextButton(onClick = onShuffle) { Text("换一句") }
                Button(
                    onClick = onRecordToday,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("记录今天")
                }
            }
        }
    }
}

@Composable
private fun GoalsPreview(
    goals: List<com.liferemaining.app.domain.model.LifeGoal>,
    onOpenGoal: (Long) -> Unit,
    onOpenGoals: () -> Unit
) {
    Column {
        SectionHeader(
            title = "进行中的目标",
            trailing = { TextButton(onClick = onOpenGoals) { Text("全部") } }
        )
        LifeCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            if (goals.isEmpty()) {
                EmptyState(
                    title = "还没有进行中的目标",
                    description = "目标不必宏大，写下一件你想在余生里完成的事。"
                )
            } else {
                goals.take(3).forEachIndexed { index, goal ->
                    ListRow(
                        title = goal.title,
                        subtitle = buildString {
                            append(goal.category.label)
                            goal.dueDate?.let { append(" · 截止 ").append(Format.shortDate(it)) }
                        },
                        onClick = { onOpenGoal(goal.id) },
                        trailing = {
                            if (goal.isImportant) {
                                Pill(
                                    text = "重要",
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    if (index < minOf(goals.size, 3) - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestonesPreview(
    state: DashboardUiState,
    onOpenMilestone: (Long) -> Unit,
    onOpenMilestones: () -> Unit
) {
    Column {
        SectionHeader(
            title = "接下来值得期待",
            trailing = { TextButton(onClick = onOpenMilestones) { Text("管理") } }
        )
        LifeCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            if (state.upcomingMilestones.isEmpty()) {
                EmptyState(
                    title = "还没有安排重要日期",
                    description = "生日、纪念日、一次旅行，都可以放进来。"
                )
            } else {
                state.upcomingMilestones.forEachIndexed { index, occurrence ->
                    ListRow(
                        title = occurrence.milestone.title,
                        subtitle = "${Format.longDate(occurrence.nextDate)} · ${Format.daysUntil(occurrence.daysUntil)}",
                        onClick = { onOpenMilestone(occurrence.milestone.id) },
                        trailing = {
                            Text(
                                text = "${occurrence.daysUntil} 天",
                                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    if (index < state.upcomingMilestones.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
