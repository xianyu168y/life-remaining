package com.liferemaining.app.ui.milestones

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.model.Milestone
import com.liferemaining.app.ui.components.EmptyState
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.ListRow
import com.liferemaining.app.ui.components.OutlinePill
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format

@Composable
fun MilestonesScreen(
    vm: MilestonesViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Milestone?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("新日期") }
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
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                    Column {
                        Text(
                            text = "重要日期",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "年度重复的日期会自动计算下一次，2 月 29 日在平年按 2 月 28 日算。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { SectionHeader(title = "接下来") }

            if (state.upcoming.isEmpty()) {
                item {
                    LifeCard(modifier = Modifier.fillMaxWidth()) {
                        EmptyState(
                            title = "还没有未来的日期",
                            description = "添加生日、纪念日或一次旅行，首页会显示最近的三条。"
                        )
                    }
                }
            } else {
                items(items = state.upcoming, key = { "u-${it.milestone.id}" }) { item ->
                    LifeCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        MilestoneRow(
                            item = item,
                            onClick = { onEdit(item.milestone.id) },
                            onDelete = { pendingDelete = item.milestone }
                        )
                    }
                }
            }

            if (state.past.isNotEmpty()) {
                item { SectionHeader(title = "已经过去") }
                items(items = state.past, key = { "p-${it.milestone.id}" }) { item ->
                    LifeCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        MilestoneRow(
                            item = item,
                            onClick = { onEdit(item.milestone.id) },
                            onDelete = { pendingDelete = item.milestone }
                        )
                    }
                }
            }
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这个日期？") },
            text = { Text("「${toDelete.title}」将被永久删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.delete(toDelete)
                        pendingDelete = null
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun MilestoneRow(
    item: MilestoneItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Column {
        ListRow(
            title = item.milestone.title,
            subtitle = buildString {
                append(Format.longDate(item.nextDate))
                if (item.milestone.repeatsAnnually) append(" · 每年")
                if (item.milestone.note.isNotBlank()) append(" · ").append(item.milestone.note)
            },
            onClick = onClick,
            trailing = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Format.daysUntil(item.daysUntil),
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = if (item.isPast) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    OutlinePill(text = item.milestone.category.label)
                }
            }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClick) { Text("编辑") }
            TextButton(onClick = onDelete) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
