package com.phoenix.fitpro.data.repository

import com.phoenix.fitpro.data.local.dao.MealEntryDao
import com.phoenix.fitpro.data.local.entity.FoodItemEntity
import com.phoenix.fitpro.data.local.entity.MealEntryEntity
import com.phoenix.fitpro.data.remote.OpenFoodFactsApi
import com.phoenix.fitpro.domain.model.FoodItem
import com.phoenix.fitpro.domain.model.MealEntry
import com.phoenix.fitpro.domain.model.MealType
import com.phoenix.fitpro.domain.model.StapleFoodDatabase
import com.phoenix.fitpro.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val mealDao: MealEntryDao,
    private val api: OpenFoodFactsApi
) : NutritionRepository {

    override fun getMealsForDate(date: LocalDate): Flow<List<MealEntry>> {
        return mealDao.getMealsForDate(date.toEpochDay()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addMealWithFoods(meal: MealEntry): Long {
        val mealEntity = MealEntryEntity(
            date = meal.date.toEpochDay(),
            mealType = meal.mealType.name
        )
        val mealId = mealDao.insertMeal(mealEntity)
        val foodEntities = meal.foods.map { it.toEntity(mealId) }
        mealDao.insertFoods(foodEntities)
        return mealId
    }

    override suspend fun deleteMeal(meal: MealEntry) {
        mealDao.deleteMeal(MealEntryEntity(id = meal.id, date = meal.date.toEpochDay(), mealType = meal.mealType.name))
    }

    override suspend fun deleteFood(food: FoodItem) {
        mealDao.deleteFood(food.toEntity(food.mealId))
    }

    override suspend fun getTotalCaloriesForDate(date: LocalDate): Int {
        return mealDao.getTotalCaloriesForDate(date.toEpochDay()) ?: 0
    }

    override suspend fun getDailyCalories(from: LocalDate, to: LocalDate): List<Pair<LocalDate, Int>> {
        return mealDao.getDailyCalories(from.toEpochDay(), to.toEpochDay()).map { dc ->
            Pair(LocalDate.ofEpochDay(dc.date), dc.totalCal ?: 0)
        }
    }

    override suspend fun searchFoodOnline(query: String): List<FoodItem> {
        val results = mutableListOf<FoodItem>()

        // 1. Instant match from verified staple foods database
        val localMatches = StapleFoodDatabase.search(query)
        results.addAll(localMatches)

        // 2. Open Food Facts API for branded / additional products
        try {
            val isEn = java.util.Locale.getDefault().language.equals("en", ignoreCase = true)
            val lang = if (isEn) "en" else "fr"
            val country = if (isEn) "us" else "fr"
            val response = api.searchProducts(query = query, lang = lang, country = country)
            val remoteMatches = response.products
                .filter { it.displayName.isNotBlank() && it.displayName != "Aliment inconnu" && it.displayName != "Unknown Food" }
                .map { product ->
                    FoodItem(
                        name = product.displayName,
                        quantity = product.quantity ?: "100g",
                        calories = product.caloriesPer100g,
                        proteinG = product.proteinPer100g,
                        carbsG = product.carbsPer100g,
                        fatG = product.fatPer100g,
                        openFoodFactsId = product.id
                    )
                }

            // Deduplicate with existing local results
            remoteMatches.forEach { remote ->
                if (results.none { it.name.equals(remote.name, ignoreCase = true) }) {
                    results.add(remote)
                }
            }
        } catch (e: Exception) {
            // If offline, local results are still returned!
        }

        return results
    }

    override suspend fun searchRecentFoodNames(query: String): List<String> {
        return mealDao.searchRecentFoodNames(query)
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private suspend fun MealEntryEntity.toDomain(): MealEntry {
        val foods = mealDao.getFoodsForMeal(id).map { it.toDomain() }
        return MealEntry(
            id = id,
            date = LocalDate.ofEpochDay(date),
            mealType = MealType.valueOf(mealType),
            foods = foods
        )
    }

    private fun FoodItemEntity.toDomain() = FoodItem(
        id = id, mealId = mealId, name = name, quantity = quantity,
        calories = calories, proteinG = proteinG, carbsG = carbsG, fatG = fatG,
        openFoodFactsId = openFoodFactsId
    )

    private fun FoodItem.toEntity(mealId: Long) = FoodItemEntity(
        id = if (id == 0L) 0 else id,
        mealId = mealId, name = name, quantity = quantity,
        calories = calories, proteinG = proteinG, carbsG = carbsG, fatG = fatG,
        openFoodFactsId = openFoodFactsId
    )
}
