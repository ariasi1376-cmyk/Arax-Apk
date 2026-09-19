package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AraxDarkColorScheme = darkColorScheme(
    primary = AraxAmber,
    onPrimary = Color.Black,
    primaryContainer = AraxAmberContainer,
    onPrimaryContainer = AraxAmberLight,
    secondary = AraxAmberLight,
    onSecondary = Color.Black,
    tertiary = AraxAmberDark,
    onTertiary = Color.White,
    background = AraxBlack,
    onBackground = AraxTextPrimary,
    surface = AraxDarkSurface,
    onSurface = AraxTextPrimary,
    surfaceVariant = AraxCardBg,
    onSurfaceVariant = AraxTextSecondary,
    outline = AraxBorder,
)

@Composable
fun AraxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AraxDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for backwards compatibility with tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    AraxTheme(content = content)
}
