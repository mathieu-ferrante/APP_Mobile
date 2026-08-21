package com.phoenix.fitpro.data.repository

import com.phoenix.fitpro.data.local.dao.SportDao
import com.phoenix.fitpro.data.local.dao.WorkoutSessionDao
import com.phoenix.fitpro.data.local.entity.ExerciseSetEntity
import com.phoenix.fitpro.data.local.entity.WorkoutSessionEntity
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val sessionDao: WorkoutSessionDao,
    private val sportDao: SportDao
) : WorkoutRepository {

    override fun getSessionsForDate(date: LocalDate): Flow<List<WorkoutSession>> {
        return sessionDao.getSessionsForDate(date.toEpochDay()).map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun getAllSessions(): Flow<List<WorkoutSession>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun getSessionsInRange(from: LocalDate, to: LocalDate): Flow<List<WorkoutSession>> {
        return sessionDao.getSessionsInRange(from.toEpochDay(), to.toEpochDay()).map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun getTotalSessionCount(): Flow<Int> = sessionDao.getTotalSessionCount()

    override suspend fun addSession(session: WorkoutSession): Long {
        val entity = session.toEntity()
        val sessionId = sessionDao.insertSession(entity)
        if (session.exercises.isNotEmpty()) {
            val exerciseEntities = session.exercises.mapIndexed { i, ex ->
                ex.toEntity(sessionId = sessionId, order = i)
            }
            sessionDao.insertExercises(exerciseEntities)
        }
        return sessionId
    }

    override suspend fun updateSession(session: WorkoutSession) {
        sessionDao.updateSession(session.toEntity())
        sessionDao.deleteExercisesForSession(session.id)
        val exerciseEntities = session.exercises.mapIndexed { i, ex ->
            ex.toEntity(sessionId = session.id, order = i)
        }
        sessionDao.insertExercises(exerciseEntities)
    }

    override suspend fun deleteSession(session: WorkoutSession) {
        sessionDao.deleteSessionById(session.id)
    }

    override fun getAllSports(): Flow<List<Sport>> {
        return sportDao.getAllSports().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addSport(sport: Sport): Long {
        return sportDao.insertSport(sport.toEntity())
    }

    override suspend fun deleteSport(sport: Sport) {
        if (!sport.isDefault) sportDao.deleteSport(sport.toEntity())
    }

    override suspend fun getTopSports(): List<Pair<Sport, Int>> {
        return sessionDao.getTopSports().mapNotNull { sc ->
            val sport = sportDao.getSportById(sc.sportId)?.toDomain() ?: return@mapNotNull null
            Pair(sport, sc.count)
        }
    }

    override suspend fun getTotalVolume(from: LocalDate, to: LocalDate): Float {
        return sessionDao.getTotalVolume(from.toEpochDay(), to.toEpochDay()) ?: 0f
    }

    override suspend fun getSessionCountInRange(from: LocalDate, to: LocalDate): Int {
        return sessionDao.getSessionCountInRange(from.toEpochDay(), to.toEpochDay())
    }

    override suspend fun getActiveDates(since: LocalDate): List<LocalDate> {
        return sessionDao.getDistinctActiveDates(since.toEpochDay())
            .map { LocalDate.ofEpochDay(it) }
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private suspend fun WorkoutSessionEntity.toDomain(): WorkoutSession? {
        val sport = sportDao.getSportById(sportId)?.toDomain() ?: return null
        val exercises = sessionDao.getExercisesForSession(id).map { it.toDomain() }
        return WorkoutSession(
            id = id,
            sport = sport,
            date = LocalDate.ofEpochDay(date),
            startTime = LocalTime.parse(startTime),
            endTime = endTime?.let { LocalTime.parse(it) },
            intensity = intensity,
            notes = notes,
            exercises = exercises,
            caloriesBurned = caloriesBurned,
            recurrenceType = RecurrenceType.valueOf(recurrenceType),
            recurrenceDays = recurrenceDays.split(",")
                .filter { it.isNotBlank() }
                .map { DayOfWeek.valueOf(it) },
            createdAt = LocalDate.ofEpochDay(createdAt)
        )
    }

    private fun WorkoutSession.toEntity() = WorkoutSessionEntity(
        id = id,
        sportId = sport.id,
        date = date.toEpochDay(),
        startTime = startTime.toString(),
        endTime = endTime?.toString(),
        intensity = intensity,
        notes = notes,
        caloriesBurned = caloriesBurned,
        recurrenceType = recurrenceType.name,
        recurrenceDays = recurrenceDays.joinToString(",") { it.name },
        createdAt = createdAt.toEpochDay()
    )

    private fun ExerciseSetEntity.toDomain() = ExerciseSet(
        id = id, sessionId = sessionId, exerciseName = exerciseName,
        sets = sets, reps = reps, weightKg = weightKg,
        durationSeconds = durationSeconds, distanceKm = distanceKm, orderIndex = orderIndex
    )

    private fun ExerciseSet.toEntity(sessionId: Long, order: Int) = ExerciseSetEntity(
        id = if (id == 0L) 0 else id,
        sessionId = sessionId, exerciseName = exerciseName,
        sets = sets, reps = reps, weightKg = weightKg,
        durationSeconds = durationSeconds, distanceKm = distanceKm, orderIndex = order
    )
}

private fun com.phoenix.fitpro.data.local.entity.SportEntity.toDomain() = Sport(
    id = id, name = name,
    category = com.phoenix.fitpro.domain.model.SportCategory.valueOf(category),
    iconName = iconName, colorArgb = colorArgb, isDefault = isDefault
)

private fun Sport.toEntity() = com.phoenix.fitpro.data.local.entity.SportEntity(
    id = id, name = name, category = category.name,
    iconName = iconName, colorArgb = colorArgb, isDefault = isDefault
)
