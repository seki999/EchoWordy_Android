package com.seki999.echowordy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyCardDao {

    @Query("SELECT * FROM vocabulary_cards WHERE listId = :listId ORDER BY sortOrder ASC")
    fun observeForList(listId: Long): Flow<List<VocabularyCardEntity>>

    @Query("SELECT * FROM vocabulary_cards WHERE listId = :listId ORDER BY sortOrder ASC")
    suspend fun getForListOnce(listId: Long): List<VocabularyCardEntity>

    @Insert
    suspend fun insertAll(cards: List<VocabularyCardEntity>)

    @Query("DELETE FROM vocabulary_cards WHERE listId = :listId")
    suspend fun deleteForList(listId: Long)
}
