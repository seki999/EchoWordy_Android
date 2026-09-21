package com.seki999.echowordy.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seki999.echowordy.domain.model.VocabularyCard
import com.seki999.echowordy.domain.repository.SettingsRepository
import com.seki999.echowordy.domain.repository.VocabularyRepository
import com.seki999.echowordy.domain.usecase.ParsedCard
import com.seki999.echowordy.domain.usecase.UnknownListNameGenerator
import com.seki999.echowordy.tts.TtsController
import com.seki999.echowordy.tts.TtsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val listId: Long = -1L,
    val listName: String = "",
    val cards: List<VocabularyCard> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val wasStopped: Boolean = false,
    val markedUnknownCardIds: Set<Long> = emptySet(),
    val reviewedCardCount: Int = 0,
    val cardDurationMs: Long = SettingsRepository.DEFAULT_CARD_DURATION_MS,
    val isLoading: Boolean = true,
    val ttsUnavailable: Boolean = false,
    val generatedUnknownListId: Long? = null,
    val generatedUnknownListName: String? = null,
    val showStopConfirmation: Boolean = false,
) {
    val currentCard: VocabularyCard? get() = cards.getOrNull(currentIndex)
    val isCurrentMarkedUnknown: Boolean get() = currentCard?.id in markedUnknownCardIds
    val progressText: String get() = if (cards.isEmpty()) "" else "${currentIndex + 1} / ${cards.size}"
    val unknownCount: Int get() = markedUnknownCardIds.size
    val isAwaitingUnknownListCreation: Boolean
        get() = isCompleted && unknownCount > 0 && generatedUnknownListName == null
}

/**
 * Drives a single review session. There is exactly one timer job alive at any
 * moment: every action that changes what is displayed (advancing, going back,
 * pausing, stopping, or leaving the screen) cancels it before doing anything
 * else, so auto-advance can never race with a manual navigation.
 */
class ReviewViewModel(
    private val repository: VocabularyRepository,
    private val settingsRepository: SettingsRepository,
    private val ttsController: TtsController,
    listId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState(listId = listId))
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var hasStarted = false

    init {
        viewModelScope.launch {
            val list = repository.getListOnce(listId)
            val cards = repository.getCardsOnce(listId)
            val durationMs = settingsRepository.cardDurationMsFlow().first()
            _uiState.update {
                it.copy(
                    listName = list?.name.orEmpty(),
                    cards = cards,
                    cardDurationMs = durationMs,
                    isLoading = false,
                )
            }
        }
        viewModelScope.launch {
            ttsController.state.collect { state ->
                _uiState.update { it.copy(ttsUnavailable = state == TtsState.UNAVAILABLE) }
            }
        }
    }

    fun startReview() {
        if (hasStarted) return
        if (_uiState.value.cards.isEmpty()) return
        hasStarted = true
        _uiState.update { it.copy(isPlaying = true, isPaused = false, currentIndex = 0) }
        showCurrentCard()
    }

    fun pause() {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isCompleted) return
        timerJob?.cancel()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resume() {
        val state = _uiState.value
        if (!state.isPlaying || !state.isPaused || state.isCompleted) return
        _uiState.update { it.copy(isPaused = false) }
        startTimer()
    }

    fun previous() {
        val state = _uiState.value
        if (state.isCompleted || state.currentIndex <= 0) return
        _uiState.update { it.copy(currentIndex = it.currentIndex - 1) }
        showCurrentCard()
    }

    fun next() {
        if (_uiState.value.isCompleted) return
        advanceOrFinish()
    }

    fun markCurrentAsUnknown() {
        val card = _uiState.value.currentCard ?: return
        _uiState.update { it.copy(markedUnknownCardIds = it.markedUnknownCardIds + card.id) }
    }

    fun removeCurrentFromUnknown() {
        val card = _uiState.value.currentCard ?: return
        _uiState.update { it.copy(markedUnknownCardIds = it.markedUnknownCardIds - card.id) }
    }

    fun requestStop() {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(showStopConfirmation = true) }
    }

    fun cancelStop() {
        _uiState.update { it.copy(showStopConfirmation = false) }
    }

    fun confirmStop() {
        _uiState.update { it.copy(showStopConfirmation = false) }
        finishReview(stopped = true)
    }

    private fun showCurrentCard() {
        timerJob?.cancel()
        val state = _uiState.value
        val card = state.currentCard ?: return
        ttsController.speak(card.word)
        if (state.isPlaying && !state.isPaused) {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        val durationMs = _uiState.value.cardDurationMs
        timerJob = viewModelScope.launch {
            delay(durationMs)
            advanceOrFinish()
        }
    }

    private fun advanceOrFinish() {
        timerJob?.cancel()
        val state = _uiState.value
        if (state.cards.isEmpty()) return
        if (state.currentIndex >= state.cards.lastIndex) {
            finishReview(stopped = false)
        } else {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
            showCurrentCard()
        }
    }

    private fun finishReview(stopped: Boolean) {
        timerJob?.cancel()
        ttsController.stop()
        val state = _uiState.value
        val reviewedCount = state.currentIndex + 1
        _uiState.update {
            it.copy(
                isPlaying = false,
                isPaused = false,
                isCompleted = true,
                wasStopped = stopped,
                reviewedCardCount = reviewedCount,
            )
        }
        if (state.markedUnknownCardIds.isNotEmpty()) {
            generateUnknownWordsList(state)
        }
    }

    private fun generateUnknownWordsList(state: ReviewUiState) {
        viewModelScope.launch {
            val unknownCards = state.cards.filter { it.id in state.markedUnknownCardIds }
            val existingNames = repository.getAllListNames()
            val newName = UnknownListNameGenerator.generateName(state.listName, existingNames)
            val newListId = repository.createListWithCards(
                name = newName,
                cards = unknownCards.map { ParsedCard(word = it.word, body = it.body) },
            )
            _uiState.update {
                it.copy(generatedUnknownListId = newListId, generatedUnknownListName = newName)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        ttsController.stop()
    }
}
