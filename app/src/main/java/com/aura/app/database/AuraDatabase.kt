package com.aura.app.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aura.app.models.SavedResponse

@Database(
    entities = [SavedResponse::class],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun savedResponseDao(): SavedResponseDao
}