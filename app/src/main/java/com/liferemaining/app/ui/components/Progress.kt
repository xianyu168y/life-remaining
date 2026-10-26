package com.liferemaining.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liferemaining.app.domain.model.PeriodProgress
import com.liferemaining.app.ui.theme.Spacing
import kotlin.math.roundToInt

/**
 * 生命进度环。低饱和强调色，单色描边，不做渐变与发光。
 * 值变化用 240ms 的补间动画；系统开启「减少动画」时 Compose 会自动缩短时长。
 */
@Composable
fun LifeProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 148.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    center: @Composable () -> Unit
) {
    val raw = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = raw,
        animationSpec = tween(durationMillis = 240),
        label = "life-progress-ring"
    )
    val percent = (raw * 100).roundToInt()
    Box(
        modifier = modifier
            .size(ringSize)
            .semantics { contentDescription = "生命进度 $percent%" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (animated > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        center()
    }
}

/** 线性进度条：用于年度 / 月度 / 周进度。 */
@Composable
fun ProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200),
        label = "progress-bar"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2f))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(RoundedCornerShape(height / 2f))
                .background(color)
        )
    }
}

@Composable
fun PeriodProgressRow(
    label: String,
    progress: PeriodProgress,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${progress.percent.roundToInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        ProgressBar(fraction = progress.fraction, color = color)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "已过去 ${progress.elapsed} 天 · 还剩 ${progress.remaining} 天",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
