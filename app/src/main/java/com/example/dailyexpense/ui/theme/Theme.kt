package com.example.dailyexpense.ui.theme

import android.app.Activity
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
    primary = SkyBlueDarkPrimary,
    onPrimary = SkyBlueDarkOnPrimary,
    primaryContainer = SkyBlueDarkPrimaryContainer,
    onPrimaryContainer = SkyBlueDarkOnPrimaryContainer,

    secondary = SoftOrangeDarkSecondary,
    onSecondary = SoftOrangeDarkOnSecondary,
    secondaryContainer = SoftOrangeDarkSecondaryContainer,
    onSecondaryContainer = SoftOrangeDarkOnSecondaryContainer,

    tertiary = GentleTealDarkTertiary,
    onTertiary = GentleTealDarkOnTertiary,
    tertiaryContainer = GentleTealDarkTertiaryContainer,
    onTertiaryContainer = GentleTealDarkOnTertiaryContainer,

    error = ErrorColorDark,
    onError = OnErrorColorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,

    background = DarkGray, // Using a common dark background
    onBackground = TextColorDark,

    surface = DarkGray, // Can be same as background or slightly different
    onSurface = TextColorDark,
    surfaceVariant = Color(0xFF42474E), // A neutral dark gray for variants
    onSurfaceVariant = Color(0xFFC2C7CE),

    outline = Color(0xFF8C9198),
    outlineVariant = Color(0xFF42474E),

    scrim = Color.Black,
    inverseSurface = LightGray, // Light surface for inverse
    inversePrimary = SkyBlueLightPrimary // Light theme's primary as inverse
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlueLightPrimary,
    onPrimary = SkyBlueLightOnPrimary,
    primaryContainer = SkyBlueLightPrimaryContainer,
    onPrimaryContainer = SkyBlueLightOnPrimaryContainer,

    secondary = SoftOrangeLightSecondary,
    onSecondary = SoftOrangeLightOnSecondary,
    secondaryContainer = SoftOrangeLightSecondaryContainer,
    onSecondaryContainer = SoftOrangeLightOnSecondaryContainer,

    tertiary = GentleTealLightTertiary,
    onTertiary = GentleTealLightOnTertiary,
    tertiaryContainer = GentleTealLightTertiaryContainer,
    onTertiaryContainer = GentleTealLightOnTertiaryContainer,

    error = ErrorColorLight,
    onError = OnErrorColorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,

    background = LightGray, // Using a common light background
    onBackground = TextColorLight,

    surface = LightGray, // Can be same as background or slightly different
    onSurface = TextColorLight,
    surfaceVariant = Color(0xFFDEE3EA), // A neutral light gray for variants
    onSurfaceVariant = Color(0xFF42474E),

    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC2C7CE),

    scrim = Color.Black,
    inverseSurface = DarkGray, // Dark surface for inverse
    inversePrimary = SkyBlueDarkPrimary // Dark theme's primary as inverse
)

@Composable
fun DailyExpenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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