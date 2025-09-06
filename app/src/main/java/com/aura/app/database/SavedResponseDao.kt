package com.aura.app.database

import androidx.room.*
import com.aura.app.models.SavedResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedResponseDao {
    @Query("SELECT * FROM saved_responses ORDER BY timestamp DESC")
    fun getAllResponses(): Flow<List<SavedResponse>>
    
    @Insert
    suspend fun insertResponse(response: SavedResponse): Long
    
    @Delete
    suspend fun deleteResponse(response: SavedResponse)
    
    @Query("DELETE FROM saved_responses WHERE id = :id")
    suspend fun deleteResponseById(id: Long)
    
    @Query("SELECT COUNT(*) FROM saved_responses")
    suspend fun getResponseCount(): Int
}