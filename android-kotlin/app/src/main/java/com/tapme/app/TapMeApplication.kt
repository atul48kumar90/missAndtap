package com.tapme.app

import android.app.Application
import androidx.room.Room
import com.tapme.app.data.local.TapDatabase
import com.tapme.app.utils.AppStateLifecycleCallback

class TapMeApplication : Application() {
    val database: TapDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            TapDatabase::class.java,
            "tapme_database"
        )
            .addMigrations(*TapDatabase.MIGRATIONS)
            .fallbackToDestructiveMigration() // Only for development
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        // Register activity lifecycle callback to track app state
        registerActivityLifecycleCallbacks(AppStateLifecycleCallback())
    }
}
