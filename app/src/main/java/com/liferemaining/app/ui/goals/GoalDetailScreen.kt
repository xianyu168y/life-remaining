package com.liferemaining.app.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.ui.components.EmptyState
import com.liferemaining.app.ui.components.KeyValueRow
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.Pill
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import java.time.temporal.ChronoUnit

@Composable
fun GoalDetailScreen(
    vm: GoalDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val goal = state.goal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg)
            .padding(top = Spacing.md, bottom = Spacing.xxl)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "目标详情",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (goal != null) {
                IconButton(onClick = { onEdit(goal.id) }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "编辑目标")
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "删除目标",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        if (goal == null) {
            LifeCard(modifier = Modifier.fillMaxWidth()) {
                EmptyState(
                    title = if (state.loading) "正在读取…" else "目标不存在",
                    description = if (state.loading) "请稍候。" else "它可能已被删除。"
                )
            }
            return@Column
        }

        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (goal.isCompleted) {
                    Pill(
                        text = "已完成",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (goal.description.isNotBlank()) {
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.md))
            KeyValueRow("分类", goal.category.label)
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow("优先级", goal.priority.label)
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow("创建日期", Format.longDate(goal.createdAt))
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow(
                "截止日期",
                goal.dueDate?.let(Format::longDate) ?: "无（长期愿望）"
            )
            if (goal.dueDate != null) {
                Spacer(Modifier.height(Spacing.sm))
                val days = ChronoUnit.DAYS.between(state.today, goal.dueDate)
                KeyValueRow(
                    "距离截止",
                    if (goal.isCompleted) "已结束" else Format.daysUntil(days),
                    valueColor = if (days < 0 && !goal.isCompleted) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            if (goal.isImportant) {
                Spacer(Modifier.height(Spacing.md))
                Pill(
                    text = "人生目标",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Button(
                onClick = vm::toggleCompleted,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (goal.isCompleted) "重新打开" else "标记完成")
            }
            OutlinedButton(
                onClick = { onEdit(goal.id) },
                modifier = Modifier.weight(1f)
            ) {
                Text("编辑")
            }
        }

        Spacer(Modifier.height(Spacing.xl))
        SectionHeader(title = "说明")
        Text(
            text = "完成目标只会改变状态，不会产生积分或排名。这个 App 不把时间变成游戏。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirmDelete && goal != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这个目标？") },
            text = { Text("「${goal.title}」将被永久删除，无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        vm.delete(onBack)
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            }
        )
    }
}
