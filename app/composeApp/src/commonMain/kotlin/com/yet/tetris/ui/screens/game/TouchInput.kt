package com.yet.tetris.ui.screens.game

import androidx.compose.runtime.Composable

/**
 * Whether there is a touchscreen to tap the on-screen buttons with. Game consoles without
 * one play with their buttons, so the touch-only buttons stay hidden there.
 */
@Composable
expect fun rememberHasTouchscreen(): Boolean
