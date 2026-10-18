package com.yet.tetris.ui.screens.game

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.yet.tetris.domain.model.game.GameBoard
import com.yet.tetris.domain.model.game.GameState
import com.yet.tetris.domain.model.game.Position
import com.yet.tetris.domain.model.game.Tetromino
import com.yet.tetris.domain.model.game.TetrominoType
import com.yet.tetris.domain.model.game.RotationDirection
import com.yet.tetris.feature.game.GameComponent
import com.yet.tetris.uikit.theme.TetrisLiteAppTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Which layout the consoles' screens get, and a render of the game screen at their sizes
 * (178 dpi like the consoles). Writes PNGs to TETRIS_SCREENSHOT_DIR when it is set.
 */
class GameLayoutRenderTest {
    private val density = 178f / 160f

    private fun dp(px: Int) = (px / density).dp

    @Test
    fun consolesInLandscapeGetTheHandheldLayout() {
        assertTrue(isHandheldLandscape(dp(640), dp(480)), "R36S 640x480")
        assertTrue(isHandheldLandscape(dp(720), dp(720)), "720x720")
        assertFalse(isHandheldLandscape(dp(480), dp(640)), "D-28S portrait")
        assertFalse(isHandheldLandscape(780.dp, 360.dp), "phone in landscape")
        assertFalse(isHandheldLandscape(1280.dp, 800.dp), "tablet in landscape")
        assertFalse(isHandheldLandscape(360.dp, 780.dp), "phone in portrait")
    }

    @Test
    fun rendersAtConsoleSizes() {
        val out = System.getenv("TETRIS_SCREENSHOT_DIR")?.let(::File)
        val cells =
            buildMap {
                for (x in 0 until 9) put(Position(x, 19), TetrominoType.entries[x % 7])
                for (x in 1 until 10) put(Position(x, 18), TetrominoType.entries[(x + 3) % 7])
            }
        val state =
            GameState(
                board = GameBoard(cells = cells),
                currentPiece = Tetromino.create(TetrominoType.T),
                currentPosition = Position(4, 2),
                nextPiece = Tetromino.create(TetrominoType.I),
                nextQueue = listOf(TetrominoType.O, TetrominoType.S, TetrominoType.L, TetrominoType.J).map { Tetromino.create(it) },
                holdPiece = Tetromino.create(TetrominoType.Z),
                score = 12345,
                linesCleared = 42,
                level = 5,
            )
        val model = GameComponent.Model(isLoading = false, gameState = state, elapsedTime = 83_000, ghostPieceY = 15)
        val actions =
            GameInputActions(
                {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {},
                RotationDirection.CLOCKWISE, false,
            )
        listOf(640 to 480, 720 to 720, 480 to 640).forEach { (width, height) ->
            ImageComposeScene(width, height, Density(density)) {
                TetrisLiteAppTheme {
                    GamePlayingContent(model, actions, rememberJuiceOverlayState())
                }
            }.use { scene ->
                val image = scene.render(0)
                out?.let { File(it, "game-${width}x$height.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes) }
            }
        }
    }
}
