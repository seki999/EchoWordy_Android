package com.seki999.echowordy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seki999.echowordy.domain.repository.SettingsRepository
import com.seki999.echowordy.tts.TtsController
import com.seki999.echowordy.tts.TtsState
import com.seki999.echowordy.tts.VoiceOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val cardDurationMs: Long = SettingsRepository.DEFAULT_CARD_DURATION_MS,
    val availableDurationsMs: List<Long> = SettingsRepository.AVAILABLE_DURATIONS_MS,
    val availableVoices: List<VoiceOption> = emptyList(),
    val selectedVoiceName: String? = null,
    val ttsState: TtsState = TtsState.INITIALIZING,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val ttsController: TtsController,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.cardDurationMsFlow(),
        settingsRepository.selectedVoiceNameFlow(),
        ttsController.state,
    ) { durationMs, voiceName, ttsState ->
        SettingsUiState(
            cardDurationMs = durationMs,
            availableVoices = if (ttsState == TtsState.READY) ttsController.availableAmericanVoices() else emptyList(),
            selectedVoiceName = voiceName,
            ttsState = ttsState,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setCardDuration(durationMs: Long) {
        viewModelScope.launch { settingsRepository.setCardDurationMs(durationMs) }
    }

    fun selectVoice(voiceName: String?) {
        viewModelScope.launch {
            settingsRepository.setSelectedVoiceName(voiceName)
            ttsController.selectVoice(voiceName)
        }
    }
}
