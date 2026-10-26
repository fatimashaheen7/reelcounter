package com.reelcounter.app.data.repository

import com.reelcounter.app.data.local.ReelRecord
import kotlinx.coroutines.flow.Flow

interface ReelRepository {
    fun getTodayDateString(): String
    fun getTodayCountFlow(): Flow<Int>
    fun getTodayRecordFlow(): Flow<ReelRecord?>
    fun getAllHistoryFlow(): Flow<List<ReelRecord>>
    suspend fun incrementReelCount(): Int
    suspend fun getTodayCount(): Int
}
