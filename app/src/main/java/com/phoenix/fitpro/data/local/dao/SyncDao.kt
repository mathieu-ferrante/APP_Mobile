package com.phoenix.fitpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phoenix.fitpro.data.local.entity.AchievementEntity
import com.phoenix.fitpro.data.local.entity.ExerciseSetEntity
import com.phoenix.fitpro.data.local.entity.FoodItemEntity
import com.phoenix.fitpro.data.local.entity.MealEntryEntity
import com.phoenix.fitpro.data.local.entity.SportEntity
import com.phoenix.fitpro.data.local.entity.UserProfileEntity
import com.phoenix.fitpro.data.local.entity.WeightEntryEntity
import com.phoenix.fitpro.data.local.entity.WorkoutSessionEntity

/**
 * Accès brut à toutes les tables, utilisé uniquement par la synchronisation cloud :
 * export complet (push) et remplacement complet (pull).
 */
@Dao
interface SyncDao {

    // ── Export ────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM user_profile")
    suspend fun allProfiles(): List<UserProfileEntity>

    @Query("SELECT * FROM weight_entries")
    suspend fun allWeightEntries(): List<WeightEntryEntity>

    @Query("SELECT * FROM sports")
    suspend fun allSports(): List<SportEntity>

    @Query("SELECT * FROM workout_sessions")
    suspend fun allSessions(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM exercise_sets")
    suspend fun allExerciseSets(): List<ExerciseSetEntity>

    @Query("SELECT * FROM meal_entries")
    suspend fun allMeals(): List<MealEntryEntity>

    @Query("SELECT * FROM food_items")
    suspend fun allFoodItems(): List<FoodItemEntity>

    @Query("SELECT * FROM achievements")
    suspend fun allAchievements(): List<AchievementEntity>

    // ── Import ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(items: List<UserProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntries(items: List<WeightEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSports(items: List<SportEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(items: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSets(items: List<ExerciseSetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(items: List<MealEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(items: List<AchievementEntity>)

    // ── Nettoyage (ordre inverse des clés étrangères) ─────────────────────────

    @Query("DELETE FROM food_items")
    suspend fun clearFoodItems()

    @Query("DELETE FROM meal_entries")
    suspend fun clearMeals()

    @Query("DELETE FROM exercise_sets")
    suspend fun clearExerciseSets()

    @Query("DELETE FROM workout_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM sports")
    suspend fun clearSports()

    @Query("DELETE FROM weight_entries")
    suspend fun clearWeightEntries()

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()

    @Query("DELETE FROM user_profile")
    suspend fun clearProfiles()
}
