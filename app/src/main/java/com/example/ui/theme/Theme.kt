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

private val DarkColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = PrimaryNavy,
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = SlateDark,
    surface = Color.White,
    onSurface = SlateDark,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = SlateMedium,
    outline = SlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = PrimaryNavy,
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = EmeraldGreenSubtle,
    onSecondaryContainer = EmeraldGreen,
    background = Color.White,
    onBackground = SlateDark,
    surface = Color.White,
    onSurface = SlateDark,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = SlateMedium,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

