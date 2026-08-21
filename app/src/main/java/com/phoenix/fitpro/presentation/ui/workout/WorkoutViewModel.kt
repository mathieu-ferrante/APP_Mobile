package com.phoenix.fitpro.presentation.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.domain.repository.UserRepository
import com.phoenix.fitpro.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val todaySessions: List<WorkoutSession> = emptyList(),
    val recentSessions: List<WorkoutSession> = emptyList(),
    val sports: List<Sport> = emptyList(),
    val totalCount: Int = 0
)

data class AddWorkoutUiState(
    val sports: List<Sport> = emptyList(),
    val selectedSport: Sport? = null,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val endTime: LocalTime? = null,
    val intensity: Int = 3,
    val notes: String = "",
    val exercises: List<ExerciseSet> = emptyList(),
    val caloriesBurned: String = "",
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceDays: Set<DayOfWeek> = emptySet(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(WorkoutUiState())
    val listState: StateFlow<WorkoutUiState> = _listState.asStateFlow()

    private val _addState = MutableStateFlow(AddWorkoutUiState())
    val addState: StateFlow<AddWorkoutUiState> = _addState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                workoutRepo.getSessionsForDate(LocalDate.now()),
                workoutRepo.getAllSessions(),
                workoutRepo.getAllSports(),
                workoutRepo.getTotalSessionCount()
            ) { today, all, sports, count ->
                WorkoutUiState(
                    isLoading = false,
                    todaySessions = today,
                    recentSessions = all.take(20),
                    sports = sports,
                    totalCount = count
                )
            }.collect { _listState.value = it }
        }

        // Load sports for the add form
        viewModelScope.launch {
            workoutRepo.getAllSports().collect { sports ->
                _addState.update { it.copy(sports = sports, selectedSport = it.selectedSport ?: sports.firstOrNull()) }
            }
        }
    }

    // ── Add workout form ──────────────────────────────────────────────────────

    fun selectSport(sport: Sport) = _addState.update { it.copy(selectedSport = sport) }
    fun setDate(date: LocalDate) = _addState.update { it.copy(date = date) }
    fun setStartTime(time: LocalTime) = _addState.update { it.copy(startTime = time) }
    fun setEndTime(time: LocalTime?) = _addState.update { it.copy(endTime = time) }
    fun setIntensity(v: Int) = _addState.update { it.copy(intensity = v) }
    fun setNotes(v: String) = _addState.update { it.copy(notes = v) }
    fun setCaloriesBurned(v: String) = _addState.update { it.copy(caloriesBurned = v) }
    fun setRecurrenceType(v: RecurrenceType) = _addState.update { it.copy(recurrenceType = v) }
    fun toggleRecurrenceDay(day: DayOfWeek) = _addState.update { state ->
        val days = state.recurrenceDays.toMutableSet()
        if (day in days) days.remove(day) else days.add(day)
        state.copy(recurrenceDays = days)
    }

    fun addExercise(exercise: ExerciseSet) = _addState.update { state ->
        state.copy(exercises = state.exercises + exercise)
    }

    fun removeExercise(index: Int) = _addState.update { state ->
        state.copy(exercises = state.exercises.filterIndexed { i, _ -> i != index })
    }

    fun saveWorkout() {
        val state = _addState.value
        if (state.selectedSport == null) {
            _addState.update { it.copy(error = "Veuillez sélectionner un sport") }
            return
        }

        viewModelScope.launch {
            _addState.update { it.copy(isSaving = true, error = null) }
            try {
                val session = WorkoutSession(
                    sport = state.selectedSport,
                    date = state.date,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    intensity = state.intensity,
                    notes = state.notes,
                    exercises = state.exercises,
                    caloriesBurned = state.caloriesBurned.toIntOrNull(),
                    recurrenceType = state.recurrenceType,
                    recurrenceDays = state.recurrenceDays.toList()
                )
                workoutRepo.addSession(session)

                // Update streak & XP
                userRepo.updateStreak(activeToday = state.date == LocalDate.now())
                userRepo.addXp(UserProfile.XP_PER_WORKOUT)

                // Check achievements
                checkAchievements()

                _addState.update { it.copy(isSaving = false, saveSuccess = true) }
                // Reset form
                _addState.value = AddWorkoutUiState(sports = state.sports, selectedSport = state.sports.firstOrNull())

            } catch (e: Exception) {
                _addState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteSession(session: WorkoutSession) {
        viewModelScope.launch { workoutRepo.deleteSession(session) }
    }

    fun addCustomSport(name: String, category: SportCategory) {
        viewModelScope.launch {
            workoutRepo.addSport(Sport(name = name, category = category))
        }
    }

    fun deleteSport(sport: Sport) {
        viewModelScope.launch { workoutRepo.deleteSport(sport) }
    }

    private suspend fun checkAchievements() {
        val totalCount = workoutRepo.getSessionCountInRange(LocalDate.MIN, LocalDate.MAX)
        val todayCount = workoutRepo.getSessionCountInRange(LocalDate.now(), LocalDate.now())

        if (totalCount >= 1) userRepo.unlockAchievement("first_workout")
        if (totalCount >= 5) userRepo.unlockAchievement("workouts_5")
        if (totalCount >= 25) userRepo.unlockAchievement("workouts_25")
        if (totalCount >= 50) userRepo.unlockAchievement("workouts_50")
        if (totalCount >= 100) userRepo.unlockAchievement("workouts_100")
        if (todayCount >= 2) userRepo.unlockAchievement("double_day")
        if (todayCount >= 3) userRepo.unlockAchievement("triple_day")

        val currentHour = LocalTime.now().hour
        if (currentHour < 7) userRepo.unlockAchievement("early_bird")
        if (currentHour >= 21) userRepo.unlockAchievement("night_owl")

        val profile = userRepo.getProfile()
        if (profile.currentStreak >= 3) userRepo.unlockAchievement("streak_3")
        if (profile.currentStreak >= 7) userRepo.unlockAchievement("streak_7")
        if (profile.currentStreak >= 30) userRepo.unlockAchievement("streak_30")
        if (profile.currentStreak >= 100) userRepo.unlockAchievement("streak_100")
        if (profile.level >= 5) userRepo.unlockAchievement("level_5")
        if (profile.level >= 10) userRepo.unlockAchievement("level_10")
    }

    fun clearSaveSuccess() = _addState.update { it.copy(saveSuccess = false) }
    fun clearError() = _addState.update { it.copy(error = null) }
}
