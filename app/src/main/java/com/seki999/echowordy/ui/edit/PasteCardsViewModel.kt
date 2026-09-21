package com.seki999.echowordy.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seki999.echowordy.domain.repository.VocabularyRepository
import com.seki999.echowordy.domain.usecase.CardParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PasteCardsUiState(
    val listName: String = "",
    val text: String = "",
    val detectedCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class PasteCardsViewModel(
    private val repository: VocabularyRepository,
    private val listId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasteCardsUiState())
    val uiState: StateFlow<PasteCardsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val list = repository.getListOnce(listId)
            val cards = repository.getCardsOnce(listId)
            val text = cards.joinToString("\n\n") { card ->
                if (card.body.isBlank()) card.word else "${card.word}\n${card.body}"
            }
            _uiState.update {
                it.copy(
                    listName = list?.name.orEmpty(),
                    text = text,
                    detectedCount = cards.size,
                    isLoading = false,
                )
            }
        }
    }

    fun onTextChange(newText: String) {
        val parsedCount = CardParser.parse(newText).size
        _uiState.update { it.copy(text = newText, detectedCount = parsedCount, errorMessage = null) }
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val parsed = CardParser.parse(state.text)
        if (state.text.isNotBlank() && parsed.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Some card blocks do not contain a valid first line.")
            }
            return
        }
        viewModelScope.launch {
            repository.replaceCards(listId, parsed)
            onSaved()
        }
    }
}
