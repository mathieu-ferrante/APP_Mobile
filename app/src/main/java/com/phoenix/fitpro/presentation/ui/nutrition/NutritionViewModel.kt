package com.phoenix.fitpro.presentation.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.fitpro.domain.ai.ChatMessage
import com.phoenix.fitpro.domain.ai.GeminiAiService
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.domain.repository.NutritionRepository
import com.phoenix.fitpro.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class NutritionUiState(
    val isLoading: Boolean = true,
    val todayMeals: List<MealEntry> = emptyList(),
    val totalCalories: Int = 0,
    val totalProteinG: Float = 0f,
    val totalCarbsG: Float = 0f,
    val totalFatG: Float = 0f,
    val weeklyCalories: List<Pair<LocalDate, Int>> = emptyList(),
    // Coach Nutrition IA
    val coachMessages: List<ChatMessage> = listOf(
        ChatMessage(
            isFromCoach = true,
            content = "🥗 Salut ! Je suis ton **Coach Nutrition IA**. Pose-moi tes questions sur tes macros, des idées de repas équilibrés ou comment optimiser ton alimentation !",
            quickActions = listOf("Analyser ma journée", "Idée de dîner riche en protéines", "Que manger avant l'entraînement ?")
        )
    ),
    val coachInput: String = "",
    val isCoachThinking: Boolean = false
)

data class AddMealUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMealType: MealType = MealType.LUNCH,
    val foods: MutableList<FoodItem> = mutableListOf(),
    val searchQuery: String = "",
    val searchResults: List<FoodItem> = emptyList(),
    val recentNames: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isAiEstimating: Boolean = false,
    val error: String? = null,
    // Manual entry fields
    val manualFoodName: String = "",
    val manualCalories: String = "",
    val manualProtein: String = "",
    val manualCarbs: String = "",
    val manualFat: String = "",
    val manualQuantity: String = "1 portion",
    val showManualForm: Boolean = false
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepo: NutritionRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(NutritionUiState())
    val listState: StateFlow<NutritionUiState> = _listState.asStateFlow()

    private val _addState = MutableStateFlow(AddMealUiState())
    val addState: StateFlow<AddMealUiState> = _addState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.minusDays(6)

            nutritionRepo.getMealsForDate(today).collect { meals ->
                val weeklyCalories = nutritionRepo.getDailyCalories(weekStart, today)
                val allFoods = meals.flatMap { it.foods }
                _listState.update { current ->
                    current.copy(
                        isLoading = false,
                        todayMeals = meals,
                        totalCalories = meals.sumOf { it.totalCalories },
                        totalProteinG = allFoods.mapNotNull { it.proteinG }.sum(),
                        totalCarbsG = allFoods.mapNotNull { it.carbsG }.sum(),
                        totalFatG = allFoods.mapNotNull { it.fatG }.sum(),
                        weeklyCalories = weeklyCalories
                    )
                }
            }
        }

        // Preload recent names
        viewModelScope.launch {
            val recents = nutritionRepo.searchRecentFoodNames("")
            _addState.update { it.copy(recentNames = recents) }
        }
    }

    fun setMealType(type: MealType) = _addState.update { it.copy(selectedMealType = type) }

    fun setMealDate(date: LocalDate) = _addState.update { it.copy(selectedDate = date) }

    fun setSearchQuery(query: String) {
        _addState.update { it.copy(searchQuery = query, isSearching = query.isNotBlank()) }
        if (query.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(200) // fast debounce
                val results = nutritionRepo.searchFoodOnline(query)
                val recentNames = nutritionRepo.searchRecentFoodNames(query)
                _addState.update { it.copy(searchResults = results, recentNames = recentNames, isSearching = false) }
            }
        } else {
            _addState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        }
    }

    fun addFoodFromSearch(food: FoodItem) {
        _addState.update { state ->
            val newFoods = state.foods.toMutableList()
            newFoods.add(food)
            state.copy(foods = newFoods, searchQuery = "", searchResults = emptyList())
        }
    }

    fun removeFood(index: Int) {
        _addState.update { state ->
            val newFoods = state.foods.toMutableList()
            newFoods.removeAt(index)
            state.copy(foods = newFoods)
        }
    }

    // ── AI Estimation for custom complex dishes ──────────────────────────────
    fun estimateDishWithAi(dishName: String) {
        if (dishName.isBlank()) return
        _addState.update { it.copy(isAiEstimating = true) }

        viewModelScope.launch {
            try {
                val prompt = """
Estime précisément les valeurs nutritionnelles moyennes pour 1 portion standard de : "$dishName".
Réponds UNIQUEMENT sur une seule ligne au format strict suivant :
Calories | Protéines(g) | Glucides(g) | Lipides(g) | Portion
Exemple : 450 | 28 | 45 | 18 | 1 assiette (350g)
"""
                val res = GeminiAiService.generate(prompt)
                val parts = res.split("|").map { it.trim() }
                if (parts.size >= 4) {
                    val cal = parts[0].filter { it.isDigit() }.toIntOrNull() ?: 350
                    val prot = parts[1].filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 20f
                    val carbs = parts[2].filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 35f
                    val fat = parts[3].filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 12f
                    val qty = parts.getOrNull(4)?.take(30) ?: "1 portion"

                    val item = FoodItem(
                        name = dishName.trim(),
                        quantity = qty,
                        calories = cal,
                        proteinG = prot,
                        carbsG = carbs,
                        fatG = fat
                    )
                    addFoodFromSearch(item)
                }
            } catch (e: Exception) {
                // fallback
                val item = FoodItem(
                    name = dishName.trim(),
                    quantity = "1 portion",
                    calories = 300,
                    proteinG = 15f,
                    carbsG = 30f,
                    fatG = 10f
                )
                addFoodFromSearch(item)
            } finally {
                _addState.update { it.copy(isAiEstimating = false) }
            }
        }
    }

    // Manual food form
    fun setManualFoodName(v: String) = _addState.update { it.copy(manualFoodName = v) }
    fun setManualCalories(v: String) = _addState.update { it.copy(manualCalories = v) }
    fun setManualProtein(v: String) = _addState.update { it.copy(manualProtein = v) }
    fun setManualCarbs(v: String) = _addState.update { it.copy(manualCarbs = v) }
    fun setManualFat(v: String) = _addState.update { it.copy(manualFat = v) }
    fun setManualQuantity(v: String) = _addState.update { it.copy(manualQuantity = v) }
    fun toggleManualForm() = _addState.update { it.copy(showManualForm = !it.showManualForm) }

    fun addManualFood() {
        val state = _addState.value
        if (state.manualFoodName.isBlank()) return

        val food = FoodItem(
            name = state.manualFoodName,
            quantity = state.manualQuantity,
            calories = state.manualCalories.toIntOrNull(),
            proteinG = state.manualProtein.toFloatOrNull(),
            carbsG = state.manualCarbs.toFloatOrNull(),
            fatG = state.manualFat.toFloatOrNull()
        )
        _addState.update { s ->
            val foods = s.foods.toMutableList()
            foods.add(food)
            s.copy(
                foods = foods,
                manualFoodName = "", manualCalories = "",
                manualProtein = "", manualCarbs = "", manualFat = "",
                manualQuantity = "1 portion", showManualForm = false
            )
        }
    }

    fun saveMeal() {
        val state = _addState.value
        if (state.foods.isEmpty()) {
            _addState.update { it.copy(error = "Ajoutez au moins un aliment") }
            return
        }
        viewModelScope.launch {
            _addState.update { it.copy(isSaving = true, error = null) }
            try {
                val meal = MealEntry(
                    date = state.selectedDate,
                    mealType = state.selectedMealType,
                    foods = state.foods
                )
                nutritionRepo.addMealWithFoods(meal)
                userRepo.unlockAchievement("first_meal")
                _addState.update { it.copy(isSaving = false, saveSuccess = true, foods = mutableListOf()) }
            } catch (e: Exception) {
                _addState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteMeal(meal: MealEntry) {
        viewModelScope.launch { nutritionRepo.deleteMeal(meal) }
    }

    // ── Nutrition Coach AI ───────────────────────────────────────────────────
    fun setCoachInput(v: String) = _listState.update { it.copy(coachInput = v) }

    fun sendCoachMessage(customText: String? = null) {
        val text = (customText ?: _listState.value.coachInput).trim()
        if (text.isBlank()) return

        val userMsg = ChatMessage(isFromCoach = false, content = text)
        val currentState = _listState.value

        _listState.update {
            it.copy(
                coachMessages = it.coachMessages + userMsg,
                coachInput = "",
                isCoachThinking = true
            )
        }

        viewModelScope.launch {
            val totalCal = currentState.totalCalories
            val totalProt = currentState.totalProteinG.toInt()
            val totalCarbs = currentState.totalCarbsG.toInt()
            val totalFat = currentState.totalFatG.toInt()
            val mealCount = currentState.todayMeals.size
            val mealsSummary = currentState.todayMeals.joinToString("; ") { meal ->
                "${meal.mealType.labelFr}: " + meal.foods.joinToString(", ") { it.name }
            }

            val context = """
Données de nutrition de l'utilisateur aujourd'hui :
- Nombre de repas enregistrés : $mealCount
- Total calories consommées : $totalCal kcal
- Macros : Protéines ${totalProt}g, Glucides ${totalCarbs}g, Lipides ${totalFat}g
- Repas du jour : $mealsSummary
Tu es un expert en nutrition sportive, bienveillant, précis et motivant.
"""

            val reply = try {
                GeminiAiService.ask(text, context)
            } catch (e: Exception) {
                "Pour aujourd'hui tu as consommé **$totalCal kcal** et **${totalProt}g de protéines**. N'hésite pas à équilibrer avec des légumes et une bonne hydratation !"
            }

            val coachReply = ChatMessage(
                isFromCoach = true,
                content = reply,
                quickActions = listOf("Idée de collation saine", "Analyser mes protéines", "Conseils hydratation")
            )

            _listState.update {
                it.copy(
                    coachMessages = it.coachMessages + coachReply,
                    isCoachThinking = false
                )
            }
        }
    }

    fun clearSaveSuccess() = _addState.update { it.copy(saveSuccess = false) }
    fun clearError() = _addState.update { it.copy(error = null) }
}
