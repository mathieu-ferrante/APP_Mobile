package com.phoenix.fitpro.data.remote.sync

import com.phoenix.fitpro.data.local.entity.AchievementEntity
import com.phoenix.fitpro.data.local.entity.ExerciseSetEntity
import com.phoenix.fitpro.data.local.entity.FoodItemEntity
import com.phoenix.fitpro.data.local.entity.MealEntryEntity
import com.phoenix.fitpro.data.local.entity.SportEntity
import com.phoenix.fitpro.data.local.entity.UserProfileEntity
import com.phoenix.fitpro.data.local.entity.WeightEntryEntity
import com.phoenix.fitpro.data.local.entity.WorkoutSessionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Photographie complète des données d'un utilisateur, stockée telle quelle
 * dans la colonne jsonb `payload` de la table Supabase `user_data`.
 */
@Serializable
data class SyncSnapshot(
    val schemaVersion: Int = SCHEMA_VERSION,
    val savedAtEpochMs: Long = System.currentTimeMillis(),
    val profiles: List<ProfileDto> = emptyList(),
    val weightEntries: List<WeightEntryDto> = emptyList(),
    val sports: List<SportDto> = emptyList(),
    val sessions: List<SessionDto> = emptyList(),
    val exerciseSets: List<ExerciseSetDto> = emptyList(),
    val meals: List<MealDto> = emptyList(),
    val foodItems: List<FoodItemDto> = emptyList(),
    val achievements: List<AchievementDto> = emptyList(),
    /** JSON brut du programme du coach (SharedPreferences). */
    val coachProgramJson: String? = null,
    /** JSON brut du profil coach (SharedPreferences). */
    val coachProfileJson: String? = null
) {
    companion object {
        const val SCHEMA_VERSION = 1
    }
}

@Serializable
data class ProfileDto(
    val id: Long = 1,
    val name: String = "",
    val photoUri: String? = null,
    val weightKg: Float? = null,
    val heightCm: Float? = null,
    val objectiveType: String = "GENERAL_FITNESS",
    val level: Int = 1,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long? = null,
    val preferredLanguage: String = "fr",
    val notificationsEnabled: Boolean = true
)

@Serializable
data class WeightEntryDto(val id: Long = 0, val date: Long, val weightKg: Float)

@Serializable
data class SportDto(
    val id: Long = 0,
    val name: String,
    val category: String,
    val iconName: String = "fitness_center",
    val colorArgb: Long = 0xFF4C6EF5,
    val isDefault: Boolean = false
)

@Serializable
data class SessionDto(
    val id: Long = 0,
    val sportId: Long,
    val date: Long,
    val startTime: String,
    val endTime: String? = null,
    val intensity: Int = 3,
    val notes: String = "",
    val caloriesBurned: Int? = null,
    val recurrenceType: String = "NONE",
    val recurrenceDays: String = "",
    val createdAt: Long = 0
)

@Serializable
data class ExerciseSetDto(
    val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Float? = null,
    val durationSeconds: Int? = null,
    val distanceKm: Float? = null,
    val orderIndex: Int = 0
)

@Serializable
data class MealDto(val id: Long = 0, val date: Long, val mealType: String)

@Serializable
data class FoodItemDto(
    val id: Long = 0,
    val mealId: Long,
    val name: String,
    val quantity: String = "1 portion",
    val calories: Int? = null,
    val proteinG: Float? = null,
    val carbsG: Float? = null,
    val fatG: Float? = null,
    val openFoodFactsId: String? = null
)

@Serializable
data class AchievementDto(
    val id: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

/** Ligne de la table Supabase `user_data` lue depuis le serveur. */
@Serializable
data class UserDataRow(
    @SerialName("user_id") val userId: String,
    val payload: SyncSnapshot,
    @SerialName("updated_at") val updatedAt: String? = null
)

/** Payload envoyé au serveur (sans `updated_at`, géré par Postgres). */
@Serializable
data class UserDataUpsert(
    @SerialName("user_id") val userId: String,
    val payload: SyncSnapshot
)

// ── Conversions entité Room <-> DTO ──────────────────────────────────────────

fun UserProfileEntity.toDto() = ProfileDto(
    id, name, photoUri, weightKg, heightCm, objectiveType, level, xp,
    currentStreak, longestStreak, lastActivityDate, preferredLanguage, notificationsEnabled
)

fun ProfileDto.toEntity() = UserProfileEntity(
    id = id, name = name, photoUri = photoUri, weightKg = weightKg, heightCm = heightCm,
    objectiveType = objectiveType, level = level, xp = xp,
    currentStreak = currentStreak, longestStreak = longestStreak,
    lastActivityDate = lastActivityDate, preferredLanguage = preferredLanguage,
    notificationsEnabled = notificationsEnabled
)

fun WeightEntryEntity.toDto() = WeightEntryDto(id, date, weightKg)
fun WeightEntryDto.toEntity() = WeightEntryEntity(id, date, weightKg)

fun SportEntity.toDto() = SportDto(id, name, category, iconName, colorArgb, isDefault)
fun SportDto.toEntity() = SportEntity(id, name, category, iconName, colorArgb, isDefault)

fun WorkoutSessionEntity.toDto() = SessionDto(
    id, sportId, date, startTime, endTime, intensity, notes,
    caloriesBurned, recurrenceType, recurrenceDays, createdAt
)

fun SessionDto.toEntity() = WorkoutSessionEntity(
    id, sportId, date, startTime, endTime, intensity, notes,
    caloriesBurned, recurrenceType, recurrenceDays, createdAt
)

fun ExerciseSetEntity.toDto() = ExerciseSetDto(
    id, sessionId, exerciseName, sets, reps, weightKg, durationSeconds, distanceKm, orderIndex
)

fun ExerciseSetDto.toEntity() = ExerciseSetEntity(
    id, sessionId, exerciseName, sets, reps, weightKg, durationSeconds, distanceKm, orderIndex
)

fun MealEntryEntity.toDto() = MealDto(id, date, mealType)
fun MealDto.toEntity() = MealEntryEntity(id, date, mealType)

fun FoodItemEntity.toDto() = FoodItemDto(
    id, mealId, name, quantity, calories, proteinG, carbsG, fatG, openFoodFactsId
)

fun FoodItemDto.toEntity() = FoodItemEntity(
    id, mealId, name, quantity, calories, proteinG, carbsG, fatG, openFoodFactsId
)

fun AchievementEntity.toDto() = AchievementDto(id, isUnlocked, unlockedAt)
fun AchievementDto.toEntity() = AchievementEntity(id, isUnlocked, unlockedAt)
