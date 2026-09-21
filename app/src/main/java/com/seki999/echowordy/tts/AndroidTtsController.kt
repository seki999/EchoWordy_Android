package com.seki999.echowordy.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "AndroidTtsController"
private const val UTTERANCE_ID = "echowordy_word"

/**
 * Wraps a single, long-lived [TextToSpeech] instance for the whole app.
 * Initialization is asynchronous; callers observe [state] instead of
 * assuming the engine is ready immediately after construction.
 */
class AndroidTtsController(context: Context) : TtsController {

    private val _state = MutableStateFlow(TtsState.INITIALIZING)
    override val state: StateFlow<TtsState> = _state.asStateFlow()

    private var pendingVoiceName: String? = null
    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            onInitialized(status)
        }
    }

    private fun onInitialized(status: Int) {
        val engine = textToSpeech
        if (status != TextToSpeech.SUCCESS || engine == null) {
            Log.w(TAG, "TextToSpeech initialization failed with status $status")
            _state.value = TtsState.UNAVAILABLE
            return
        }

        val result = engine.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "American English (Locale.US) is not supported by this device's TTS engine")
            _state.value = TtsState.UNAVAILABLE
            return
        }

        pendingVoiceName?.let { applyVoice(it) }
        _state.value = TtsState.READY
    }

    override fun speak(text: String) {
        if (_state.value != TtsState.READY || text.isBlank()) return
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    override fun stop() {
        textToSpeech?.stop()
    }

    override fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _state.value = TtsState.UNAVAILABLE
    }

    override fun availableAmericanVoices(): List<VoiceOption> {
        val engine = textToSpeech ?: return emptyList()
        if (_state.value != TtsState.READY) return emptyList()
        return try {
            engine.voices.orEmpty()
                .filter { it.locale == Locale.US && !it.isNetworkConnectionRequired }
                .map { VoiceOption(name = it.name, label = it.name) }
                .sortedBy { it.label }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query available voices", e)
            emptyList()
        }
    }

    override fun selectVoice(voiceName: String?) {
        if (voiceName == null) {
            val engine = textToSpeech ?: return
            engine.voice = engine.defaultVoice ?: return
            return
        }
        applyVoice(voiceName)
    }

    private fun applyVoice(voiceName: String) {
        val engine = textToSpeech
        if (engine == null || _state.value != TtsState.READY) {
            pendingVoiceName = voiceName
            return
        }
        val voice = engine.voices?.firstOrNull { it.name == voiceName }
        if (voice != null) {
            engine.voice = voice
        }
        pendingVoiceName = null
    }
}
