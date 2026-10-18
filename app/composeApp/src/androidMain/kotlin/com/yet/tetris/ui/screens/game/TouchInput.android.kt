package com.yet.tetris.ui.screens.game

import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.view.InputDevice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberHasTouchscreen(): Boolean {
    val context = LocalContext.current
    val feature =
        remember(context) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        }
    if (!feature) return false

    // A touchscreen can come and go: andr36oid's touch controls add a virtual one while on
    var hasDevice by remember { mutableStateOf(anyTouchscreenDevice()) }
    DisposableEffect(context) {
        val inputManager = context.getSystemService(InputManager::class.java)
        val listener =
            object : InputManager.InputDeviceListener {
                override fun onInputDeviceAdded(deviceId: Int) {
                    hasDevice = anyTouchscreenDevice()
                }

                override fun onInputDeviceRemoved(deviceId: Int) {
                    hasDevice = anyTouchscreenDevice()
                }

                override fun onInputDeviceChanged(deviceId: Int) {
                    hasDevice = anyTouchscreenDevice()
                }
            }
        inputManager?.registerInputDeviceListener(listener, null)
        onDispose { inputManager?.unregisterInputDeviceListener(listener) }
    }
    return hasDevice
}

private fun anyTouchscreenDevice(): Boolean =
    InputDevice.getDeviceIds().any { id ->
        InputDevice.getDevice(id)?.supportsSource(InputDevice.SOURCE_TOUCHSCREEN) == true
    }
