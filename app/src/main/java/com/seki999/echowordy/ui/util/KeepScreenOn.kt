package com.seki999.echowordy.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * Keeps the device screen from dimming or locking while this composable is
 * part of the composition. Cards auto-advance without any touch input, so
 * without this the screen would time out mid-review.
 */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }
}
