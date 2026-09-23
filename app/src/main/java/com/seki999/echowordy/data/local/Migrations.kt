package com.seki999.echowordy.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds manual drag-to-reorder support for lists. Existing lists keep their
 * current (createdAt) order by being backfilled with sequential sortOrder
 * values, so nobody's list order visibly changes right after the upgrade.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vocabulary_lists ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")

        val cursor = db.query("SELECT id FROM vocabulary_lists ORDER BY createdAt ASC")
        cursor.use {
            var order = 0
            val idColumn = it.getColumnIndexOrThrow("id")
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                db.execSQL("UPDATE vocabulary_lists SET sortOrder = ? WHERE id = ?", arrayOf(order, id))
                order++
            }
        }
    }
}
