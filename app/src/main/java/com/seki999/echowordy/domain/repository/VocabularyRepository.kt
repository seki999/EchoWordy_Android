package com.seki999.echowordy.domain.repository

import com.seki999.echowordy.domain.model.VocabularyCard
import com.seki999.echowordy.domain.model.VocabularyList
import com.seki999.echowordy.domain.model.VocabularyListSummary
import com.seki999.echowordy.domain.usecase.ParsedCard
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {

    fun observeListSummaries(): Flow<List<VocabularyListSummary>>

    fun observeList(listId: Long): Flow<VocabularyList?>

    fun observeCards(listId: Long): Flow<List<VocabularyCard>>

    suspend fun getListOnce(listId: Long): VocabularyList?

    suspend fun getCardsOnce(listId: Long): List<VocabularyCard>

    suspend fun getAllListNames(): List<String>

    /** Creates an empty list and returns its new id. */
    suspend fun createList(name: String): Long

    /** Creates a list pre-populated with [cards] and returns its new id. */
    suspend fun createListWithCards(name: String, cards: List<ParsedCard>): Long

    suspend fun renameList(listId: Long, newName: String)

    /** Deletes the list and all of its cards. */
    suspend fun deleteList(listId: Long)

    /** Replaces every card in the list with [cards], preserving their order. */
    suspend fun replaceCards(listId: Long, cards: List<ParsedCard>)

    /** Persists a new manual display order for lists, as given by [orderedListIds]. */
    suspend fun reorderLists(orderedListIds: List<Long>)
}
