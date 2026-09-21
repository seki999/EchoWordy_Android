package com.seki999.echowordy.fakes

import com.seki999.echowordy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initialDurationMs: Long = SettingsRepository.DEFAULT_CARD_DURATION_MS,
) : SettingsRepository {

    private val durationFlow = MutableStateFlow(initialDurationMs)
    private val voiceFlow = MutableStateFlow<String?>(null)

    override fun cardDurationMsFlow(): Flow<Long> = durationFlow

    override suspend fun setCardDurationMs(durationMs: Long) {
        durationFlow.value = durationMs
    }

    override fun selectedVoiceNameFlow(): Flow<String?> = voiceFlow

    override suspend fun setSelectedVoiceName(voiceName: String?) {
        voiceFlow.value = voiceName
    }
}
