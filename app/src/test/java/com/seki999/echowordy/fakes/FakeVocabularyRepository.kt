package com.seki999.echowordy.fakes

import com.seki999.echowordy.domain.model.VocabularyCard
import com.seki999.echowordy.domain.model.VocabularyList
import com.seki999.echowordy.domain.model.VocabularyListSummary
import com.seki999.echowordy.domain.repository.VocabularyRepository
import com.seki999.echowordy.domain.usecase.ParsedCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeVocabularyRepository : VocabularyRepository {

    private var nextListId = 1L
    private var nextCardId = 1L

    private val lists = LinkedHashMap<Long, VocabularyList>()
    private val cards = LinkedHashMap<Long, MutableList<VocabularyCard>>()

    private val listsFlow = MutableStateFlow<List<VocabularyList>>(emptyList())

    /** Test helper: seeds a list with cards, preserving the given order. */
    fun seedList(name: String, cardWordsAndBodies: List<Pair<String, String>>): Long {
        val id = nextListId++
        val now = System.currentTimeMillis()
        lists[id] = VocabularyList(id = id, name = name, createdAt = now, updatedAt = now)
        cards[id] = cardWordsAndBodies.mapIndexed { index, (word, body) ->
            VocabularyCard(id = nextCardId++, listId = id, word = word, body = body, sortOrder = index)
        }.toMutableList()
        listsFlow.value = lists.values.toList()
        return id
    }

    override fun observeListSummaries(): Flow<List<VocabularyListSummary>> =
        listsFlow.map { all -> all.map { VocabularyListSummary(it.id, it.name, cards[it.id]?.size ?: 0) } }

    override fun observeList(listId: Long): Flow<VocabularyList?> =
        listsFlow.map { all -> all.firstOrNull { it.id == listId } }

    override fun observeCards(listId: Long): Flow<List<VocabularyCard>> =
        listsFlow.map { cards[listId].orEmpty() }

    override suspend fun getListOnce(listId: Long): VocabularyList? = lists[listId]

    override suspend fun getCardsOnce(listId: Long): List<VocabularyCard> = cards[listId].orEmpty()

    override suspend fun getAllListNames(): List<String> = lists.values.map { it.name }

    override suspend fun createList(name: String): Long {
        val id = nextListId++
        val now = System.currentTimeMillis()
        lists[id] = VocabularyList(id = id, name = name, createdAt = now, updatedAt = now)
        cards[id] = mutableListOf()
        listsFlow.value = lists.values.toList()
        return id
    }

    override suspend fun createListWithCards(name: String, cards: List<ParsedCard>): Long {
        val id = createList(name)
        val now = System.currentTimeMillis()
        this.cards[id] = cards.mapIndexed { index, card ->
            VocabularyCard(id = nextCardId++, listId = id, word = card.word, body = card.body, sortOrder = index)
        }.toMutableList()
        listsFlow.value = lists.values.toList()
        return id
    }

    override suspend fun renameList(listId: Long, newName: String) {
        val existing = lists[listId] ?: return
        lists[listId] = existing.copy(name = newName, updatedAt = System.currentTimeMillis())
        listsFlow.value = lists.values.toList()
    }

    override suspend fun deleteList(listId: Long) {
        lists.remove(listId)
        cards.remove(listId)
        listsFlow.value = lists.values.toList()
    }

    override suspend fun replaceCards(listId: Long, cards: List<ParsedCard>) {
        val now = System.currentTimeMillis()
        this.cards[listId] = cards.mapIndexed { index, card ->
            VocabularyCard(id = nextCardId++, listId = listId, word = card.word, body = card.body, sortOrder = index)
        }.toMutableList()
    }

    override suspend fun reorderLists(orderedListIds: List<Long>) {
        val reordered = LinkedHashMap<Long, VocabularyList>()
        orderedListIds.forEach { id -> lists[id]?.let { reordered[id] = it } }
        lists.values.forEach { if (it.id !in reordered) reordered[it.id] = it }
        lists.clear()
        lists.putAll(reordered)
        listsFlow.value = lists.values.toList()
    }
}
