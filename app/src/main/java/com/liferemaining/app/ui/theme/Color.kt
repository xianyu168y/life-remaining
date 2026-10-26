package com.liferemaining.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.liferemaining.app.domain.model.AccentKey

/**
 * KernelSU / Miuix 风格的低饱和度配色。
 * 深色是默认语境：接近黑的深灰底 + 稍亮卡片 + 中性灰次要文字。
 * 强调色克制，只用于进度、当前状态与关键数字。
 */
internal object Palette {
    // 深色
    val DarkBackground = Color(0xFF101312)
    val DarkSurface = Color(0xFF171B1A)
    val DarkSurfaceHigh = Color(0xFF1E2322)
    val DarkSurfaceHigher = Color(0xFF252B29)
    val DarkOutline = Color(0xFF2E3533)
    val DarkTextPrimary = Color(0xFFE7ECEA)
    val DarkTextSecondary = Color(0xFF9AA5A1)
    val DarkTextTertiary = Color(0xFF6F7976)

    // 浅色
    val LightBackground = Color(0xFFF4F5F3)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceHigh = Color(0xFFFAFBFA)
    val LightOutline = Color(0xFFE1E4E0)
    val LightTextPrimary = Color(0xFF1A1E1C)
    val LightTextSecondary = Color(0xFF5F6965)
    val LightTextTertiary = Color(0xFF8C948F)

    val Error = Color(0xFFC9584F)
    val ErrorDark = Color(0xFFE07C72)
}

/** 强调色不跟随 Material 的动态取色，保持品牌一致的低饱和度。 */
internal object Accents {
    val Teal = Color(0xFF6FC6B2)
    val TealDark = Color(0xFF4E9E8D)
    val Moss = Color(0xFF8FB37C)
    val MossDark = Color(0xFF6E8F5F)
    val Blue = Color(0xFF7FA6C9)
    val BlueDark = Color(0xFF5C7F9E)
    val Amber = Color(0xFFC9A46F)
    val AmberDark = Color(0xFF9E7E4F)

    fun forKey(key: AccentKey): Color = when (key) {
        AccentKey.TEAL -> Teal
        AccentKey.MOSS -> Moss
        AccentKey.BLUE -> Blue
        AccentKey.AMBER -> Amber
    }

    fun forKeyOnLight(key: AccentKey): Color = when (key) {
        AccentKey.TEAL -> TealDark
        AccentKey.MOSS -> MossDark
        AccentKey.BLUE -> BlueDark
        AccentKey.AMBER -> AmberDark
    }
}
