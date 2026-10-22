package com.yet.tetris.ui.screens.game.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yet.tetris.feature.game.GameComponent
import org.jetbrains.compose.resources.stringResource
import tetrislite.composeapp.generated.resources.Res
import tetrislite.composeapp.generated.resources.back_to_home
import tetrislite.composeapp.generated.resources.final_score
import tetrislite.composeapp.generated.resources.game_over
import tetrislite.composeapp.generated.resources.lines_cleared
import tetrislite.composeapp.generated.resources.retry

@Composable
fun GameOverDialog(
    component: GameComponent,
    model: GameComponent.Model,
) {
    GameDialogPage(
        leading = "Game over",
        hints = listOf("A" to "Select", "B" to "Home"),
        onBack = component::onQuit,
    ) {
        Text(
            text = stringResource(Res.string.game_over),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        val gameState = model.gameState
        Text(
            text = stringResource(Res.string.final_score, model.finalScore),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.lines_cleared, model.finalLinesCleared),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        if (gameState != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Level ${gameState.level} \u2022 ${formatDuration(model.elapsedTime)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Pieces ${gameState.piecesPlaced} \u2022 Max combo ${gameState.maxCombo}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text =
                    "Tetrises ${gameState.tetrisesCleared} \u2022 " +
                        "T-Spins ${gameState.tSpinClears} \u2022 " +
                        "Perfect clears ${gameState.perfectClears}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = component::onRetry, modifier = Modifier.initialFocus()) {
                Text(stringResource(Res.string.retry))
            }
            OutlinedButton(onClick = component::onQuit, modifier = Modifier.dialogFocusRing()) {
                Text(stringResource(Res.string.back_to_home))
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / 1000) / 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
