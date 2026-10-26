package com.liferemaining.app.ui.dashboard

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liferemaining.app.domain.calculator.TimeProgressCalculator
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.PeriodProgressRow
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.components.WeekStrip
import com.liferemaining.app.ui.theme.Spacing
import com.liferemaining.app.util.Format
import java.time.LocalDate
import java.time.Month

/** 首页统计卡的详情页。period 取 life / year / month / week。 */
@Composable
fun PeriodDetailScreen(
    period: String,
    today: LocalDate,
    daysLived: Long,
    birthDate: LocalDate?,
    onBack: () -> Unit
) {
    val year = TimeProgressCalculator.yearProgress(today)
    val month = TimeProgressCalculator.monthProgress(today)
    val week = TimeProgressCalculator.weekProgress(today)

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
                text = when (period) {
                    "life" -> "已度过的日子"
                    "year" -> "今年"
                    "month" -> "本月"
                    else -> "本周"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        when (period) {
            "life" -> {
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Format.grouped(daysLived),
                        style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "天，从 ${birthDate?.let(Format::longDate) ?: "未设置出生日期"} 到今天",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.lg))
                SectionHeader(title = "换算")
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    val years = daysLived / 365
                    Text(
                        text = "约 ${years} 年，${daysLived / 7} 周，${Format.grouped(daysLived * 24)} 小时。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "换算值按 365 天/年、7 天/周计算，只用于直观感受；所有正式统计仍以日历日为准。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            "year" -> {
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    PeriodProgressRow("今年", year)
                }
                Spacer(Modifier.height(Spacing.lg))
                SectionHeader(title = "逐月")
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    (1..12).forEach { m ->
                        val monthDate = LocalDate.of(today.year, m, 1)
                        val total = monthDate.lengthOfMonth()
                        val elapsed = when {
                            m < today.monthValue -> total
                            m > today.monthValue -> 0
                            else -> today.dayOfMonth
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Month.of(m).getDisplayName(
                                    java.time.format.TextStyle.SHORT,
                                    java.util.Locale.SIMPLIFIED_CHINESE
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$elapsed / $total 天",
                                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(Spacing.sm))
                    }
                }
            }

            "month" -> {
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    PeriodProgressRow("本月", month)
                }
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = "本月共 ${month.total} 天，已过去 ${month.elapsed} 天，还剩 ${month.remaining} 天（含今天之后的完整日子）。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                LifeCard(modifier = Modifier.fillMaxWidth()) {
                    PeriodProgressRow("本周", week)
                    Spacer(Modifier.height(Spacing.lg))
                    WeekStrip(days = TimeProgressCalculator.weekDays(today))
                }
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = "一周从周一开始。今天算作已经过去的一天。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
