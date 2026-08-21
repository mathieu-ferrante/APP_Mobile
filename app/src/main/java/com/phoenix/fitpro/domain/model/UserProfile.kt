package com.phoenix.fitpro.domain.model

import java.time.LocalDate

data class UserProfile(
    val id: Long = 1,
    val name: String = "",
    val photoUri: String? = null,
    val weightKg: Float? = null,
    val heightCm: Float? = null,
    val objectiveType: ObjectiveType = ObjectiveType.GENERAL_FITNESS,
    val level: Int = 1,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: LocalDate? = null,
    val preferredLanguage: String = "fr",
    val notificationsEnabled: Boolean = true,
    val firebaseUid: String? = null,
    val email: String? = null
) {
    val bmi: Float?
        get() = if (weightKg != null && heightCm != null && heightCm > 0) {
            weightKg / ((heightCm / 100f) * (heightCm / 100f))
        } else null

    val xpForNextLevel: Int get() = level * 500
    val xpProgress: Float get() = (xp % 500) / 500f
    val xpCurrentLevelBase: Int get() = (level - 1) * 500
    val xpInCurrentLevel: Int get() = xp - xpCurrentLevelBase

    companion object {
        const val XP_PER_WORKOUT = 50
        const val XP_PER_ACHIEVEMENT = 100
        const val XP_PER_STREAK_DAY = 10
    }
}

enum class ObjectiveType(val labelFr: String, val labelEn: String, val emoji: String) {
    WEIGHT_LOSS("Perte de poids", "Weight Loss", "⚖️"),
    MUSCLE_GAIN("Prise de masse", "Muscle Gain", "💪"),
    ENDURANCE("Endurance", "Endurance", "🏃"),
    GENERAL_FITNESS("Remise en forme", "General Fitness", "🏋️"),
    FLEXIBILITY("Flexibilité", "Flexibility", "🧘")
}

data class WeightEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float
)
