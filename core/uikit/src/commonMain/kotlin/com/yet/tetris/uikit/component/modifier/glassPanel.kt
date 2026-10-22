package com.yet.tetris.uikit.component.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A flat dark panel with a thin grey edge: the console look (no gradients, no shadows).
 *
 * @param shape The shape of the panel.
 */
fun Modifier.glassPanel(
    shape: Shape,
    @Suppress("UNUSED_PARAMETER") shadowElevation: Dp = 0.dp,
): Modifier =
    this
        .clip(shape)
        .background(Color(0xFF111111), shape)
        .border(1.dp, Color(0xFF3A3A3A), shape)

/** A convenience overload of [glassPanel] for rounded rectangles. */
fun Modifier.glassPanel(
    cornerRadius: Dp,
    shadowElevation: Dp = 0.dp,
): Modifier = glassPanel(shape = RoundedCornerShape(cornerRadius), shadowElevation = shadowElevation)
