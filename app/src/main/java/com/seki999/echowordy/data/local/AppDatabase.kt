package com.seki999.echowordy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VocabularyListEntity::class, VocabularyCardEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vocabularyListDao(): VocabularyListDao
    abstract fun vocabularyCardDao(): VocabularyCardDao

    companion object {
        private const val DATABASE_NAME = "echowordy.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
