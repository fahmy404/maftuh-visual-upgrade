package com.example.maftuh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NoirColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = NoirDark,
    primaryContainer = AmberGoldDark,
    onPrimaryContainer = AmberGoldLight,
    secondary = ForensicGreen,
    onSecondary = NoirDark,
    secondaryContainer = ForensicGreenDark,
    onSecondaryContainer = ForensicGreenLight,
    tertiary = CrimeRed,
    onTertiary = TextPrimary,
    background = NoirDark,
    onBackground = TextPrimary,
    surface = NoirSurface,
    onSurface = TextPrimary,
    surfaceVariant = NoirSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NoirBorder
)

@Composable
fun MaftuhTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NoirColorScheme,
        content = content
    )
}
