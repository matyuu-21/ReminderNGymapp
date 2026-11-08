package com.example.remindergym.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color.Black,
    background = DarkBackground,
    surface = DarkBackground,
    surfaceContainerHighest = DarkCard,
    onSurfaceVariant = Color(0xFFB8B8B8)
)

private val LightColors = lightColorScheme(
    primary = OrangeDark,
    onPrimary = Color.White,
    background = LightBackground,
    surface = LightBackground,
    surfaceContainerHighest = LightCard,
    onSurfaceVariant = Color(0xFF5E5650)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ReminderGymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}