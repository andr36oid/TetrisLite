package com.yet.tetris.ui.screens.game.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tetrislite.composeapp.generated.resources.Res
import tetrislite.composeapp.generated.resources.error_title
import tetrislite.composeapp.generated.resources.ok

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    GameDialogPage(
        leading = "Error",
        hints = listOf("A" to "Ok", "B" to "Close"),
        onBack = onDismiss,
    ) {
        Text(
            text = stringResource(Res.string.error_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(text = message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDismiss, modifier = Modifier.initialFocus()) {
            Text(stringResource(Res.string.ok))
        }
    }
}
