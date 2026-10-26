package com.liferemaining.app.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.model.PeriodProgress
import com.liferemaining.app.ui.components.EmptyState
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.PeriodProgressRow
import com.liferemaining.app.ui.components.Pill
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.components.WeekStrip
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import kotlin.math.roundToInt

@Composable
fun TimelineScreen(
    vm: TimelineViewModel,
    onOpenYear: (Int) -> Unit,
    onOpenProfileSetup: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = Spacing.md,
            bottom = Spacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Column {
                Text(
                    text = "人生时间轴",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = if (state.birthYear != null) {
                        "从 ${state.birthYear} 年开始，一共 ${state.years.size} 个年份。未来年份只是规划，不代表任何确定事件。"
                    } else {
                        "设置出生日期后，这里会按年份展开你的人生时间轴。"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TimelineMode.entries.forEach { entry ->
                    FilterChip(
                        selected = state.mode == entry,
                        onClick = { vm.setMode(entry) },
                        label = { Text(entry.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        if (state.birthYear == null) {
            item {
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        title = "还没有出生日期",
                        description = "时间轴需要出生日期作为起点，填一次就够，之后可以随时修改。",
                        action = {
                            Button(onClick = onOpenProfileSetup) { Text("去设置") }
                        }
                    )
                }
            }
            return@LazyColumn
        }

        when (state.mode) {
            TimelineMode.YEAR -> {
                item {
                    LifeCard(modifier = Modifier.fillMaxWidth()) {
                        PeriodProgressRow(label = "今年", progress = state.yearProgress)
                    }
                }
                items(items = state.years, key = { it.year }) { entry ->
                    YearRow(entry = entry, onClick = { onOpenYear(entry.year) })
                }
            }

            TimelineMode.MONTH -> {
                item {
                    LifeCard(modifier = Modifier.fillMaxWidth()) {
                        PeriodProgressRow(label = "${state.today.year} 年", progress = state.yearProgress)
                    }
                }
                items(items = state.months, key = { it.month }) { entry ->
                    MonthRow(entry = entry, isCurrent = entry.month == state.today.monthValue)
                }
            }

            TimelineMode.WEEK -> {
                item {
                    LifeCard(modifier = Modifier.fillMaxWidth()) {
                        PeriodProgressRow(label = "本周", progress = state.weekProgress)
                        Spacer(Modifier.height(Spacing.lg))
                        WeekStrip(days = state.weekCells)
                        Spacer(Modifier.height(Spacing.md))
                        Text(
                            text = "一周从周一开始，今天算作已过去的一天。",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearRow(entry: TimelineRow.YearEntry, onClick: () -> Unit) {
    val accent = when (entry.state) {
        YearState.CURRENT -> MaterialTheme.colorScheme.primary
        YearState.PAST -> MaterialTheme.colorScheme.onSurfaceVariant
        YearState.FUTURE -> MaterialTheme.colorScheme.outline
        YearState.BEYOND_LIFESPAN -> MaterialTheme.colorScheme.outlineVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (entry.state == YearState.CURRENT) 10.dp else 6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.year.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    color = if (entry.state == YearState.BEYOND_LIFESPAN) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(Modifier.size(Spacing.sm))
                if (entry.state == YearState.CURRENT) {
                    Pill(
                        text = "今年",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
                if (entry.state == YearState.FUTURE || entry.state == YearState.BEYOND_LIFESPAN) {
                    Text(
                        text = "规划",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = entry.ageAtYear?.let { "$it 岁" } ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (entry.noteCount > 0) {
            Text(
                text = "${entry.noteCount} 条回忆",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthRow(entry: TimelineRow.MonthEntry, isCurrent: Boolean) {
    val progress = PeriodProgress(entry.elapsed, entry.total)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${entry.month} 月",
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(56.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            com.liferemaining.app.ui.components.ProgressBar(
                fraction = progress.fraction,
                color = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Spacer(Modifier.size(Spacing.md))
        Text(
            text = "${progress.percent.roundToInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(38.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
