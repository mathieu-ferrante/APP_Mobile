package com.phoenix.fitpro.data.repository

import com.phoenix.fitpro.data.local.dao.MealEntryDao
import com.phoenix.fitpro.data.local.entity.FoodItemEntity
import com.phoenix.fitpro.data.local.entity.MealEntryEntity
import com.phoenix.fitpro.data.remote.OpenFoodFactsApi
import com.phoenix.fitpro.domain.model.FoodItem
import com.phoenix.fitpro.domain.model.MealEntry
import com.phoenix.fitpro.domain.model.MealType
import com.phoenix.fitpro.domain.model.StapleFoodDatabase
import com.phoenix.fitpro.domain.repository.FoodSearchOutcome
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

    override suspend fun searchFoodOnline(query: String): FoodSearchOutcome {
        val results = mutableListOf<FoodItem>()

        // 1. Correspondances immediates depuis la base d'aliments courants.
        //    Toujours disponibles, meme hors ligne ou si Open Food Facts limite.
        results.addAll(StapleFoodDatabase.search(query))

        // 2. Open Food Facts pour les produits de marque.
        var remoteFailed = false
        try {
            val isEn = java.util.Locale.getDefault().language.equals("en", ignoreCase = true)
            val response = api.searchProducts(query = query, lang = if (isEn) "en" else "fr")
            response.products
                .filter { it.isUsable }
                .forEach { product ->
                    if (results.none { it.name.equals(product.displayName, ignoreCase = true) }) {
                        results.add(
                            FoodItem(
                                name = product.displayName,
                                quantity = product.quantity ?: "100g",
                                calories = product.caloriesPer100g,
                                proteinG = product.proteinPer100g,
                                carbsG = product.carbsPer100g,
                                fatG = product.fatPer100g,
                                openFoodFactsId = product.code
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            // Hors ligne, ou Open Food Facts limite les clients anonymes et
            // renvoie une page HTML que Gson ne sait pas lire. Les resultats
            // locaux restent valides, mais l'ecran doit pouvoir le signaler
            // plutot que d'afficher une liste vide sans explication.
            remoteFailed = true
        }

        return FoodSearchOutcome(items = results, remoteFailed = remoteFailed)
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
