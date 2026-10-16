package com.yet.tetris.uikit.component.modifier

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Draws a ring in the primary color around the element while it, or something in it, has
 * the focus. Material's own focus overlay is barely visible on the glass buttons, so with
 * a D-pad or a keyboard it was hard to tell what is selected.
 *
 * @param shape The shape of the element the ring goes around.
 */
fun Modifier.focusRing(shape: Shape): Modifier =
    composed {
        var focused by remember { mutableStateOf(false) }
        val color = MaterialTheme.colorScheme.primary
        this
            .onFocusChanged { focused = it.hasFocus }
            .then(if (focused) Modifier.border(3.dp, color, shape) else Modifier)
    }
