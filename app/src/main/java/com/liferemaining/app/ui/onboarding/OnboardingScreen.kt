package com.liferemaining.app.ui.onboarding

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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.model.ReminderTone
import com.liferemaining.app.domain.model.ThemeMode
import com.liferemaining.app.ui.components.DatePickerField
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun OnboardingScreen(
    vm: OnboardingViewModel,
    hasNotificationPermission: () -> Boolean,
    requestNotificationPermission: () -> Unit,
    onFinished: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val earliest = remember { LocalDate.of(1900, 1, 1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl)
            .padding(top = Spacing.xxl, bottom = Spacing.xxl)
    ) {
        LinearProgressIndicator(
            progress = { (state.step + 1f) / (OnboardingViewModel.LAST_STEP + 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "第 ${state.step + 1} / ${OnboardingViewModel.LAST_STEP + 1} 步",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xl))

        when (state.step) {
            0 -> WelcomeStep()
            1 -> BirthDateStep(
                birthDate = state.birthDate,
                earliest = earliest,
                today = today,
                onChange = vm::setBirthDate
            )
            2 -> LifespanStep(
                enabled = state.lifespanEnabled,
                text = state.lifespanText,
                onEnabledChange = vm::setLifespanEnabled,
                onTextChange = vm::setLifespanText,
                estimatedEnd = state.estimatedEndDate,
                birthDate = state.birthDate
            )
            3 -> PreferencesStep(
                themeMode = state.themeMode,
                showExactDays = state.showExactDays,
                reminderEnabled = state.reminderEnabled,
                reminderTime = state.reminderTime,
                reminderTone = state.reminderTone,
                onThemeChange = vm::setTheme,
                onShowExactChange = vm::setShowExactDays,
                onReminderChange = { enabled ->
                    if (enabled && !hasNotificationPermission()) {
                        requestNotificationPermission()
                    }
                    vm.setReminderEnabled(enabled)
                },
                onPickTime = { showTimePicker = true },
                onToneChange = vm::setReminderTone
            )
            4 -> SummaryStep(
                birthDate = state.birthDate,
                lifespan = if (state.lifespanEnabled) state.lifespanValue else null,
                estimatedEnd = state.estimatedEndDate,
                themeMode = state.themeMode,
                reminderEnabled = state.reminderEnabled,
                reminderTime = state.reminderTime
            )
        }

        state.error?.let {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(Spacing.xxl))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            if (state.step > 0) {
                TextButton(onClick = vm::back, modifier = Modifier.weight(1f)) {
                    Text("上一步")
                }
            }
            if (state.step < OnboardingViewModel.LAST_STEP) {
                Button(onClick = vm::next, modifier = Modifier.weight(1f)) {
                    Text("继续")
                }
            } else {
                Button(onClick = { vm.finish(onFinished) }, modifier = Modifier.weight(1f)) {
                    Text("完成并进入")
                }
            }
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "所有数据只保存在本机，不上传、不需要账号。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showTimePicker) {
        val picker = rememberTimePickerState(
            initialHour = state.reminderTime.hour,
            initialMinute = state.reminderTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("提醒时间") },
            text = { TimePicker(state = picker) },
            confirmButton = {
                TextButton(onClick = {
                    vm.setReminderTime(LocalTime.of(picker.hour, picker.minute))
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun WelcomeStep() {
    Column {
        Text(
            text = "了解时间，\n让每一天更有意义。",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "通过你设定的时间范围，观察人生进度并规划接下来的日子。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xxl))
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "倒计时不是为了制造焦虑，而是提醒我们认真对待今天。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = "所有数字都是基于你填写内容的数学估算，不是医学预测，也不会被上传。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BirthDateStep(
    birthDate: LocalDate?,
    earliest: LocalDate,
    today: LocalDate,
    onChange: (LocalDate) -> Unit
) {
    Column {
        Text(
            text = "你的出生日期",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "用于计算已经度过的天数与当前年龄。不能选择未来日期，之后可以随时修改。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xl))
        DatePickerField(
            label = "出生日期",
            value = birthDate,
            onChange = onChange,
            selectableRange = earliest..today
        )
        if (birthDate != null) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = "已选择：${Format.longDate(birthDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LifespanStep(
    enabled: Boolean,
    text: String,
    onEnabledChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    estimatedEnd: LocalDate?,
    birthDate: LocalDate?
) {
    Column {
        Text(
            text = "预期寿命",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "这是你自己设定的估算参数，不是医学判断。范围 ${LifeCountdownCalculator.MIN_LIFESPAN_YEARS}–${LifeCountdownCalculator.MAX_LIFESPAN_YEARS} 岁。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xl))

        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "启用倒计时",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "关闭后仍会计算年龄与已度过天数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }
            if (enabled) {
                Spacer(Modifier.height(Spacing.lg))
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text("预期寿命（岁）") },
                    singleLine = true,
                    isError = text.toIntOrNull()?.let {
                        !LifeCountdownCalculator.isValidLifespan(it)
                    } ?: text.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = estimatedEnd?.let { "按此估算，预计终点：${Format.isoDate(it)}" }
                        ?: "填写后显示预计终点",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "已选择暂不启用。首页会显示年龄与已度过天数，不显示剩余时间。之后可在设置里开启。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (birthDate == null) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = "提示：还没有选择出生日期，返回上一步即可补上。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PreferencesStep(
    themeMode: ThemeMode,
    showExactDays: Boolean,
    reminderEnabled: Boolean,
    reminderTime: LocalTime,
    reminderTone: ReminderTone,
    onThemeChange: (ThemeMode) -> Unit,
    onShowExactChange: (Boolean) -> Unit,
    onReminderChange: (Boolean) -> Unit,
    onPickTime: () -> Unit,
    onToneChange: (ReminderTone) -> Unit
) {
    Column {
        Text(
            text = "偏好设置",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "都可以之后在设置里修改。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xl))

        SectionHeader(title = "外观")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { onThemeChange(mode) },
                        label = { Text(mode.label) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "显示")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "显示精确剩余天数",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "关闭后只显示进度环与百分比",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = showExactDays, onCheckedChange = onShowExactChange)
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "提醒")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "每日提醒",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "默认关闭。开启后需要通知权限。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = reminderEnabled, onCheckedChange = onReminderChange)
            }
            if (reminderEnabled) {
                Spacer(Modifier.height(Spacing.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "提醒时间 ${Format.time(reminderTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onPickTime) { Text("修改") }
                }
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    ReminderTone.entries.forEach { tone ->
                        FilterChip(
                            selected = reminderTone == tone,
                            onClick = { onToneChange(tone) },
                            label = { Text(tone.label) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStep(
    birthDate: LocalDate?,
    lifespan: Int?,
    estimatedEnd: LocalDate?,
    themeMode: ThemeMode,
    reminderEnabled: Boolean,
    reminderTime: LocalTime
) {
    Column {
        Text(
            text = "准备好了。",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "下面是即将写入本机的参数。确认后可随时修改。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xl))
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            SummaryRow("出生日期", birthDate?.let(Format::longDate) ?: "未填写")
            Spacer(Modifier.height(Spacing.sm))
            SummaryRow("预期寿命", lifespan?.let { "$it 岁" } ?: "未启用倒计时")
            Spacer(Modifier.height(Spacing.sm))
            SummaryRow("预计终点", estimatedEnd?.let(Format::isoDate) ?: "不计算")
            Spacer(Modifier.height(Spacing.sm))
            SummaryRow("主题", themeMode.label)
            Spacer(Modifier.height(Spacing.sm))
            SummaryRow(
                "每日提醒",
                if (reminderEnabled) Format.time(reminderTime) else "关闭"
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "这些数字只是你给自己的参照系。真正重要的，是今天打算怎么过。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun SummaryRow(key: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
