package com.liferemaining.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * 排版：系统字体；关键数字使用等宽数字（tnum），避免数字变化造成布局抖动。
 */
private val tightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun base(
    size: Int,
    weight: FontWeight,
    lineHeight: Int,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = tightLineHeight
)

val LifeTypography = Typography(
    displayLarge = base(56, FontWeight.Light, 60, -1.2),
    displayMedium = base(44, FontWeight.Light, 48, -1.0),
    displaySmall = base(34, FontWeight.Normal, 40, -0.6),
    headlineLarge = base(28, FontWeight.Medium, 34, -0.3),
    headlineMedium = base(24, FontWeight.Medium, 30, -0.2),
    headlineSmall = base(20, FontWeight.Medium, 26),
    titleLarge = base(18, FontWeight.Medium, 24),
    titleMedium = base(15, FontWeight.Medium, 21),
    titleSmall = base(13, FontWeight.Medium, 18),
    bodyLarge = base(15, FontWeight.Normal, 23),
    bodyMedium = base(14, FontWeight.Normal, 21),
    bodySmall = base(12, FontWeight.Normal, 18),
    labelLarge = base(13, FontWeight.Medium, 18, 0.2),
    labelMedium = base(11, FontWeight.Medium, 15, 0.4),
    labelSmall = base(10, FontWeight.Medium, 14, 0.6)
)

/** 关键数字：等宽。单独暴露给主卡片与统计卡使用。 */
val NumericStyle = base(56, FontWeight.Light, 60, -1.4).copy(
    fontFeatureSettings = "tnum"
)

val NumericStyleSmall = base(26, FontWeight.Normal, 30, -0.6).copy(
    fontFeatureSettings = "tnum"
)
