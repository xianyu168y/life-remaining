package com.liferemaining.app.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.model.LifeGoal
import com.liferemaining.app.domain.model.GoalPriority
import com.liferemaining.app.ui.components.EmptyState
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.OutlinePill
import com.liferemaining.app.ui.components.Pill
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import java.time.temporal.ChronoUnit

@Composable
fun GoalsScreen(
    vm: GoalsViewModel,
    onOpenGoal: (Long) -> Unit,
    onCreate: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("新目标") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = Spacing.md,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Column {
                    Text(
                        text = "目标与愿望",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "不必都完成，但值得写下来。共 ${state.goals.size} 条当前视图。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    GoalFilter.entries.forEach { entry ->
                        FilterChip(
                            selected = state.filter == entry,
                            onClick = { vm.setFilter(entry) },
                            label = { Text(entry.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "列表", modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        vm.setSort(
                            if (state.sort == GoalSort.CREATED) GoalSort.DUE_DATE else GoalSort.CREATED
                        )
                    }) {
                        Text(state.sort.label)
                    }
                }
            }

            if (state.goals.isEmpty()) {
                item {
                    LifeCard(modifier = Modifier.fillMaxWidth()) {
                        EmptyState(
                            title = if (state.filter == GoalFilter.COMPLETED) "还没有完成的目标" else "还没有目标",
                            description = if (state.filter == GoalFilter.COMPLETED) {
                                "完成一个目标后，它会出现在这里。"
                            } else {
                                "写下一件你想在接下来的时间里完成的事。可以是今天，也可以是十年后。"
                            }
                        )
                    }
                }
            } else {
                items(items = state.goals, key = { it.id }) { goal ->
                    GoalRow(
                        goal = goal,
                        today = state.today,
                        onOpen = { onOpenGoal(goal.id) },
                        onToggle = { vm.toggleCompleted(goal) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalRow(
    goal: LifeGoal,
    today: java.time.LocalDate,
    onOpen: () -> Unit,
    onToggle: () -> Unit
) {
    LifeCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.Top) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (goal.isCompleted) {
                        Icons.Outlined.CheckCircle
                    } else {
                        Icons.Outlined.RadioButtonUnchecked
                    },
                    contentDescription = if (goal.isCompleted) "标记为进行中" else "标记为完成",
                    tint = if (goal.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (goal.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (goal.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinePill(text = goal.category.label)
                    if (goal.priority == GoalPriority.HIGH) {
                        Pill(
                            text = "高优先",
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    }
                    if (goal.isImportant) {
                        Pill(
                            text = "人生目标",
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                goal.dueDate?.let { due ->
                    Spacer(Modifier.height(Spacing.sm))
                    val days = ChronoUnit.DAYS.between(today, due)
                    Text(
                        text = "${Format.shortDate(due)} · ${Format.daysUntil(days)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (days < 0 && !goal.isCompleted) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
