package com.tapme.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tapme.app.data.local.dao.TapDao
import com.tapme.app.data.local.entity.TapEntity
import com.tapme.app.data.local.migrations.Migration1To2

@Database(
    entities = [TapEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TapDatabase : RoomDatabase() {
    abstract fun tapDao(): TapDao
    
    companion object {
        val MIGRATIONS = arrayOf(Migration1To2)
    }
}
