package com.reelcounter.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReelRecord::class], version = 1, exportSchema = false)
abstract class ReelDatabase : RoomDatabase() {
    abstract fun reelDao(): ReelDao
}
