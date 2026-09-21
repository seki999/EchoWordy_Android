package com.seki999.echowordy.fakes

import com.seki999.echowordy.tts.TtsController
import com.seki999.echowordy.tts.TtsState
import com.seki999.echowordy.tts.VoiceOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeTtsController(initialState: TtsState = TtsState.READY) : TtsController {

    val spokenWords = mutableListOf<String>()
    var stopCallCount = 0
        private set

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TtsState> = _state

    override fun speak(text: String) {
        spokenWords.add(text)
    }

    override fun stop() {
        stopCallCount++
    }

    override fun shutdown() {
        _state.value = TtsState.UNAVAILABLE
    }

    override fun availableAmericanVoices(): List<VoiceOption> = emptyList()

    override fun selectVoice(voiceName: String?) = Unit

    fun setState(newState: TtsState) {
        _state.value = newState
    }
}
