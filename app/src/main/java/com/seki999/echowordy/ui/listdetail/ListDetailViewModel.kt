package com.seki999.echowordy.ui.listdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seki999.echowordy.domain.model.VocabularyCard
import com.seki999.echowordy.domain.model.VocabularyList
import com.seki999.echowordy.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ListDetailUiState(
    val list: VocabularyList? = null,
    val cards: List<VocabularyCard> = emptyList(),
    val isLoading: Boolean = true,
)

class ListDetailViewModel(
    private val repository: VocabularyRepository,
    private val listId: Long,
) : ViewModel() {

    val uiState: StateFlow<ListDetailUiState> = combine(
        repository.observeList(listId),
        repository.observeCards(listId),
    ) { list, cards ->
        ListDetailUiState(list = list, cards = cards, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListDetailUiState())

    fun renameList(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.renameList(listId, trimmed) }
    }

    fun deleteList(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteList(listId)
            onDeleted()
        }
    }
}
