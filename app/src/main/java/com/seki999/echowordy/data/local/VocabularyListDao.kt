package com.seki999.echowordy.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyListDao {

    @Query(
        """
        SELECT l.id AS id, l.name AS name, l.createdAt AS createdAt, l.updatedAt AS updatedAt,
               COUNT(c.id) AS cardCount
        FROM vocabulary_lists l
        LEFT JOIN vocabulary_cards c ON c.listId = l.id
        GROUP BY l.id
        ORDER BY l.createdAt ASC
        """
    )
    fun observeListsWithCount(): Flow<List<VocabularyListWithCount>>

    @Query("SELECT * FROM vocabulary_lists WHERE id = :listId")
    fun observeById(listId: Long): Flow<VocabularyListEntity?>

    @Query("SELECT * FROM vocabulary_lists WHERE id = :listId")
    suspend fun getByIdOnce(listId: Long): VocabularyListEntity?

    @Query("SELECT name FROM vocabulary_lists")
    suspend fun getAllNames(): List<String>

    @Insert
    suspend fun insert(list: VocabularyListEntity): Long

    @Update
    suspend fun update(list: VocabularyListEntity)

    @Delete
    suspend fun delete(list: VocabularyListEntity)
}
