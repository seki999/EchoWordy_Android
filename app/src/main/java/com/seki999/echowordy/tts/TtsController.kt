package com.seki999.echowordy.tts

import kotlinx.coroutines.flow.StateFlow

enum class TtsState {
    INITIALIZING,
    READY,
    UNAVAILABLE,
}

data class VoiceOption(val name: String, val label: String)

/**
 * Abstraction over Android's [android.speech.tts.TextToSpeech] so review logic
 * can be unit tested without a real speech engine.
 */
interface TtsController {

    val state: StateFlow<TtsState>

    /** Speaks [text], flushing any speech currently queued or in progress. */
    fun speak(text: String)

    fun stop()

    fun shutdown()

    /** American English voices installed on the device, if the engine is ready. */
    fun availableAmericanVoices(): List<VoiceOption>

    /** Pass null to fall back to the engine's default American English voice. */
    fun selectVoice(voiceName: String?)
}
