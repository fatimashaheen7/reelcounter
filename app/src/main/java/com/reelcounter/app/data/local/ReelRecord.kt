package com.reelcounter.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reels")
data class ReelRecord(
    @PrimaryKey
    val date: String,
    val count: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)