package com.seki999.echowordy.data.repository

import androidx.room.withTransaction
import com.seki999.echowordy.data.local.AppDatabase
import com.seki999.echowordy.data.local.VocabularyCardDao
import com.seki999.echowordy.data.local.VocabularyCardEntity
import com.seki999.echowordy.data.local.VocabularyListDao
import com.seki999.echowordy.data.local.VocabularyListEntity
import com.seki999.echowordy.domain.model.VocabularyCard
import com.seki999.echowordy.domain.model.VocabularyList
import com.seki999.echowordy.domain.model.VocabularyListSummary
import com.seki999.echowordy.domain.repository.VocabularyRepository
import com.seki999.echowordy.domain.usecase.ParsedCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VocabularyRepositoryImpl(
    private val database: AppDatabase,
    private val listDao: VocabularyListDao,
    private val cardDao: VocabularyCardDao,
) : VocabularyRepository {

    override fun observeListSummaries(): Flow<List<VocabularyListSummary>> =
        listDao.observeListsWithCount().map { rows ->
            rows.map { VocabularyListSummary(id = it.id, name = it.name, cardCount = it.cardCount) }
        }

    override fun observeList(listId: Long): Flow<VocabularyList?> =
        listDao.observeById(listId).map { it?.toDomain() }

    override fun observeCards(listId: Long): Flow<List<VocabularyCard>> =
        cardDao.observeForList(listId).map { cards -> cards.map { it.toDomain() } }

    override suspend fun getListOnce(listId: Long): VocabularyList? =
        listDao.getByIdOnce(listId)?.toDomain()

    override suspend fun getCardsOnce(listId: Long): List<VocabularyCard> =
        cardDao.getForListOnce(listId).map { it.toDomain() }

    override suspend fun getAllListNames(): List<String> = listDao.getAllNames()

    override suspend fun createList(name: String): Long {
        val now = System.currentTimeMillis()
        val nextSortOrder = listDao.getMaxSortOrder() + 1
        return listDao.insert(
            VocabularyListEntity(name = name, createdAt = now, updatedAt = now, sortOrder = nextSortOrder)
        )
    }

    override suspend fun createListWithCards(name: String, cards: List<ParsedCard>): Long =
        database.withTransaction {
            val now = System.currentTimeMillis()
            val nextSortOrder = listDao.getMaxSortOrder() + 1
            val listId = listDao.insert(
                VocabularyListEntity(name = name, createdAt = now, updatedAt = now, sortOrder = nextSortOrder)
            )
            if (cards.isNotEmpty()) {
                cardDao.insertAll(cards.mapIndexed { index, card -> card.toEntity(listId, index, now) })
            }
            listId
        }

    override suspend fun renameList(listId: Long, newName: String) {
        val existing = listDao.getByIdOnce(listId) ?: return
        listDao.update(existing.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteList(listId: Long) {
        val existing = listDao.getByIdOnce(listId) ?: return
        listDao.delete(existing)
    }

    override suspend fun replaceCards(listId: Long, cards: List<ParsedCard>) {
        database.withTransaction {
            cardDao.deleteForList(listId)
            val now = System.currentTimeMillis()
            if (cards.isNotEmpty()) {
                cardDao.insertAll(cards.mapIndexed { index, card -> card.toEntity(listId, index, now) })
            }
            val existing = listDao.getByIdOnce(listId)
            if (existing != null) {
                listDao.update(existing.copy(updatedAt = now))
            }
        }
    }

    override suspend fun reorderLists(orderedListIds: List<Long>) {
        database.withTransaction {
            orderedListIds.forEachIndexed { index, id ->
                listDao.updateSortOrder(id, index)
            }
        }
    }

    private fun VocabularyListEntity.toDomain() =
        VocabularyList(id = id, name = name, createdAt = createdAt, updatedAt = updatedAt)

    private fun VocabularyCardEntity.toDomain() =
        VocabularyCard(id = id, listId = listId, word = word, body = body, sortOrder = sortOrder)

    private fun ParsedCard.toEntity(listId: Long, sortOrder: Int, now: Long) =
        VocabularyCardEntity(
            listId = listId,
            word = word,
            body = body,
            sortOrder = sortOrder,
            createdAt = now,
        )
}
