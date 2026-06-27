package com.stepandemianenko.dsaprep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light, non-aggressive dusty-pink palette. Role values picked to clear WCAG AA
// (4.5:1) for text on the surface each role sits on.
private val DustyPinkColors = lightColorScheme(
    primary = Color(0xFFA8576E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF7D9E0),
    onPrimaryContainer = Color(0xFF43101F),

    secondary = Color(0xFF9C6F62),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3DED6),
    onSecondaryContainer = Color(0xFF3A211A),

    tertiary = Color(0xFF9A7B4F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3E6C9),
    onTertiaryContainer = Color(0xFF3A2C0E),

    background = Color(0xFFFBF3F2),
    onBackground = Color(0xFF2C2024),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2C2024),
    surfaceVariant = Color(0xFFF1E4E6),
    onSurfaceVariant = Color(0xFF6E5860),
    outline = Color(0xFFB79AA1),
    outlineVariant = Color(0xFFE4D2D6)
)

@Composable
fun DsaPrepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DustyPinkColors,
        content = content
    )
}
