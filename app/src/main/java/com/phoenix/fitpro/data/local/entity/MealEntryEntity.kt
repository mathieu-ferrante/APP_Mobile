package com.phoenix.fitpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meal_entries", indices = [Index("date")])
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,             // LocalDate.toEpochDay()
    val mealType: String        // MealType.name
)

@Entity(
    tableName = "food_items",
    foreignKeys = [ForeignKey(
        entity = MealEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["mealId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("mealId")]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val name: String,
    val quantity: String = "1 portion",
    val calories: Int? = null,
    val proteinG: Float? = null,
    val carbsG: Float? = null,
    val fatG: Float? = null,
    val openFoodFactsId: String? = null
)
