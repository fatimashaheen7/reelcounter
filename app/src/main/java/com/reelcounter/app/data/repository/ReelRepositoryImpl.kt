package com.reelcounter.app.data.repository

import com.reelcounter.app.data.local.ReelDao
import com.reelcounter.app.data.local.ReelRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReelRepositoryImpl @Inject constructor(
    private val reelDao: ReelDao
) : ReelRepository {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getTodayDateString(): String {
        return LocalDate.now().format(dateFormatter)
    }

    override fun getTodayCountFlow(): Flow<Int> {
        val today = getTodayDateString()
        return reelDao.getRecordByDate(today).map { record ->
            record?.count ?: 0
        }
    }

    override fun getTodayRecordFlow(): Flow<ReelRecord?> {
        val today = getTodayDateString()
        return reelDao.getRecordByDate(today)
    }

    override fun getAllHistoryFlow(): Flow<List<ReelRecord>> {
        return reelDao.getAllRecords()
    }

    override suspend fun incrementReelCount(): Int {
        val today = getTodayDateString()
        return reelDao.incrementDailyCount(today)
    }

    override suspend fun getTodayCount(): Int {
        val today = getTodayDateString()
        return reelDao.getRecordByDateDirect(today)?.count ?: 0
    }
}