package com.liferemaining.app.ui.milestones

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.model.MilestoneCategory
import com.liferemaining.app.ui.components.DatePickerField
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing

@Composable
fun MilestoneEditorScreen(
    vm: MilestoneEditorViewModel,
    onBack: () -> Unit
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
                text = if (state.id > 0) "编辑日期" else "新日期",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "内容")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.title,
                onValueChange = vm::updateTitle,
                label = { Text("标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.md))
            DatePickerField(
                label = "日期",
                value = state.date,
                onChange = vm::updateDate
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = state.note,
                onValueChange = vm::updateNote,
                label = { Text("备注（可选）") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "类型")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            MilestoneCategory.entries.chunked(2).forEach { rowItems ->
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
        SectionHeader(title = "重复")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "每年重复",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "适用于生日、纪念日。闰年 2 月 29 日在平年按 2 月 28 日显示。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.repeatsAnnually,
                    onCheckedChange = vm::updateRepeat
                )
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
