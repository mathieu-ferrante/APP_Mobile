package com.phoenix.fitpro.di

import android.content.Context
import com.phoenix.fitpro.data.local.PhoenixDatabase
import com.phoenix.fitpro.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhoenixDatabase {
        return PhoenixDatabase.getInstance(context)
    }

    @Provides fun provideSportDao(db: PhoenixDatabase): SportDao = db.sportDao()
    @Provides fun provideWorkoutSessionDao(db: PhoenixDatabase): WorkoutSessionDao = db.workoutSessionDao()
    @Provides fun provideMealEntryDao(db: PhoenixDatabase): MealEntryDao = db.mealEntryDao()
    @Provides fun provideUserProfileDao(db: PhoenixDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideAchievementDao(db: PhoenixDatabase): AchievementDao = db.achievementDao()
}
