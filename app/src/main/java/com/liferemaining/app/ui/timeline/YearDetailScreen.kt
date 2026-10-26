package com.liferemaining.app.ui.timeline

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format

@Composable
fun YearDetailScreen(
    vm: YearDetailViewModel,
    year: Int,
    onBack: () -> Unit
) {
    val notes by vm.notes.collectAsStateWithLifecycle()
    val age by vm.ageAtYear.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = Spacing.md,
            bottom = Spacing.xxl
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
                        text = "$year 年",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = age?.let { "这一年你 $it 岁" } ?: "还没有设置出生日期",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionHeader(title = "这一年的回忆")
            LifeCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("记下一件与 $year 年有关的事") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedButton(
                        onClick = {
                            vm.addNote(draft)
                            draft = ""
                        },
                        enabled = draft.isNotBlank()
                    ) { Text("保存") }
                    TextButton(onClick = { draft = "" }, enabled = draft.isNotEmpty()) {
                        Text("清空")
                    }
                }
            }
        }

        if (notes.isEmpty()) {
            item {
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        title = "这一年还没有记录",
                        description = "写下当时的一个画面、一句话，或一件小事。"
                    )
                }
            }
        } else {
            items(items = notes, key = { it.id }) { note ->
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { vm.deleteNote(note) }) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = "删除这条回忆",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "写于 ${note.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
