package com.phoenix.fitpro.data.local.dao

import androidx.room.*
import com.phoenix.fitpro.data.local.entity.ExerciseSetEntity
import com.phoenix.fitpro.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    // ── Session queries ───────────────────────────────────────────────────────

    @Query("SELECT * FROM workout_sessions WHERE date = :epochDay ORDER BY startTime ASC")
    fun getSessionsForDate(epochDay: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC, startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :fromEpoch AND :toEpoch ORDER BY date ASC, startTime ASC")
    fun getSessionsInRange(fromEpoch: Long, toEpoch: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Query("SELECT COUNT(*) FROM workout_sessions")
    fun getTotalSessionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE date = :epochDay")
    suspend fun getSessionCountForDate(epochDay: Long): Int

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE date BETWEEN :fromEpoch AND :toEpoch")
    suspend fun getSessionCountInRange(fromEpoch: Long, toEpoch: Long): Int

    @Query("SELECT DISTINCT date FROM workout_sessions WHERE date >= :fromEpoch ORDER BY date DESC")
    suspend fun getDistinctActiveDates(fromEpoch: Long): List<Long>

    @Query("SELECT SUM(s.weightKg * s.sets * s.reps) FROM exercise_sets s INNER JOIN workout_sessions w ON s.sessionId = w.id WHERE w.date BETWEEN :fromEpoch AND :toEpoch")
    suspend fun getTotalVolume(fromEpoch: Long, toEpoch: Long): Float?

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    // ── Exercise queries ──────────────────────────────────────────────────────

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getExercisesForSession(sessionId: Long): List<ExerciseSetEntity>

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun observeExercisesForSession(sessionId: Long): Flow<List<ExerciseSetEntity>>

    @Insert
    suspend fun insertExercise(exercise: ExerciseSetEntity): Long

    @Insert
    suspend fun insertExercises(exercises: List<ExerciseSetEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseSetEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseSetEntity)

    @Query("DELETE FROM exercise_sets WHERE sessionId = :sessionId")
    suspend fun deleteExercisesForSession(sessionId: Long)

    // ── Stats queries ─────────────────────────────────────────────────────────

    @Query("SELECT sportId, COUNT(*) as count FROM workout_sessions GROUP BY sportId ORDER BY count DESC LIMIT 5")
    suspend fun getTopSports(): List<SportCount>

    @Query("SELECT date, COUNT(*) as sessionCount FROM workout_sessions GROUP BY date ORDER BY date DESC LIMIT 90")
    suspend fun getDailySessionCounts(): List<DailyCount>
}

data class SportCount(val sportId: Long, val count: Int)
data class DailyCount(val date: Long, val sessionCount: Int)
