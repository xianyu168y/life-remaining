package com.liferemaining.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.ReminderTone
import com.liferemaining.app.domain.model.ThemeMode
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.ui.components.GroupCard
import com.liferemaining.app.ui.components.ListRow
import com.liferemaining.app.ui.components.RowDivider
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Accents
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import kotlinx.coroutines.launch
import java.time.LocalTime

private const val MAX_IMPORT_BYTES = 4 * 1024 * 1024

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    profile: UserProfile,
    onOpenProfile: () -> Unit,
    onOpenMilestones: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenReflections: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onAccentChange: (AccentKey) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onShowExactDaysChange: (Boolean) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (LocalTime) -> Unit,
    onReminderToneChange: (ReminderTone) -> Unit,
    onCountdownEnabledChange: (Boolean) -> Unit,
    onDataCleared: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val message by vm.message.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var pendingImportContent by remember { mutableStateOf<String?>(null) }
    var permissionHint by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.resyncReminder()
        } else {
            permissionHint = true
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = vm.buildExportJson()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("无法写入所选文件")
                uri.lastPathSegment ?: "life-remaining-backup.json"
            }.onSuccess { vm.reportExportSaved(it) }
                .onFailure { vm.reportError("导出失败：${it.message ?: "未知错误"}") }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                    fd.statSize
                } ?: -1L
                if (size > MAX_IMPORT_BYTES) throw IllegalArgumentException("文件过大（上限 4MB）")
                val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                } ?: throw IllegalStateException("无法读取所选文件")
                text
            }.onSuccess { pendingImportContent = it }
                .onFailure { vm.reportError("读取文件失败：${it.message ?: "未知错误"}") }
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it.text)
            vm.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = Spacing.md,
                bottom = Spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                Column {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "外观、生命参数、提醒与数据。所有设置都在本机生效。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                SectionHeader(title = "外观")
                GroupCard {
                    ListRow(
                        title = "主题",
                        subtitle = profile.themeMode.label,
                        onClick = {
                            val next = when (profile.themeMode) {
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                            }
                            onThemeChange(next)
                        },
                        trailing = { Text("切换", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                    )
                    RowDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "强调色",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "低饱和，避免抢走数字的注意力",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            AccentKey.entries.forEach { key ->
                                AccentSwatch(
                                    key = key,
                                    selected = profile.accentKey == key,
                                    onClick = { onAccentChange(key) }
                                )
                            }
                        }
                    }
                    RowDivider()
                    ListRow(
                        title = "动态颜色",
                        subtitle = "Android 12+ 跟随壁纸取色",
                        trailing = {
                            Switch(
                                checked = profile.dynamicColor,
                                onCheckedChange = onDynamicColorChange
                            )
                        }
                    )
                }
            }

            item {
                SectionHeader(title = "生命参数")
                GroupCard {
                    ListRow(
                        title = "出生日期",
                        subtitle = profile.birthDate?.let(Format::longDate) ?: "未设置",
                        onClick = onOpenProfile,
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    ListRow(
                        title = "预期寿命",
                        subtitle = profile.expectedLifespanYears?.let { "$it 岁（估算参数）" } ?: "未设置",
                        onClick = onOpenProfile,
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    ListRow(
                        title = "启用倒计时",
                        subtitle = "关闭后只显示年龄与已度过天数",
                        trailing = {
                            Switch(
                                checked = profile.countdownEnabled,
                                onCheckedChange = onCountdownEnabledChange
                            )
                        }
                    )
                    RowDivider()
                    ListRow(
                        title = "隐藏精确数字",
                        subtitle = "首页只显示进度环与百分比",
                        trailing = {
                            Switch(
                                checked = !profile.showExactDays,
                                onCheckedChange = { hidden -> onShowExactDaysChange(!hidden) }
                            )
                        }
                    )
                }
            }

            item {
                SectionHeader(title = "提醒")
                GroupCard {
                    ListRow(
                        title = "每日提醒",
                        subtitle = if (profile.dailyReminderEnabled) {
                            "每天 ${Format.time(profile.reminderTime)} 前后"
                        } else {
                            "默认关闭"
                        },
                        trailing = {
                            Switch(
                                checked = profile.dailyReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && !vm.hasNotificationPermission()) {
                                        notificationPermissionLauncher.launch(
                                            android.Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    }
                                    onReminderEnabledChange(enabled)
                                }
                            )
                        }
                    )
                    RowDivider()
                    ListRow(
                        title = "提醒时间",
                        subtitle = Format.time(profile.reminderTime),
                        onClick = { showTimePicker = true },
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    ListRow(
                        title = "提醒语气",
                        subtitle = profile.reminderTone.label,
                        onClick = {
                            val next = when (profile.reminderTone) {
                                ReminderTone.GENTLE -> ReminderTone.NEUTRAL
                                ReminderTone.NEUTRAL -> ReminderTone.DIRECT
                                ReminderTone.DIRECT -> ReminderTone.GENTLE
                            }
                            onReminderToneChange(next)
                        },
                        trailing = { Text("切换", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                    )
                }
                if (permissionHint) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "系统未授予通知权限，提醒不会送达。可以稍后在系统设置中开启。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                SectionHeader(title = "更多")
                GroupCard {
                    ListRow(
                        title = "重要日期",
                        subtitle = "生日、纪念日、旅行与截止日期",
                        onClick = onOpenMilestones,
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    ListRow(
                        title = "每日记录",
                        subtitle = "浏览所有写下的日子",
                        onClick = onOpenReflections,
                        trailing = { Chevron() }
                    )
                }
            }

            item {
                SectionHeader(title = "数据管理")
                GroupCard {
                    ListRow(
                        title = "导出数据",
                        subtitle = "生成 JSON 备份，保存到你选择的位置",
                        onClick = {
                            exportLauncher.launch("life-remaining-backup.json")
                        }
                    )
                    RowDivider()
                    ListRow(
                        title = "导入数据",
                        subtitle = "校验通过后覆盖现有数据",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }
                    )
                    RowDivider()
                    ListRow(
                        title = "清除全部数据",
                        subtitle = "删除本机所有参数、目标与记录",
                        onClick = { confirmClear = true },
                        trailing = { Chevron() }
                    )
                }
            }

            item {
                SectionHeader(title = "关于")
                GroupCard {
                    ListRow(
                        title = "隐私与数据",
                        subtitle = "本地优先，无账号，无追踪",
                        onClick = onOpenPrivacy,
                        trailing = { Chevron() }
                    )
                    RowDivider()
                    ListRow(
                        title = "版本",
                        subtitle = "1.0.0（debug 构建带 -debug 后缀）"
                    )
                    RowDivider()
                    ListRow(
                        title = "开源许可",
                        subtitle = "Jetpack Compose · Material 3 · Room · WorkManager",
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://developer.android.com/jetpack/androidx/releases/compose")
                                )
                            )
                        }
                    )
                }
            }

            item {
                Text(
                    text = "界面语言参考 KernelSU Manager 的工具型布局：分层卡片、清晰的状态标识、克制的强调色。没有复制其品牌或 Root 功能。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showTimePicker) {
        val picker = rememberTimePickerState(
            initialHour = profile.reminderTime.hour,
            initialMinute = profile.reminderTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("提醒时间") },
            text = { TimePicker(state = picker) },
            confirmButton = {
                TextButton(onClick = {
                    onReminderTimeChange(LocalTime.of(picker.hour, picker.minute))
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清除全部数据？") },
            text = {
                Text(
                    "这会删除出生日期、预期寿命、全部目标、重要日期和每日记录，且无法恢复。导出备份后再清除会更稳妥。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        vm.clearAll(onDataCleared)
                    }
                ) {
                    Text("确认清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            }
        )
    }

    val importContent = pendingImportContent
    if (importContent != null) {
        AlertDialog(
            onDismissRequest = { pendingImportContent = null },
            title = { Text("覆盖现有数据？") },
            text = {
                Text("导入会用备份内容替换当前全部目标、重要日期、记录与参数。此操作无法撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.import(importContent)
                        pendingImportContent = null
                    }
                ) { Text("导入并覆盖") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportContent = null }) { Text("取消") }
            }
        )
    }

    if (busy) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.24f))
        )
    }
}

@Composable
private fun AccentSwatch(key: AccentKey, selected: Boolean, onClick: () -> Unit) {
    val color = Accents.forKey(key)
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = key.label,
                tint = Color(0xFF101312),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(12.dp)
    )
}
