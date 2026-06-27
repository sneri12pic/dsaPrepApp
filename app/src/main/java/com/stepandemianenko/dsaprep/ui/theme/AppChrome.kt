package com.stepandemianenko.dsaprep.ui.theme

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun premiumBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFBF3F2),
        Color(0xFFF6E9EC)
    )
)

// ponytail: alpha param kept so callers compile, intentionally ignored — solid cards now.
@Composable
fun glassCardColors(alpha: Float = 1f): CardColors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface
)

// ponytail: glow muddied the light background; no-op keeps call sites compiling.
fun Modifier.dustGlow(): Modifier = this
