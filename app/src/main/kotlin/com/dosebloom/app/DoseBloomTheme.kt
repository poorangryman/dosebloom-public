package com.dosebloom.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DoseLight = lightColorScheme(
    primary = Color(0xFF2A6B55),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6EFE3),
    onPrimaryContainer = Color(0xFF04281B),
    secondary = Color(0xFF4C6357),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9D9),
    onSecondaryContainer = Color(0xFF092016),
    tertiary = Color(0xFF3B6470),
    background = Color(0xFFF7F9F8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFEDF2EE),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFDCE3DF)
)

val DoseDark = darkColorScheme(
    primary = Color(0xFF7FD1A7),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF124E38),
    onPrimaryContainer = Color(0xFFA1F2C5),
    secondary = Color(0xFFB3CCBE),
    onSecondary = Color(0xFF1E352A),
    secondaryContainer = Color(0xFF354B40),
    onSecondaryContainer = Color(0xFFCEE9D9),
    tertiary = Color(0xFFA2CDE0),
    background = Color(0xFF111513),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF181E1B),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF232B27),
    onSurfaceVariant = Color(0xFFC0C9C2),
    outline = Color(0xFF8A938D),
    outlineVariant = Color(0xFF343D37)
)

@Composable
fun DoseBloomTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DoseDark else DoseLight,
        content = content
    )
}
