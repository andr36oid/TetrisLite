package com.yet.tetris.uikit.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** andr36oid system accent: the fallback when the device's own accent can't be read */
val DefaultAccent = Color(0xFFB34700)

private val Grey = Color(0xFFE6E6E6)

/**
 * The console look: true black, light grey text, one accent colour for focus, highlights and
 * primary buttons. [accent] is the device's own accent (read by the platform).
 */
private fun consoleColorScheme(accent: Color) =
    darkColorScheme(
        primary = accent,
        onPrimary = Color.Black,
        primaryContainer = accent,
        onPrimaryContainer = Color.Black,
        secondary = accent,
        onSecondary = Color.Black,
        secondaryContainer = accent,
        onSecondaryContainer = Color.Black,
        tertiary = accent,
        onTertiary = Color.Black,
        background = Color.Black,
        onBackground = Grey,
        surface = Color.Black,
        onSurface = Grey,
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Grey,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF111111),
        surfaceContainer = Color(0xFF111111),
        surfaceContainerHigh = Color(0xFF1A1A1A),
        surfaceContainerHighest = Color(0xFF222222),
        outline = Color(0xFF444444),
        outlineVariant = Color(0xFF333333),
        error = Color(0xFFFF5555),
        onError = Color.Black,
    )

@Composable
fun TetrisLiteAppTheme(
    accent: Color = DefaultAccent,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = consoleColorScheme(accent),
        typography = Typography,
        content = content,
    )
}
