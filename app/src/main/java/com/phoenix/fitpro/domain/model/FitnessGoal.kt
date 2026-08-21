package com.phoenix.fitpro.domain.model

/** Primary fitness objective selected by the user */
enum class FitnessGoal(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val targetCaloriesDelta: Int,      // kcal surplus or deficit relative to maintenance
    val recommendedProteinPerKg: Float, // g of protein per kg of bodyweight
    val recommendedCarbsRatio: Float,   // % of total energy
    val recommendedFatRatio: Float      // % of total energy
) {
    MASS_GAIN(
        id = "mass_gain",
        title = "Prise de masse",
        description = "Développement musculaire avec surplus calorique propre et fort apport protéique.",
        emoji = "🥩",
        targetCaloriesDelta = +350,
        recommendedProteinPerKg = 2.0f,
        recommendedCarbsRatio = 0.50f,
        recommendedFatRatio = 0.25f
    ),
    WEIGHT_LOSS(
        id = "weight_loss",
        title = "Perte de poids / Sèche",
        description = "Brûler les graisses en maintenant un déficit maîtrisé et en préservant le muscle.",
        emoji = "🔥",
        targetCaloriesDelta = -400,
        recommendedProteinPerKg = 2.2f,
        recommendedCarbsRatio = 0.35f,
        recommendedFatRatio = 0.25f
    ),
    MUSCLE_TONE(
        id = "muscle_tone",
        title = "Musculation & Force",
        description = "Gain de force et définition sans prise excessive de gras (maintien/léger surplus).",
        emoji = "💪",
        targetCaloriesDelta = +150,
        recommendedProteinPerKg = 1.8f,
        recommendedCarbsRatio = 0.45f,
        recommendedFatRatio = 0.25f
    ),
    HYBRID_ARCHERY_FITNESS(
        id = "hybrid_archery",
        title = "Performance Tir à l'arc & Gainage",
        description = "Renforcement du dos, épaules, posture, endurance cardiovasculaire et stabilité mentale.",
        emoji = "🏹",
        targetCaloriesDelta = 0,
        recommendedProteinPerKg = 1.6f,
        recommendedCarbsRatio = 0.45f,
        recommendedFatRatio = 0.30f
    ),
    HEALTH_ENDURANCE(
        id = "health_endurance",
        title = "Santé, Cardio & Maintien",
        description = "Condition physique globale, longévité, énergie au quotidien et mobilité.",
        emoji = "🏃",
        targetCaloriesDelta = 0,
        recommendedProteinPerKg = 1.4f,
        recommendedCarbsRatio = 0.50f,
        recommendedFatRatio = 0.25f
    )
}

/** User profile and constraints used by the AI Coach to tailor programs and advice */
data class CoachUserProfile(
    val goal: FitnessGoal = FitnessGoal.MUSCLE_TONE,
    val preferredSports: List<String> = listOf("Musculation", "Tir à l'arc", "Marche / Course"),
    val dislikedSportsOrExercises: List<String> = emptyList(), // e.g. "Burpees", "Course sur bitume"
    val healthIssuesAndInjuries: List<String> = emptyList(),   // e.g. "Douleur lombaire", "Genou fragile", "Tendinite épaule"
    val daysPerWeek: Int = 4,                                  // 2 to 6 days
    val sessionDurationMin: Int = 50,                          // average duration in minutes
    val experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val weightKg: Float = 75f,
    val heightCm: Int = 178,
    val additionalNotes: String = ""
)

enum class ExperienceLevel(val labelFr: String) {
    BEGINNER("Débutant (0 - 6 mois)"),
    INTERMEDIATE("Intermédiaire (6 mois - 2 ans)"),
    ADVANCED("Confirmé (+ 2 ans)")
}
