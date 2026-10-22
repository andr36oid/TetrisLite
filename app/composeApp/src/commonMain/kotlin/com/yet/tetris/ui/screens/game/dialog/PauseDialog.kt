package com.yet.tetris.ui.screens.game.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import tetrislite.composeapp.generated.resources.game_paused
import tetrislite.composeapp.generated.resources.game_settings
import tetrislite.composeapp.generated.resources.pause_message
import tetrislite.composeapp.generated.resources.quit
import tetrislite.composeapp.generated.resources.resume

@Composable
fun PauseDialog(component: GameComponent) {
    GameDialogPage(
        leading = "Paused",
        hints = listOf("D-pad" to "Move", "A" to "Select", "B" to "Resume"),
        onBack = component::onResume,
    ) {
        Text(
            text = stringResource(Res.string.game_paused),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(text = stringResource(Res.string.pause_message), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = component::onResume, modifier = Modifier.initialFocus()) {
                Text(stringResource(Res.string.resume))
            }
            OutlinedButton(onClick = component::onQuit, modifier = Modifier.dialogFocusRing()) {
                Text(stringResource(Res.string.quit))
            }
            OutlinedButton(onClick = component::onSettings, modifier = Modifier.dialogFocusRing()) {
                Text(stringResource(Res.string.game_settings))
            }
        }
    }
}
