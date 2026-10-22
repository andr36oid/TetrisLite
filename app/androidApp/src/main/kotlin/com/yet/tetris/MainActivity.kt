package com.yet.tetris

import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.defaultComponentContext
import com.yet.tetris.di.createRootComponent

class MainActivity : ComponentActivity() {
    // the device's accent (Settings > Display > Styles), read again on every resume so a
    // change shows without reinstalling
    private var systemAccent by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        systemAccent = readSystemAccent()

        val appGraph = (application as TetrisApp).appGraph

        val root =
            createRootComponent(
                componentContext = defaultComponentContext(),
                graph = appGraph,
            )

        setContent {
            App(root, systemAccentArgb = systemAccent)
        }
    }

    override fun onResume() {
        super.onResume()
        systemAccent = readSystemAccent()
    }

    /** The system's colorAccent as ARGB, or null if the device doesn't give one */
    private fun readSystemAccent(): Int? {
        val themed = ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault)
        val value = TypedValue()
        if (!themed.theme.resolveAttribute(android.R.attr.colorAccent, value, true)) return null
        if (value.type < TypedValue.TYPE_FIRST_COLOR_INT || value.type > TypedValue.TYPE_LAST_COLOR_INT) return null
        return value.data or 0xFF000000.toInt()
    }
}
