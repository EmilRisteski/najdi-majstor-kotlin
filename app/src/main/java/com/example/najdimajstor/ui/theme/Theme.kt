package com.example.najdimajstor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}