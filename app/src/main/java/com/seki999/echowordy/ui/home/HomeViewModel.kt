package com.seki999.echowordy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seki999.echowordy.domain.model.VocabularyListSummary
import com.seki999.echowordy.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeEvent {
    data class ListCreated(val listId: Long) : HomeEvent
}

class HomeViewModel(private val repository: VocabularyRepository) : ViewModel() {

    val listSummaries: StateFlow<List<VocabularyListSummary>> =
        repository.observeListSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    fun createList(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val id = repository.createList(trimmed)
            _events.emit(HomeEvent.ListCreated(id))
        }
    }
}
