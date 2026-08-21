package com.phoenix.fitpro.domain.model

import java.time.LocalDate

data class MealEntry(
    val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val foods: List<FoodItem> = emptyList()
) {
    val totalCalories: Int get() = foods.sumOf { it.calories ?: 0 }
    val totalProtein: Float get() = foods.sumOf { (it.proteinG ?: 0f).toDouble() }.toFloat()
    val totalCarbs: Float get() = foods.sumOf { (it.carbsG ?: 0f).toDouble() }.toFloat()
    val totalFat: Float get() = foods.sumOf { (it.fatG ?: 0f).toDouble() }.toFloat()
    val hasCaloriesData: Boolean get() = foods.any { it.calories != null }
}

enum class MealType(val labelFr: String, val labelEn: String, val emoji: String, val order: Int) {
    BREAKFAST("Petit-déjeuner", "Breakfast", "🌅", 0),
    LUNCH("Déjeuner", "Lunch", "☀️", 1),
    DINNER("Dîner", "Dinner", "🌙", 2),
    SNACK("Collation", "Snack", "🍎", 3)
}

data class FoodItem(
    val id: Long = 0,
    val mealId: Long = 0,
    val name: String,
    val quantity: String = "1 portion",
    val calories: Int? = null,           // Optional — user may skip
    val proteinG: Float? = null,
    val carbsG: Float? = null,
    val fatG: Float? = null,
    val openFoodFactsId: String? = null  // Populated when fetched from API
) {
    val macroSummary: String
        get() = buildString {
            proteinG?.let { append("P: ${it.toInt()}g ") }
            carbsG?.let { append("G: ${it.toInt()}g ") }
            fatG?.let { append("L: ${it.toInt()}g") }
        }.trim()
}
