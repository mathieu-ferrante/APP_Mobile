package com.phoenix.fitpro.data.local.dao

import androidx.room.*
import com.phoenix.fitpro.data.local.entity.FoodItemEntity
import com.phoenix.fitpro.data.local.entity.MealEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {

    @Query("SELECT * FROM meal_entries WHERE date = :epochDay ORDER BY mealType ASC")
    fun getMealsForDate(epochDay: Long): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :fromEpoch AND :toEpoch ORDER BY date DESC")
    fun getMealsInRange(fromEpoch: Long, toEpoch: Long): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries ORDER BY date DESC LIMIT 30")
    fun getRecentMeals(): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM food_items WHERE mealId = :mealId ORDER BY id ASC")
    suspend fun getFoodsForMeal(mealId: Long): List<FoodItemEntity>

    @Query("SELECT * FROM food_items WHERE mealId = :mealId ORDER BY id ASC")
    fun observeFoodsForMeal(mealId: Long): Flow<List<FoodItemEntity>>

    @Query("SELECT SUM(calories) FROM food_items fi INNER JOIN meal_entries me ON fi.mealId = me.id WHERE me.date = :epochDay")
    suspend fun getTotalCaloriesForDate(epochDay: Long): Int?

    @Query("SELECT SUM(calories) FROM food_items fi INNER JOIN meal_entries me ON fi.mealId = me.id WHERE me.date BETWEEN :fromEpoch AND :toEpoch")
    suspend fun getTotalCaloriesInRange(fromEpoch: Long, toEpoch: Long): Int?

    @Query("SELECT date, SUM(fi.calories) as totalCal FROM food_items fi INNER JOIN meal_entries me ON fi.mealId = me.id WHERE me.date BETWEEN :fromEpoch AND :toEpoch GROUP BY me.date ORDER BY me.date ASC")
    suspend fun getDailyCalories(fromEpoch: Long, toEpoch: Long): List<DailyCalories>

    /** Search in previously entered food names for autocomplete */
    @Query("SELECT DISTINCT name FROM food_items WHERE name LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchRecentFoodNames(query: String): List<String>

    @Insert
    suspend fun insertMeal(meal: MealEntryEntity): Long

    @Insert
    suspend fun insertFood(food: FoodItemEntity): Long

    @Insert
    suspend fun insertFoods(foods: List<FoodItemEntity>)

    @Update
    suspend fun updateMeal(meal: MealEntryEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntryEntity)

    @Delete
    suspend fun deleteFood(food: FoodItemEntity)

    @Query("DELETE FROM food_items WHERE mealId = :mealId")
    suspend fun deleteFoodsForMeal(mealId: Long)
}

data class DailyCalories(val date: Long, val totalCal: Int?)
