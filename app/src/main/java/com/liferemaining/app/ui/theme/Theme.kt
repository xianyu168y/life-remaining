package com.liferemaining.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.liferemaining.app.domain.model.AccentKey
import com.liferemaining.app.domain.model.ThemeMode

@Composable
fun LifeRemainingTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentKey: AccentKey = AccentKey.TEAL,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> darkScheme(accentKey)
        else -> lightScheme(accentKey)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // enableEdgeToEdge 已把系统栏设为透明；这里只同步图标明暗，
            // 不再直接改 statusBarColor / navigationBarColor（Android 15 起已弃用且无效）。
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    MaterialTheme(colorScheme = scheme, typography = LifeTypography, shapes = LifeShapes, content = content)
}

private fun darkScheme(accentKey: AccentKey) = darkColorScheme(
    primary = Accents.forKey(accentKey),
    onPrimary = Color(0xFF0B1210),
    primaryContainer = Color(0xFF20302C),
    onPrimaryContainer = Accents.forKey(accentKey),
    secondary = Palette.DarkTextSecondary,
    onSecondary = Palette.DarkBackground,
    secondaryContainer = Palette.DarkSurfaceHigher,
    onSecondaryContainer = Palette.DarkTextPrimary,
    background = Palette.DarkBackground,
    onBackground = Palette.DarkTextPrimary,
    surface = Palette.DarkSurface,
    onSurface = Palette.DarkTextPrimary,
    surfaceVariant = Palette.DarkSurfaceHigh,
    onSurfaceVariant = Palette.DarkTextSecondary,
    surfaceContainer = Palette.DarkSurface,
    surfaceContainerHigh = Palette.DarkSurfaceHigh,
    surfaceContainerHighest = Palette.DarkSurfaceHigher,
    outline = Palette.DarkOutline,
    outlineVariant = Color(0xFF252B29),
    error = Palette.ErrorDark,
    onError = Color(0xFF1A0F0D),
    errorContainer = Color(0xFF3A211E),
    onErrorContainer = Palette.ErrorDark
)

private fun lightScheme(accentKey: AccentKey) = lightColorScheme(
    primary = Accents.forKeyOnLight(accentKey),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDEEE8),
    onPrimaryContainer = Color(0xFF17332C),
    secondary = Palette.LightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDEFEC),
    onSecondaryContainer = Palette.LightTextPrimary,
    background = Palette.LightBackground,
    onBackground = Palette.LightTextPrimary,
    surface = Palette.LightSurface,
    onSurface = Palette.LightTextPrimary,
    surfaceVariant = Palette.LightSurfaceHigh,
    onSurfaceVariant = Palette.LightTextSecondary,
    surfaceContainer = Palette.LightSurface,
    surfaceContainerHigh = Palette.LightSurfaceHigh,
    surfaceContainerHighest = Color(0xFFF1F3F1),
    outline = Palette.LightOutline,
    outlineVariant = Color(0xFFEEF0ED),
    error = Palette.Error,
    onError = Color.White,
    errorContainer = Color(0xFFF7E2DF),
    onErrorContainer = Color(0xFF7B2C25)
)
