package com.phoenix.fitpro.presentation.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.fitpro.domain.repository.NutritionRepository
import com.phoenix.fitpro.domain.repository.UserRepository
import com.phoenix.fitpro.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val totalWorkouts: Int = 0,
    val weeklyWorkouts: Int = 0,
    val monthlyWorkouts: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val totalVolumeKg: Float = 0f,
    val weeklyCalories: List<Pair<LocalDate, Int>> = emptyList(),
    val workoutFrequency: List<Pair<LocalDate, Int>> = emptyList(),
    val weightEntries: List<Pair<LocalDate, Float>> = emptyList(),
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK
)

enum class StatsPeriod(val labelFr: String, val labelEn: String) {
    WEEK("Semaine", "Week"),
    MONTH("Mois", "Month"),
    YEAR("Année", "Year")
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val nutritionRepo: NutritionRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.WEEK)

    init {
        viewModelScope.launch {
            combine(
                userRepo.observeProfile(),
                workoutRepo.getTotalSessionCount(),
                userRepo.getRecentWeightEntries(),
                _selectedPeriod
            ) { profile, total, weightEntries, period ->
                val today = LocalDate.now()
                val (from, to) = when (period) {
                    StatsPeriod.WEEK  -> today.minusDays(6) to today
                    StatsPeriod.MONTH -> today.minusDays(29) to today
                    StatsPeriod.YEAR  -> today.minusDays(364) to today
                }

                val periodCount = workoutRepo.getSessionCountInRange(from, to)
                val totalVolume = workoutRepo.getTotalVolume(from, to)
                val activeDates = workoutRepo.getActiveDates(from)
                val weeklyCalories = nutritionRepo.getDailyCalories(today.minusDays(6), today)

                // Build frequency map (date → session count)
                val frequency = activeDates
                    .groupBy { it }
                    .map { (date, dates) -> date to dates.size }
                    .sortedBy { it.first }

                StatsUiState(
                    isLoading = false,
                    totalWorkouts = total,
                    weeklyWorkouts = if (period == StatsPeriod.WEEK) periodCount else workoutRepo.getSessionCountInRange(today.minusDays(6), today),
                    monthlyWorkouts = if (period == StatsPeriod.MONTH) periodCount else workoutRepo.getSessionCountInRange(today.minusDays(29), today),
                    bestStreak = profile?.longestStreak ?: 0,
                    currentStreak = profile?.currentStreak ?: 0,
                    totalVolumeKg = totalVolume,
                    weeklyCalories = weeklyCalories,
                    workoutFrequency = frequency,
                    weightEntries = weightEntries.map { it.date to it.weightKg },
                    selectedPeriod = period
                )
            }.collect { _uiState.value = it }
        }
    }

    fun selectPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
    }
}
