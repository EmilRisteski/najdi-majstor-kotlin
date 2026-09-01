package com.example.najdimajstor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = NajdiNavy,
    secondary = NajdiGold,
    background = NajdiBackgroundLight,
    surface = NajdiSurfaceLight,
    onPrimary = NajdiTextLight,
    onSecondary = NajdiTextDark,
    onBackground = NajdiTextDark,
    onSurface = NajdiTextDark,
    error = NajdiError
)

private val DarkColorScheme = darkColorScheme(
    primary = NajdiGold,
    secondary = NajdiNavyLight,
    background = NajdiBackgroundDark,
    surface = NajdiSurfaceDark,
    onPrimary = NajdiTextDark,
    onSecondary = NajdiTextLight,
    onBackground = NajdiTextLight,
    onSurface = NajdiTextLight,
    error = NajdiError
)

@Composable
fun NajdiMajstorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            val backgroundColor = colorScheme.background.toArgb()

            window.statusBarColor = backgroundColor
            window.navigationBarColor = backgroundColor
            window.decorView.setBackgroundColor(backgroundColor)

            val insetsController = WindowCompat.getInsetsController(window, view)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}