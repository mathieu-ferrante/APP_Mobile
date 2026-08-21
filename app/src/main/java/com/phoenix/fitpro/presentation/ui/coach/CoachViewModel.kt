package com.phoenix.fitpro.presentation.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.fitpro.domain.ai.*
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.domain.repository.NutritionRepository
import com.phoenix.fitpro.domain.repository.ProgramRepository
import com.phoenix.fitpro.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CoachProgramUiState(
    val isLoading: Boolean = true,
    val selectedTab: CoachTab = CoachTab.PROGRAM,
    val program: TrainingProgram? = null,
    val profile: CoachUserProfile = CoachUserProfile(),
    val report: CoachAnalysisReport? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val chatInput: String = "",
    val isGeneratingProgram: Boolean = false,
    val isThinking: Boolean = false,
    val showGeneratorDialog: Boolean = false,
    // Generator temporary form state
    val formGoal: FitnessGoal = FitnessGoal.MUSCLE_TONE,
    val formPreferredSports: String = "Musculation, Tir à l'arc, Marche / Course",
    val formDislikedSports: String = "",
    val formHealthIssues: String = "",
    val formDaysPerWeek: Int = 4,
    val formDurationMin: Int = 50,
    val formLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE
)

enum class CoachTab(val title: String, val emoji: String) {
    PROGRAM("Programme", "📅"),
    DIAGNOSTIC("Diagnostic IA", "📊"),
    CHAT("Coach Ashes", "💬")
}

@HiltViewModel
class CoachViewModel @Inject constructor(
    private val programRepo: ProgramRepository,
    private val workoutRepo: WorkoutRepository,
    private val nutritionRepo: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachProgramUiState())
    val uiState: StateFlow<CoachProgramUiState> = _uiState.asStateFlow()

    init {
        // Welcome message in Coach chat
        val initialMessage = ChatMessage(
            isFromCoach = true,
            content = "Salut ! Je suis ton **Coach IA Ashes** propulsé par Gemini. Pose-moi n'importe quelle question sur tes séances, ta nutrition, tes exercices ou tes objectifs !",
            quickActions = listOf("Générer mon programme", "Analyser ma nutrition", "Conseils récupération", "Idée de repas post-séance")
        )
        _uiState.update { it.copy(chatMessages = listOf(initialMessage)) }

        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.minusDays(6)

            combine(
                programRepo.observeCurrentProgram(),
                programRepo.observeCoachProfile(),
                workoutRepo.getSessionsInRange(weekStart, today),
                nutritionRepo.getMealsForDate(today)
            ) { program, profile, workouts, _ ->
                val recentMeals = mutableListOf<MealEntry>()
                for (i in 0..6) {
                    val dayDate = today.minusDays(i.toLong())
                    val dayMeals = nutritionRepo.getMealsForDate(dayDate).firstOrNull() ?: emptyList()
                    recentMeals.addAll(dayMeals)
                }

                val analysis = AiCoachAdvisor.generateAnalysis(profile, program, recentMeals, workouts)

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        program = program ?: AiProgramGenerator.generateTailoredProgram(profile),
                        profile = profile,
                        report = analysis,
                        formGoal = profile.goal,
                        formPreferredSports = profile.preferredSports.joinToString(", "),
                        formDislikedSports = profile.dislikedSportsOrExercises.joinToString(", "),
                        formHealthIssues = profile.healthIssuesAndInjuries.joinToString(", "),
                        formDaysPerWeek = profile.daysPerWeek,
                        formDurationMin = profile.sessionDurationMin,
                        formLevel = profile.experienceLevel
                    )
                }
            }.collect()
        }
    }

    fun selectTab(tab: CoachTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun openGeneratorDialog() = _uiState.update { it.copy(showGeneratorDialog = true) }
    fun closeGeneratorDialog() = _uiState.update { it.copy(showGeneratorDialog = false) }

    // Generator Form Setters
    fun setFormGoal(goal: FitnessGoal) = _uiState.update { it.copy(formGoal = goal) }
    fun setFormPreferredSports(v: String) = _uiState.update { it.copy(formPreferredSports = v) }
    fun setFormDislikedSports(v: String) = _uiState.update { it.copy(formDislikedSports = v) }
    fun setFormHealthIssues(v: String) = _uiState.update { it.copy(formHealthIssues = v) }
    fun setFormDaysPerWeek(days: Int) = _uiState.update { it.copy(formDaysPerWeek = days) }
    fun setFormDurationMin(min: Int) = _uiState.update { it.copy(formDurationMin = min) }
    fun setFormLevel(level: ExperienceLevel) = _uiState.update { it.copy(formLevel = level) }

    fun generateAndApplyProgram() {
        val state = _uiState.value
        _uiState.update { it.copy(isGeneratingProgram = true, showGeneratorDialog = false) }

        viewModelScope.launch {
            val preferredList = state.formPreferredSports.split(",")
                .map { it.trim() }.filter { it.isNotBlank() }
                .ifEmpty { listOf("Musculation", "Tir à l'arc", "Marche / Course") }

            val dislikedList = state.formDislikedSports.split(",")
                .map { it.trim() }.filter { it.isNotBlank() }

            val healthList = state.formHealthIssues.split(",")
                .map { it.trim() }.filter { it.isNotBlank() }

            val newProfile = state.profile.copy(
                goal = state.formGoal,
                preferredSports = preferredList,
                dislikedSportsOrExercises = dislikedList,
                healthIssuesAndInjuries = healthList,
                daysPerWeek = state.formDaysPerWeek,
                sessionDurationMin = state.formDurationMin,
                experienceLevel = state.formLevel
            )

            val newProgram = AiProgramGenerator.generateTailoredProgram(newProfile)

            programRepo.saveCoachProfile(newProfile)
            programRepo.saveProgram(newProgram)

            val coachMsg = ChatMessage(
                isFromCoach = true,
                content = "✨ **Nouveau programme Ashes généré avec succès !**\n\n" +
                        "• **Objectif** : ${newProfile.goal.title}\n" +
                        "• **Fréquence** : ${newProfile.daysPerWeek} jours / semaine (~${newProfile.sessionDurationMin} min)\n" +
                        "• **Sports ciblés** : ${preferredList.joinToString(", ")}\n" +
                        if (healthList.isNotEmpty()) "• **Adaptations santé / blessures** : ${healthList.joinToString(", ")}\n" else "" +
                        "\nConsulte l'onglet **Programme** pour voir ton planning !",
                quickActions = listOf("Analyser ma semaine", "Conseils nutrition", "Que faire aujourd'hui ?")
            )

            _uiState.update {
                it.copy(
                    isGeneratingProgram = false,
                    program = newProgram,
                    profile = newProfile,
                    chatMessages = it.chatMessages + coachMsg,
                    selectedTab = CoachTab.PROGRAM
                )
            }
        }
    }

    fun setChatInput(v: String) = _uiState.update { it.copy(chatInput = v) }

    fun sendChatMessage(messageText: String? = null) {
        val text = (messageText ?: _uiState.value.chatInput).trim()
        if (text.isBlank()) return

        val userMsg = ChatMessage(isFromCoach = false, content = text)
        val currentState = _uiState.value

        // Intercept action prompts
        if (text.contains("générer mon programme", ignoreCase = true) || text.contains("nouveau programme", ignoreCase = true)) {
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + userMsg,
                    chatInput = "",
                    showGeneratorDialog = true
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                chatInput = "",
                isThinking = true
            )
        }

        viewModelScope.launch {
            val report = currentState.report
            val profile = currentState.profile

            val systemContext = buildString {
                appendLine("Profil : Poids ${profile.weightKg ?: 70f}kg, Taille ${profile.heightCm ?: 175}cm, Objectif : ${profile.goal.title}")
                appendLine("Sports pratiqués : ${profile.preferredSports.joinToString(", ")}")
                if (profile.healthIssuesAndInjuries.isNotEmpty()) {
                    appendLine("Blessures / Limitations : ${profile.healthIssuesAndInjuries.joinToString(", ")}")
                }
                if (report != null) {
                    appendLine("Cible calorique : ${report.targetDailyCalories} kcal | Apport moyen actuel : ${report.actualAvgCalories} kcal")
                    appendLine("Cible protéines : ${report.targetDailyProteinG.toInt()}g | Apport moyen actuel : ${report.actualAvgProteinG.toInt()}g")
                    appendLine("Séances complétées cette semaine : ${report.completedWorkoutsThisWeek}/${report.plannedWorkoutsThisWeek}")
                    appendLine("Score d'alignement : ${report.overallScore}/100")
                }
            }

            // Real Gemini LLM Call
            val responseText = try {
                GeminiAiService.ask(text, systemContext)
            } catch (e: Exception) {
                // Fallback to local rule engine if network / key problem
                if (report != null) {
                    AiCoachAdvisor.answerUserQuery(text, profile, report).content
                } else {
                    "Désolé, je rencontre une difficulté réseau momentanée. Réessaie !"
                }
            }

            val coachReply = ChatMessage(
                isFromCoach = true,
                content = responseText,
                quickActions = listOf("Idée d'exercice", "Analyser mes macros", "Ajuster ma séance")
            )

            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + coachReply,
                    isThinking = false
                )
            }
        }
    }
}

