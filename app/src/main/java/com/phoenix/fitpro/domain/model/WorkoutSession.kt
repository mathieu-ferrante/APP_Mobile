package com.phoenix.fitpro.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class WorkoutSession(
    val id: Long = 0,
    val sport: Sport,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
    val intensity: Int = 3,         // 1-5 stars
    val notes: String = "",
    val exercises: List<ExerciseSet> = emptyList(),
    val caloriesBurned: Int? = null,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceDays: List<DayOfWeek> = emptyList(),
    val createdAt: LocalDate = LocalDate.now()
) {
    val durationMinutes: Int?
        get() = endTime?.let {
            val start = startTime.toSecondOfDay()
            val end = it.toSecondOfDay()
            if (end >= start) (end - start) / 60 else null
        }

    val totalVolume: Float
        get() = exercises.sumOf { ex ->
            ((ex.sets ?: 1) * (ex.reps ?: 1) * (ex.weightKg ?: 0f)).toDouble()
        }.toFloat()
}

enum class RecurrenceType(val labelFr: String, val labelEn: String) {
    NONE("Unique", "Once"),
    DAILY("Quotidien", "Daily"),
    WEEKLY("Hebdomadaire", "Weekly"),
    CUSTOM("Personnalisé", "Custom")
}

data class ExerciseSet(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseName: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Float? = null,
    val durationSeconds: Int? = null,
    val distanceKm: Float? = null,
    val orderIndex: Int = 0
) {
    val isStrengthExercise: Boolean get() = sets != null || reps != null || weightKg != null
    val isCardioExercise: Boolean get() = durationSeconds != null || distanceKm != null
}
