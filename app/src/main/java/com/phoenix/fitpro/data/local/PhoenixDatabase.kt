package com.phoenix.fitpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.phoenix.fitpro.data.local.dao.*
import com.phoenix.fitpro.data.local.entity.*
import com.phoenix.fitpro.domain.model.AchievementDefinitions
import com.phoenix.fitpro.domain.model.DefaultSports
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [
        UserProfileEntity::class,
        WeightEntryEntity::class,
        SportEntity::class,
        WorkoutSessionEntity::class,
        ExerciseSetEntity::class,
        MealEntryEntity::class,
        FoodItemEntity::class,
        AchievementEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class PhoenixDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun sportDao(): SportDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        private const val DATABASE_NAME = "phoenix.db"

        @Volatile
        private var INSTANCE: PhoenixDatabase? = null

        fun getInstance(context: Context): PhoenixDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PhoenixDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /** Seeds default sports and achievements on first run */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // Seed default sports
                        val sportEntities = DefaultSports.list.map { sport ->
                            SportEntity(
                                id = sport.id,
                                name = sport.name,
                                category = sport.category.name,
                                iconName = sport.iconName,
                                colorArgb = sport.colorArgb,
                                isDefault = sport.isDefault
                            )
                        }
                        database.sportDao().insertSports(sportEntities)

                        // Seed achievement records (all locked by default)
                        val achievementEntities = AchievementDefinitions.all.map { a ->
                            AchievementEntity(id = a.id, isUnlocked = false)
                        }
                        database.achievementDao().insertAll(achievementEntities)

                        // Create a default empty user profile
                        database.userProfileDao().upsertProfile(UserProfileEntity())
                    }
                }
            }
        }
    }
}
