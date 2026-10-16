package com.yet.tetris.ui.screens.game.dialog

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.yet.tetris.uikit.component.modifier.focusRing

/**
 * For the button a dialog's D-pad or keyboard user most likely wants: it has the focus as
 * soon as the dialog opens, instead of the first key press only moving into the dialog.
 */
@Composable
internal fun Modifier.initialFocus(): Modifier {
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        requester.requestFocus()
    }
    return this.focusRequester(requester).dialogFocusRing()
}

/** The focus ring in the shape of Material's dialog buttons */
internal fun Modifier.dialogFocusRing(): Modifier = focusRing(CircleShape)
