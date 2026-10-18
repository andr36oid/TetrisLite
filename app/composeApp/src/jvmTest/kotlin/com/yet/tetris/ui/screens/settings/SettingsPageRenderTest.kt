package com.yet.tetris.ui.screens.settings

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import com.yet.tetris.feature.settings.PreviewSettingsComponent
import com.yet.tetris.uikit.theme.TetrisLiteAppTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test

/**
 * Renders the settings page at the consoles' screen sizes (density 178 dpi, like the
 * consoles), once on the sections and once inside the Audio controls. Writes PNGs to
 * TETRIS_SCREENSHOT_DIR when it is set, for a look at the layout.
 */
@OptIn(InternalComposeUiApi::class)
class SettingsPageRenderTest {
    @Test
    fun rendersAtConsoleSizes() {
        val out = System.getenv("TETRIS_SCREENSHOT_DIR")?.let(::File)
        listOf(640 to 480, 720 to 720, 480 to 640).forEach { (width, height) ->
            ImageComposeScene(width, height, Density(178f / 160f)) {
                TetrisLiteAppTheme { SettingsPage(PreviewSettingsComponent()) }
            }.use { scene ->
                // an image scene has no window that could have the focus; pretend it does,
                // as the dialog's window does on the console
                ImageComposeScene::class.java.getDeclaredField("_windowInfo").run {
                    isAccessible = true
                    val info = get(scene)
                    info.javaClass.getMethod("setWindowFocused", Boolean::class.java).invoke(info, true)
                }
                var time = 0L
                fun frame(name: String) {
                    repeat(3) { scene.render(time); time += 50_000_000L }
                    val image = scene.render(time)
                    out?.let { File(it, "settings-$name-${width}x$height.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes) }
                }
                fun press(key: Key) {
                    scene.sendKeyEvent(KeyEvent(key, KeyEventType.KeyDown))
                    scene.sendKeyEvent(KeyEvent(key, KeyEventType.KeyUp))
                    scene.render(time)
                    time += 16_000_000L
                }
                frame("sections")
                val portrait = height > width
                // to the Audio section and into its controls, then down to the theme chips
                repeat(2) { press(if (portrait) Key.DirectionRight else Key.DirectionDown) }
                press(if (portrait) Key.DirectionDown else Key.DirectionRight)
                frame("audio")
                repeat(2) { press(Key.DirectionDown) }
                press(Key.DirectionRight)
                frame("audio-chips")
            }
        }
    }
}
