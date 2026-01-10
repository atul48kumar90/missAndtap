package com.tapme.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add tapType column to taps table
        database.execSQL("ALTER TABLE taps ADD COLUMN tapType TEXT")
    }
}
