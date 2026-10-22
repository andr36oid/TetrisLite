package com.yet.tetris

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.yet.tetris.feature.root.PreviewRootComponent
import com.yet.tetris.feature.root.RootComponent
import com.yet.tetris.ui.screens.root.RootScreen
import com.yet.tetris.uikit.theme.DefaultAccent
import com.yet.tetris.uikit.theme.TetrisLiteAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * @param systemAccentArgb the device's accent colour as ARGB, or null when it couldn't be read
 *   (then the andr36oid orange is used)
 */
@Composable
@Preview
fun App(
    rootComponent: RootComponent = PreviewRootComponent(),
    systemAccentArgb: Int? = null,
) {
    TetrisLiteAppTheme(accent = systemAccentArgb?.let { Color(it) } ?: DefaultAccent) {
        RootScreen(rootComponent)
    }
}
