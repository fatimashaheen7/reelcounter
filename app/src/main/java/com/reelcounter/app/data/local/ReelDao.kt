package com.reelcounter.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {

    @Query("SELECT * FROM daily_reels WHERE date = :date LIMIT 1")
    fun getRecordByDate(date: String): Flow<ReelRecord?>

    @Query("SELECT * FROM daily_reels WHERE date = :date LIMIT 1")
    suspend fun getRecordByDateDirect(date: String): ReelRecord?

    @Query("SELECT * FROM daily_reels ORDER BY date DESC")
    fun getAllRecords(): Flow<List<ReelRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: ReelRecord)

    @Transaction
    suspend fun incrementDailyCount(date: String): Int {
        val current = getRecordByDateDirect(date)
        val newCount = (current?.count ?: 0) + 1
        val updated = ReelRecord(
            date = date,
            count = newCount,
            lastUpdated = System.currentTimeMillis()
        )
        insertOrUpdate(updated)
        return newCount
    }

    @Query("DELETE FROM daily_reels")
    suspend fun clearAll()
}