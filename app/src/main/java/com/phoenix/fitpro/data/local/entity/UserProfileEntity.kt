package com.phoenix.fitpro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phoenix.fitpro.domain.model.ObjectiveType
import java.time.LocalDate

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "",
    val photoUri: String? = null,
    val weightKg: Float? = null,
    val heightCm: Float? = null,
    val objectiveType: String = ObjectiveType.GENERAL_FITNESS.name,
    val level: Int = 1,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long? = null,   // LocalDate.toEpochDay()
    val preferredLanguage: String = "fr",
    val notificationsEnabled: Boolean = true,
    val firebaseUid: String? = null
)

@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,          // LocalDate.toEpochDay()
    val weightKg: Float
)
