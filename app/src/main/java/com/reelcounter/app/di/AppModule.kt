package com.reelcounter.app.di

import android.content.Context
import androidx.room.Room
import com.reelcounter.app.data.local.ReelDao
import com.reelcounter.app.data.local.ReelDatabase
import com.reelcounter.app.data.repository.ReelRepository
import com.reelcounter.app.data.repository.ReelRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ReelDatabase =
        Room.databaseBuilder(context, ReelDatabase::class.java, "reelcounter.db").build()

    @Provides
    @Singleton
    fun provideReelDao(db: ReelDatabase): ReelDao = db.reelDao()

    @Provides
    @Singleton
    fun provideRepository(impl: ReelRepositoryImpl): ReelRepository = impl
}
