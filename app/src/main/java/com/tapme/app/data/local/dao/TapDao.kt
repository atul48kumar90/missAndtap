package com.tapme.app.data.local.dao

import androidx.room.*
import com.tapme.app.data.local.entity.TapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TapDao {
    @Query("SELECT * FROM taps WHERE synced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedTaps(): Flow<List<TapEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTap(tap: TapEntity)
    
    @Update
    suspend fun updateTap(tap: TapEntity)
    
    @Query("DELETE FROM taps WHERE id = :id")
    suspend fun deleteTap(id: String)
    
    @Query("SELECT COUNT(*) FROM taps WHERE pairId = :pairId AND fromUserId = :fromUserId AND DATE(timestamp) = DATE('now')")
    suspend fun getTodayTapCount(pairId: String, fromUserId: String): Int
}
