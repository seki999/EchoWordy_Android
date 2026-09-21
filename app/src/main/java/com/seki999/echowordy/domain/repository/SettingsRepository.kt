package com.seki999.echowordy.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun cardDurationMsFlow(): Flow<Long>

    suspend fun setCardDurationMs(durationMs: Long)

    /** Null means "use the system default American English voice". */
    fun selectedVoiceNameFlow(): Flow<String?>

    suspend fun setSelectedVoiceName(voiceName: String?)

    companion object {
        const val DEFAULT_CARD_DURATION_MS = 2000L
        val AVAILABLE_DURATIONS_MS = listOf(1000L, 1500L, 2000L, 2500L, 3000L, 4000L)
    }
}
