package com.aura.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_responses")
data class SavedResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val inputText: String,
    val aiResponse: String,
    val inputType: String // "audio" or "ocr"
)