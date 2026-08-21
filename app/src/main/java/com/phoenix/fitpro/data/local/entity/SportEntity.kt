package com.phoenix.fitpro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sports")
data class SportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,       // SportCategory.name
    val iconName: String = "fitness_center",
    val colorArgb: Long = 0xFF4C6EF5,
    val isDefault: Boolean = false
)
