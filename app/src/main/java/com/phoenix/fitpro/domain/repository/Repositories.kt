package com.phoenix.fitpro.domain.repository

import com.phoenix.fitpro.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WorkoutRepository {
    fun getSessionsForDate(date: LocalDate): Flow<List<WorkoutSession>>
    fun getAllSessions(): Flow<List<WorkoutSession>>
    fun getSessionsInRange(from: LocalDate, to: LocalDate): Flow<List<WorkoutSession>>
    fun getTotalSessionCount(): Flow<Int>
    fun getAllSports(): Flow<List<Sport>>
    suspend fun addSession(session: WorkoutSession): Long
    suspend fun updateSession(session: WorkoutSession)
    suspend fun deleteSession(session: WorkoutSession)
    suspend fun addSport(sport: Sport): Long
    suspend fun deleteSport(sport: Sport)
    suspend fun getTopSports(): List<Pair<Sport, Int>>
    suspend fun getTotalVolume(from: LocalDate, to: LocalDate): Float
    suspend fun getSessionCountInRange(from: LocalDate, to: LocalDate): Int
    suspend fun getActiveDates(since: LocalDate): List<LocalDate>
}

/**
 * Resultat d'une recherche d'aliment.
 *
 * [remoteFailed] distingue "le service n'a rien trouve" de "le service n'a pas
 * repondu" : Open Food Facts limite les clients anonymes et renvoie alors une
 * page HTML. Sans cette distinction l'utilisateur voyait une liste vide sans
 * savoir que la recherche avait echoue.
 */
data class FoodSearchOutcome(
    val items: List<FoodItem> = emptyList(),
    val remoteFailed: Boolean = false
)

interface NutritionRepository {
    fun getMealsForDate(date: LocalDate): Flow<List<MealEntry>>
    suspend fun addMealWithFoods(meal: MealEntry): Long
    suspend fun deleteMeal(meal: MealEntry)
    suspend fun deleteFood(food: FoodItem)
    suspend fun getTotalCaloriesForDate(date: LocalDate): Int
    suspend fun getDailyCalories(from: LocalDate, to: LocalDate): List<Pair<LocalDate, Int>>
    suspend fun searchFoodOnline(query: String): FoodSearchOutcome
    suspend fun searchRecentFoodNames(query: String): List<String>
}

interface UserRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile
    suspend fun saveProfile(profile: UserProfile)
    suspend fun addWeightEntry(date: LocalDate, weightKg: Float)
    fun getRecentWeightEntries(): Flow<List<WeightEntry>>
    fun observeAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(id: String): Achievement?
    suspend fun updateStreak(activeToday: Boolean): Int
    suspend fun addXp(amount: Int)

    /**
     * Insère les définitions de succès absentes de la base.
     * Les succès ne sont semés qu'à la création de la base : sans cela, ceux
     * ajoutés par une mise à jour resteraient invisibles pour les comptes
     * existants. L'insertion ignore les conflits, les déblocages sont préservés.
     */
    suspend fun syncAchievementDefinitions()
}

interface ProgramRepository {
    fun observeCurrentProgram(): Flow<TrainingProgram?>
    suspend fun getCurrentProgram(): TrainingProgram?
    suspend fun saveProgram(program: TrainingProgram)
    fun observeCoachProfile(): Flow<CoachUserProfile>
    suspend fun getCoachProfile(): CoachUserProfile
    suspend fun saveCoachProfile(profile: CoachUserProfile)
}

