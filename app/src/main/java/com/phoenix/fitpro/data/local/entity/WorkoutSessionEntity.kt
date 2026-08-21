package com.phoenix.fitpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [ForeignKey(
        entity = SportEntity::class,
        parentColumns = ["id"],
        childColumns = ["sportId"],
        onDelete = ForeignKey.SET_DEFAULT
    )],
    indices = [Index("sportId"), Index("date")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sportId: Long,
    val date: Long,             // LocalDate.toEpochDay()
    val startTime: String,      // LocalTime.toString()
    val endTime: String? = null,
    val intensity: Int = 3,
    val notes: String = "",
    val caloriesBurned: Int? = null,
    val recurrenceType: String = "NONE",
    val recurrenceDays: String = "",    // comma-separated DayOfWeek names
    val createdAt: Long = 0             // LocalDate.toEpochDay()
)

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [ForeignKey(
        entity = WorkoutSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Float? = null,
    val durationSeconds: Int? = null,
    val distanceKm: Float? = null,
    val orderIndex: Int = 0
)
