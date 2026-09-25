package com.seki999.echowordy.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seki999.echowordy.domain.usecase.CustomDurationParser
import com.seki999.echowordy.tts.TtsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Pre-fill the custom field with the active duration only when it isn't
    // one of the presets, so returning to this screen shows what's active
    // without making it look selected twice.
    var customDurationInput by remember(uiState.cardDurationMs) {
        mutableStateOf(
            if (uiState.cardDurationMs !in uiState.availableDurationsMs) {
                (uiState.cardDurationMs / 1000).toString()
            } else {
                ""
            },
        )
    }
    var customDurationError by remember { mutableStateOf<String?>(null) }

    fun applyCustomDuration() {
        val seconds = CustomDurationParser.parseSeconds(customDurationInput)
        if (seconds != null) {
            customDurationError = null
            viewModel.setCardDuration(seconds * 1000L)
        } else {
            customDurationError = "Enter a whole number of seconds greater than 0."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(text = "Card Duration", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "How long each card stays on screen before the next one appears.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )

            Column(modifier = Modifier.selectableGroup()) {
                uiState.availableDurationsMs.forEach { durationMs ->
                    val selected = durationMs == uiState.cardDurationMs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { viewModel.setCardDuration(durationMs) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(
                            text = formatDuration(durationMs),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            Text(
                text = "Or enter a custom number of seconds:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customDurationInput,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            customDurationInput = newValue
                            customDurationError = null
                        }
                    },
                    singleLine = true,
                    label = { Text("Seconds") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(onDone = { applyCustomDuration() }),
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = { applyCustomDuration() }) {
                    Text("Set")
                }
            }

            customDurationError?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Text(
                text = "American English Voice",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp),
            )

            when (uiState.ttsState) {
                TtsState.UNAVAILABLE -> {
                    Text(
                        text = "American English voice is unavailable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                TtsState.INITIALIZING -> {
                    Text(
                        text = "Checking available voices…",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                TtsState.READY -> {
                    if (uiState.availableVoices.isEmpty()) {
                        Text(
                            text = "Using the device's default American English voice.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    } else {
                        Column(modifier = Modifier.selectableGroup().padding(top = 4.dp)) {
                            val defaultSelected = uiState.selectedVoiceName == null
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = defaultSelected,
                                        onClick = { viewModel.selectVoice(null) },
                                        role = Role.RadioButton,
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = defaultSelected, onClick = null)
                                Text(
                                    text = "System Default",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                            uiState.availableVoices.forEach { voice ->
                                val selected = voice.name == uiState.selectedVoiceName
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selected,
                                            onClick = { viewModel.selectVoice(voice.name) },
                                            role = Role.RadioButton,
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(selected = selected, onClick = null)
                                    Text(
                                        text = voice.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000.0
    val formatted = if (seconds == seconds.toLong().toDouble()) {
        "${seconds.toLong()}.0"
    } else {
        seconds.toString()
    }
    return "$formatted sec"
}
