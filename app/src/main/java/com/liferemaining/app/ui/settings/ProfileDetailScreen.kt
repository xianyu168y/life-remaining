package com.liferemaining.app.ui.settings

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liferemaining.app.domain.calculator.LifeCountdownCalculator
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.ui.components.DatePickerField
import com.liferemaining.app.ui.components.KeyValueRow
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.PeriodProgressRow
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import java.time.LocalDate

/**
 * 生命参数详情：首页主卡片点击后进入。
 * 这里同时承担「修改出生日期 / 预期寿命」的入口，避免用户到设置里再找一层。
 */
@Composable
fun ProfileDetailScreen(
    profile: UserProfile,
    today: LocalDate,
    onBack: () -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onLifespanChange: (Int?) -> Unit
) {
    var lifespanText by remember(profile.expectedLifespanYears) {
        mutableStateOf(profile.expectedLifespanYears?.toString() ?: "")
    }
    var error by remember { mutableStateOf<String?>(null) }

    val countdown = LifeCountdownCalculator.calculate(
        birthDate = profile.birthDate,
        today = today,
        lifespanYears = profile.expectedLifespanYears
    )
    val stats = TimeProgressCalculator.stats(today, profile.birthDate)

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
                text = "生命参数",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        LifeCard(modifier = Modifier.fillMaxWidth()) {
            KeyValueRow(
                "当前日期",
                Format.longDate(today)
            )
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow(
                "年龄",
                countdown.ageYears?.let { "${it} 岁 ${countdown.ageMonths} 个月 ${countdown.ageDays} 天" }
                    ?: "未设置出生日期"
            )
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow("已度过", "${Format.grouped(countdown.daysLived)} 天")
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow(
                "预计终点",
                countdown.expectedEndDate?.let(Format::isoDate) ?: "未启用"
            )
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow(
                "生命进度",
                countdown.progressPercent?.let(Format::percent) ?: "—"
            )
            Spacer(Modifier.height(Spacing.sm))
            KeyValueRow(
                "剩余天数",
                countdown.remainingDays?.let(Format::grouped) ?: "—"
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "出生日期")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            DatePickerField(
                label = "选择出生日期",
                value = profile.birthDate,
                onChange = onBirthDateChange,
                selectableRange = LocalDate.of(1900, 1, 1)..today
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "预期寿命")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = lifespanText,
                onValueChange = {
                    lifespanText = it.filter { c -> c.isDigit() }.take(3)
                    error = null
                },
                label = { Text("岁数（${LifeCountdownCalculator.MIN_LIFESPAN_YEARS}–${LifeCountdownCalculator.MAX_LIFESPAN_YEARS}）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = error != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "这是你自己设定的估算参数，不是医学预测。留空表示不计算剩余天数。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Button(
                    onClick = {
                        val value = lifespanText.toIntOrNull()
                        if (lifespanText.isNotBlank() && !LifeCountdownCalculator.isValidLifespan(value)) {
                            error = "请输入 1 到 120 之间的整数"
                        } else {
                            onLifespanChange(value)
                            error = null
                        }
                    }
                ) { Text("保存预期寿命") }
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "今年的进度")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            PeriodProgressRow("今年", stats.year)
            Spacer(Modifier.height(Spacing.lg))
            PeriodProgressRow("本月", stats.month)
            Spacer(Modifier.height(Spacing.lg))
            PeriodProgressRow("本周", stats.week)
        }

        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "计算口径：以日历日计算，出生当天算 0 天；预计终点 = 出生日期 + 预期寿命年数，2 月 29 日在平年按 2 月 28 日。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start
        )
    }
}
