package com.charr0max.polyglot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PineWood,
    secondary = RetroOffWhite,
    tertiary = PineWood,
    background = PhthaloGreen,
    surface = PhthaloGreen,
    onPrimary = PhthaloGreen,
    onSecondary = PhthaloGreen,
    onTertiary = PhthaloGreen,
    onBackground = RetroOffWhite,
    onSurface = RetroOffWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = PhthaloGreen,
    secondary = PineWood,
    tertiary = PineWood,
    background = RetroOffWhite,
    surface = RetroOffWhite,
    onPrimary = RetroOffWhite,
    onSecondary = PhthaloGreen,
    onTertiary = PhthaloGreen,
    onBackground = PhthaloGreen,
    onSurface = PhthaloGreen,
)

@Composable
fun PolyglotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}