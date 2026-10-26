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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.model.GoalCategory
import com.liferemaining.app.domain.model.GoalPriority
import com.liferemaining.app.ui.components.DatePickerField
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing

@Composable
fun GoalEditorScreen(
    vm: GoalEditorViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
                Text(
                    text = if (state.id > 0) "编辑目标" else "新目标",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "基本信息")
            LifeCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = vm::updateTitle,
                    label = { Text("标题") },
                    singleLine = true,
                    isError = state.error != null && state.title.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spacing.md))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = vm::updateDescription,
                    label = { Text("描述（可选）") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "分类")
            LifeCard(modifier = Modifier.fillMaxWidth()) {
                GoalCategory.entries.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        rowItems.forEach { category ->
                            FilterChip(
                                selected = state.category == category,
                                onClick = { vm.updateCategory(category) },
                                label = { Text(category.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(Spacing.xs))
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "优先级与属性")
            LifeCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    GoalPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = state.priority == priority,
                            onClick = { vm.updatePriority(priority) },
                            label = { Text(priority.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "标记为重要人生目标",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "会在首页与列表中优先显示",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isImportant,
                        onCheckedChange = vm::updateImportant
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "截止日期")
            LifeCard(modifier = Modifier.fillMaxWidth()) {
                DatePickerField(
                    label = "可选。长期愿望可以留空。",
                    value = state.dueDate,
                    onChange = { vm.updateDueDate(it) }
                )
                if (state.dueDate != null) {
                    Spacer(Modifier.height(Spacing.sm))
                    TextButton(onClick = { vm.updateDueDate(null) }) {
                        Text("清除截止日期")
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = state.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(Spacing.xl))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("取消") }
                Button(
                    onClick = { vm.save(onBack) },
                    enabled = !state.saving && state.loaded,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.saving) "保存中…" else "保存")
                }
            }
        }
    }
}
