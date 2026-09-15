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

    /** Niveau recalcule depuis l'XP : fait foi meme si la colonne est desynchronisee. */
    val effectiveLevel: Int get() = LevelCurve.levelForXp(xp)
    val xpForNextLevel: Int get() = LevelCurve.totalXpForLevel(effectiveLevel + 1)
    val xpProgress: Float get() = LevelCurve.progress(xp)
    val xpCurrentLevelBase: Int get() = LevelCurve.totalXpForLevel(effectiveLevel)
    val xpInCurrentLevel: Int get() = LevelCurve.xpIntoCurrentLevel(xp)
    val xpRemainingToNextLevel: Int get() = LevelCurve.xpRemainingToNextLevel(xp)
    /** Cout total du niveau en cours, pour afficher "x / y" plutot qu'un cumul. */
    val xpSpanOfCurrentLevel: Int get() = LevelCurve.xpSpanOfLevel(effectiveLevel)

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
