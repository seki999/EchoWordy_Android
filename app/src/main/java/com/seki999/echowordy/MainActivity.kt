package com.seki999.echowordy

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.seki999.echowordy.ui.navigation.EchoWordyNavHost
import com.seki999.echowordy.ui.theme.EchoWordyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences by (application as EchoWordyApplication).settingsRepository
                .readingPreferencesFlow().collectAsStateWithLifecycle(
                    initialValue = com.seki999.echowordy.domain.model.ReadingPreferences(),
                )
            EchoWordyTheme(preferences = preferences) {
                val background = MaterialTheme.colorScheme.background
                SideEffect {
                    window.statusBarColor = background.toArgb()
                    window.navigationBarColor = background.toArgb()
                    val light = preferences.theme != com.seki999.echowordy.domain.model.ReadingTheme.DARK
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = light
                        isAppearanceLightNavigationBars = light
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    EchoWordyNavHost()
                }
            }
        }
    }
}
