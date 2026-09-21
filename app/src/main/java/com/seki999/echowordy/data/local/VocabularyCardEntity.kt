package com.seki999.echowordy.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary_cards",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("listId")],
)
data class VocabularyCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val listId: Long,
    val word: String,
    val body: String,
    val sortOrder: Int,
    val createdAt: Long,
)
