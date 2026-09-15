package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AmberGlowPrimary,
    onPrimary = Color.Black,
    primaryContainer = AmberGlowDark,
    onPrimaryContainer = Color.White,
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = ElectricCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = EmeraldSuccess,
    background = ObsidianDarkBg,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianDarkSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianDarkCard,
    onSurfaceVariant = ObsidianTextSecondary,
    outline = ObsidianDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AmberGlowDark,
    onPrimary = Color.White,
    primaryContainer = AmberGlowLight,
    onPrimaryContainer = Color.Black,
    secondary = ElectricCyanDark,
    onSecondary = Color.White,
    secondaryContainer = ElectricCyan,
    onSecondaryContainer = Color.Black,
    tertiary = EmeraldSuccess,
    background = StudioLightBg,
    onBackground = StudioTextPrimary,
    surface = StudioLightSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioLightCard,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioLightBorder
)

@Composable
fun MersaSignageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature architectural brand palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
