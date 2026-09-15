package com.phoenix.fitpro.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.domain.repository.NutritionRepository
import com.phoenix.fitpro.domain.repository.UserRepository
import com.phoenix.fitpro.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile(),
    val todayWorkouts: List<WorkoutSession> = emptyList(),
    val todayCalories: Int = 0,
    val weeklyWorkoutCount: Int = 0,
    val recentAchievement: Achievement? = null,
    val motivationalQuote: String = "",
    val currentStreak: Int = 0,
    /** Objectifs de la semaine, deduits des sports reellement pratiques. */
    val weeklyGoals: List<WeeklyGoal> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val nutritionRepo: NutritionRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.minusDays(6)

            // Combine all data flows
            combine(
                userRepo.observeProfile(),
                workoutRepo.getSessionsForDate(today),
                nutritionRepo.getMealsForDate(today),
                userRepo.observeAchievements()
            ) { profile, todayWorkouts, todayMeals, achievements ->
                val todayCalories = todayMeals.sumOf { it.totalCalories }
                val recentAchievement = achievements
                    .filter { it.isUnlocked }
                    .maxByOrNull { it.unlockedAt ?: LocalDate.MIN }

                DashboardUiState(
                    isLoading = false,
                    profile = profile ?: UserProfile(),
                    todayWorkouts = todayWorkouts,
                    todayCalories = todayCalories,
                    recentAchievement = recentAchievement,
                    motivationalQuote = MotivationalQuotes.getRandom(profile?.preferredLanguage ?: "fr"),
                    currentStreak = profile?.currentStreak ?: 0
                )
            }.collect { state ->
                // Fetch weekly count separately (not reactive, just a count)
                val weeklyCount = workoutRepo.getSessionCountInRange(weekStart, today)

                // Les objectifs se calibrent sur les quatre semaines precedentes,
                // il faut donc un historique plus large que la semaine en cours.
                val challengeStart = WeeklyChallenges.weekStart(today).minusWeeks(4)
                val recentSessions = workoutRepo
                    .getSessionsInRange(challengeStart, today)
                    .first()

                _uiState.value = state.copy(
                    weeklyWorkoutCount = weeklyCount,
                    weeklyGoals = WeeklyChallenges.generate(recentSessions, today)
                )
            }
        }
    }

    fun refreshQuote() {
        _uiState.update { current ->
            current.copy(
                motivationalQuote = MotivationalQuotes.getRandom(
                    current.profile.preferredLanguage
                )
            )
        }
    }
}
