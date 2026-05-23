package com.honorshots.screenshot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private val DarkGoldColorScheme = darkColorScheme(
    primary = DarkGold,
    onPrimary = MatteBlack,
    primaryContainer = DarkGoldDim,
    onPrimaryContainer = DarkGold,
    secondary = DarkGoldLight,
    onSecondary = MatteBlack,
    secondaryContainer = DarkGoldDim,
    onSecondaryContainer = DarkGold,
    tertiary = DarkGoldLight,
    onTertiary = MatteBlack,
    tertiaryContainer = DarkGoldDim,
    onTertiaryContainer = DarkGold,
    background = MatteBlack,
    onBackground = TextGold,
    surface = SurfaceDark,
    onSurface = TextGold,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextGoldSecondary,
    error = Error,
    onError = MatteBlack,
    outline = GoldDivider,
    outlineVariant = GoldLine
)

@Composable
fun HonorScreenshotsTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkGoldColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // 设置状态栏和导航栏为纯黑底色
            window.statusBarColor = MatteBlack.toArgb()
            window.navigationBarColor = MatteBlack.toArgb()
            
            // 沉浸式：内容延伸到系统栏后面
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            val controller = WindowInsetsControllerCompat(window, view)
            // 状态栏文字设为暗金色（因为背景是纯黑）
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
