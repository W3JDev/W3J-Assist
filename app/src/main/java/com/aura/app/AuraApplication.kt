package com.aura.app

import android.app.Application
import androidx.room.Room
import com.aura.app.database.AuraDatabase

class AuraApplication : Application() {
    
    companion object {
        lateinit var instance: AuraApplication
            private set
    }
    
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AuraDatabase::class.java,
            "aura_database"
        ).build()
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}