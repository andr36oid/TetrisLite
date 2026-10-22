package com.yet.tetris.ui.screens.game.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yet.tetris.uikit.component.hint.HintBar

/**
 * A game dialog as a full-screen page in the console look, with the button hints along the
 * bottom. B does [onBack] (the same as the platform back key); the buttons in [content] take
 * the D-pad and A as usual.
 *
 * @param leading the name of the dialog, on the left of the hint bar
 * @param hints pairs of button and what it does
 */
@Composable
internal fun GameDialogPage(
    leading: String,
    hints: List<Pair<String, String>>,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onPreviewKeyEvent {
                        if (it.key == Key.ButtonB && it.type == KeyEventType.KeyUp) {
                            onBack()
                            true
                        } else {
                            false
                        }
                    },
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    content = content,
                )
                HintBar(leading = leading, hints = hints)
            }
        }
    }
}
