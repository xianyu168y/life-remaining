package com.liferemaining.app.ui.reflection

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import java.time.LocalDate

@Composable
fun ReflectionScreen(
    vm: ReflectionEditorViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.existing?.id, state.existing?.content) {
        vm.loadExistingIntoDraft(state.existing)
    }

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
            Column {
                Text(
                    text = "记录今天",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = Format.longDate(state.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "今天值得记住的事")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = vm::updateDraft,
                label = { Text("一句话也可以") },
                minLines = 6,
                maxLines = 14,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Button(
                    onClick = vm::save,
                    enabled = !state.saving && state.draft.isNotBlank()
                ) {
                    Text(if (state.saving) "保存中…" else "保存")
                }
                if (state.existing != null) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            state.savedAt?.let {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            state.error?.let {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "记录只保存在本机，不会上传。没有记录的日子，这里不会生成任何内容。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这条记录？") },
            text = { Text("删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.delete()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            }
        )
    }
}

@Composable
fun ReflectionHistoryScreen(
    vm: ReflectionHistoryViewModel,
    onBack: () -> Unit,
    onOpenDate: (LocalDate) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()

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
                text = "每日记录",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "今天",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.sm))
            TextButton(onClick = { onOpenDate(state.today) }) {
                Text("记录 ${Format.longDate(state.today)}")
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "历史记录")
        if (state.entries.isEmpty()) {
            LifeCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "还没有任何记录。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            state.entries.forEach { entry ->
                LifeCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenDate(entry.date) }) {
                    Text(
                        text = Format.longDate(entry.date),
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(Spacing.md))
            }
        }
    }
}
