package com.phoenix.fitpro.domain.ai

import com.phoenix.fitpro.domain.model.*
import java.time.LocalDate
import kotlin.math.roundToInt

data class CoachAnalysisReport(
    val overallScore: Int,                 // 0 to 100
    val scoreSummary: String,
    val targetDailyCalories: Int,
    val actualAvgCalories: Int,
    val targetDailyProteinG: Float,
    val actualAvgProteinG: Float,
    val completedWorkoutsThisWeek: Int,
    val plannedWorkoutsThisWeek: Int,
    val alerts: List<CoachAlert>,
    val nutritionAdvice: List<String>,
    val workoutAdvice: List<String>,
    val motivationalQuote: String
)

data class CoachAlert(
    val type: AlertType,
    val title: String,
    val message: String,
    val actionSuggestion: String
)

enum class AlertType {
    WARNING, SUCCESS, INFO
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isFromCoach: Boolean,
    val content: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val quickActions: List<String> = emptyList()
)

object AiCoachAdvisor {

    fun generateAnalysis(
        profile: CoachUserProfile,
        program: TrainingProgram?,
        recentMeals: List<MealEntry>,
        recentWorkouts: List<WorkoutSession>
    ): CoachAnalysisReport {
        // Base BMR estimation (Mifflin-St Jeor formula approximation)
        val weight = profile.weightKg.coerceAtLeast(40f)
        val height = profile.heightCm.coerceAtLeast(140)
        val bmr = (10 * weight + 6.25 * height - 5 * 25 + 5).toInt()
        val maintenanceCalories = (bmr * 1.45f).toInt() // with moderate activity
        val targetCalories = (maintenanceCalories + profile.goal.targetCaloriesDelta).coerceAtLeast(1400)
        val targetProtein = (weight * profile.goal.recommendedProteinPerKg)

        // Actual 7 days stats
        val totalCal = recentMeals.sumOf { it.totalCalories }
        val daysWithMeals = recentMeals.map { it.date }.distinct().size.coerceAtLeast(1)
        val actualAvgCal = totalCal / daysWithMeals

        val totalProtein = recentMeals.flatMap { it.foods }.mapNotNull { it.proteinG }.sum()
        val actualAvgProtein = totalProtein / daysWithMeals

        val completedWorkouts = recentWorkouts.size
        val plannedWorkouts = profile.daysPerWeek

        val alerts = mutableListOf<CoachAlert>()
        val nutritionAdvice = mutableListOf<String>()
        val workoutAdvice = mutableListOf<String>()

        val isEn = java.util.Locale.getDefault().language.equals("en", ignoreCase = true)

        // 1. Protein check
        if (actualAvgProtein < (targetProtein * 0.75f) && daysWithMeals > 0) {
            alerts.add(
                CoachAlert(
                    type = AlertType.WARNING,
                    title = if (isEn) "Protein intake too low" else "Apport en protéines trop bas",
                    message = if (isEn) "You average ${actualAvgProtein.roundToInt()}g/day while your goal (${profile.goal.title}) requires ~${targetProtein.roundToInt()}g." else "Tu consommes en moyenne ${actualAvgProtein.roundToInt()}g/jour alors que ton objectif (${profile.goal.title}) requiert ~${targetProtein.roundToInt()}g.",
                    actionSuggestion = if (isEn) "Add a lean protein source (chicken, eggs, 0% Greek yogurt, tofu) to your next meal." else "Ajoute une source de protéines maigres (poulet, œufs, fromage blanc 0%, tofu) à ton prochain repas."
                )
            )
        } else if (actualAvgProtein >= targetProtein && daysWithMeals > 0) {
            alerts.add(
                CoachAlert(
                    type = AlertType.SUCCESS,
                    title = if (isEn) "Great protein consistency" else "Excellente régularité protéique",
                    message = if (isEn) "You meet your daily targets (${actualAvgProtein.roundToInt()}g / ${targetProtein.roundToInt()}g). Perfect for muscle synthesis." else "Tu atteins tes quotas (${actualAvgProtein.roundToInt()}g / ${targetProtein.roundToInt()}g). C'est parfait pour la synthèse musculaire.",
                    actionSuggestion = if (isEn) "Keep spreading your protein across 3 to 4 meals daily." else "Continue à répartir tes protéines sur 3 à 4 prises par jour."
                )
            )
        }

        // 2. Caloric balance check
        if (profile.goal == FitnessGoal.MASS_GAIN) {
            if (actualAvgCal < maintenanceCalories && actualAvgCal > 0) {
                alerts.add(
                    CoachAlert(
                        type = AlertType.WARNING,
                        title = if (isEn) "Caloric deficit during Mass Gain" else "Déficit calorique incompatible avec Prise de masse",
                        message = if (isEn) "Your intake (${actualAvgCal} kcal) is below your maintenance (${maintenanceCalories} kcal)." else "Ton apport (${actualAvgCal} kcal) est sous ton maintien (${maintenanceCalories} kcal). Tu risques de stagner.",
                        actionSuggestion = if (isEn) "Increase complex carbs (rice, oats, sweet potatoes) and healthy fats." else "Augmente les glucides complexes (riz, avoine, patate douce) et les bonnes graisses (oléagineux, huile d'olive)."
                    )
                )
            }
        } else if (profile.goal == FitnessGoal.WEIGHT_LOSS) {
            if (actualAvgCal > (targetCalories + 300) && actualAvgCal > 0) {
                alerts.add(
                    CoachAlert(
                        type = AlertType.WARNING,
                        title = if (isEn) "Caloric surplus detected" else "Léger surplus calorique détecté",
                        message = if (isEn) "Your intake (${actualAvgCal} kcal) exceeds your cut target (${targetCalories} kcal)." else "Ton apport (${actualAvgCal} kcal) dépasse la cible (${targetCalories} kcal) pour ta perte de gras.",
                        actionSuggestion = if (isEn) "Focus on high-volume vegetables and cut down hidden sauces or sweet snacks." else "Privilégie les légumes volumineux et réduis les sauces ou snacks sucrés non comptabilisés."
                    )
                )
            }
        }

        // 3. Workout frequency & Health checks
        if (completedWorkouts == 0) {
            alerts.add(
                CoachAlert(
                    type = AlertType.INFO,
                    title = if (isEn) "Ready for your first session?" else "Prêt pour ta première séance ?",
                    message = if (isEn) "Your weekly program of $plannedWorkouts workouts is waiting." else "Ton programme hebdomadaire de $plannedWorkouts séances est prêt.",
                    actionSuggestion = if (isEn) "Lace up your shoes and crush your first workout today!" else "Enfile tes baskets et lance ta première séance aujourd'hui !"
                )
            )
        } else if (completedWorkouts >= plannedWorkouts) {
            alerts.add(
                CoachAlert(
                    type = AlertType.SUCCESS,
                    title = if (isEn) "Weekly workout goal crushed! 🔥" else "Objectif hebdomadaire atteint ! 🔥",
                    message = if (isEn) "You completed your $completedWorkouts scheduled workouts this week." else "Tu as validé tes $completedWorkouts séances de la semaine avec brio.",
                    actionSuggestion = if (isEn) "Prioritize quality sleep (7.5h - 8h) for maximum muscle recovery." else "Priorise le sommeil (7h30 - 8h) pour une super-compensation maximale."
                )
            )
        }

        // Health-specific advice
        if (profile.healthIssuesAndInjuries.isNotEmpty()) {
            val injuriesStr = profile.healthIssuesAndInjuries.joinToString(", ")
            workoutAdvice.add(if (isEn) "🛡️ Joint safety active: adapting to constraints ($injuriesStr)." else "🛡️ Sécurité articulaire active : adaptation aux contraintes ($injuriesStr).")
        }

        // Specific goal advice
        if (isEn) {
            when (profile.goal) {
                FitnessGoal.MASS_GAIN -> {
                    nutritionAdvice.add("Pre-workout meal: moderate carbs 90 mins before training.")
                    nutritionAdvice.add("Don't skip meals: 3 full meals + 1 or 2 dense snacks.")
                    workoutAdvice.add("Aim to add 1 rep or 1kg on your working sets each week (progressive overload).")
                }
                FitnessGoal.WEIGHT_LOSS -> {
                    nutritionAdvice.add("Drink at least 500ml water before each meal for optimal satiety.")
                    nutritionAdvice.add("Eat protein first during meals to stimulate satiety hormones.")
                    workoutAdvice.add("Keep weights heavy to signal the body to retain muscle.")
                }
                FitnessGoal.MUSCLE_TONE -> {
                    nutritionAdvice.add("Consistency over perfection: 80% whole unprocessed foods.")
                    workoutAdvice.add("Control the eccentric lowering phase on every rep.")
                }
                FitnessGoal.HYBRID_ARCHERY_FITNESS -> {
                    nutritionAdvice.add("Magnesium and Omega-3 recommended for focus and neuromuscular relaxation.")
                    workoutAdvice.add("Include 10 min of posture and chest mobility work after each session.")
                }
                FitnessGoal.HEALTH_ENDURANCE -> {
                    nutritionAdvice.add("Prioritize antioxidant-rich foods (berries, green tea, turmeric).")
                    workoutAdvice.add("Keep 80% of cardio at an easy conversational aerobic pace.")
                }
            }
        } else {
            when (profile.goal) {
                FitnessGoal.MASS_GAIN -> {
                    nutritionAdvice.add("Repas pré-entraînement : glucides à assimilation moyenne 1h30 avant la séance.")
                    nutritionAdvice.add("Ne saute aucun repas : 3 repas principaux + 1 ou 2 collations riches.")
                    workoutAdvice.add("Cherche à ajouter 1 répétition ou 1kg sur tes séries chaque semaine (surcharge progressive).")
                }
                FitnessGoal.WEIGHT_LOSS -> {
                    nutritionAdvice.add("Bois au moins 500ml d'eau avant chaque repas pour optimiser la satiété.")
                    nutritionAdvice.add("Mange tes protéines en début de repas pour stimuler la leptine (hormone de satiété).")
                    workoutAdvice.add("Maintiens tes charges lourdes pour envoyer le signal au corps de garder le muscle.")
                }
                FitnessGoal.MUSCLE_TONE -> {
                    nutritionAdvice.add("Vise la régularité avant la perfection : 80% d'aliments bruts non transformés.")
                    workoutAdvice.add("Contrôle la phase excentrique (descente) sur chaque exercice pour maximiser le recrutement.")
                }
                FitnessGoal.HYBRID_ARCHERY_FITNESS -> {
                    nutritionAdvice.add("Magnésium et oméga-3 recommandés pour la concentration et la détente neuromusculaire.")
                    workoutAdvice.add("Intègre 10 min de travail de posture et d'étirements du psoas après chaque session de tir.")
                }
                FitnessGoal.HEALTH_ENDURANCE -> {
                    nutritionAdvice.add("Privilégie les aliments riches en antioxydants (fruits rouges, thé vert, curcuma).")
                    workoutAdvice.add("Garde 80% de ton volume cardio en aisance respiratoire (capable de parler).")
                }
            }
        }

        // Calculate a holistic score
        var score = 70
        if (completedWorkouts >= (plannedWorkouts - 1)) score += 15
        if (actualAvgProtein >= (targetProtein * 0.8f)) score += 15
        if (alerts.any { it.type == AlertType.WARNING }) score -= 15
        score = score.coerceIn(30, 100)

        val summary = when {
            score >= 85 -> "Excellente progression ! Tu es parfaitement aligné avec ton objectif."
            score >= 65 -> "Bon rythme global. Quelques ajustements simples boosteront tes résultats."
            else -> "Phase de lancement. Suis les recommandations ci-dessous pour accélérer !"
        }

        return CoachAnalysisReport(
            overallScore = score,
            scoreSummary = summary,
            targetDailyCalories = targetCalories,
            actualAvgCalories = actualAvgCal,
            targetDailyProteinG = targetProtein,
            actualAvgProteinG = actualAvgProtein,
            completedWorkoutsThisWeek = completedWorkouts,
            plannedWorkoutsThisWeek = plannedWorkouts,
            alerts = alerts,
            nutritionAdvice = nutritionAdvice,
            workoutAdvice = workoutAdvice,
            motivationalQuote = "« Les champions ne naissent pas dans les gymnases. Ils naissent de quelque chose qu'ils ont au plus profond d'eux : un désir, un rêve, une vision. »"
        )
    }

    /**
     * Interactive Coach Chat response generator (Bilingual FR / EN)
     */
    fun answerUserQuery(
        userMessage: String,
        profile: CoachUserProfile,
        report: CoachAnalysisReport
    ): ChatMessage {
        val q = userMessage.lowercase().trim()
        val isEn = java.util.Locale.getDefault().language.equals("en", ignoreCase = true) ||
                q.contains("hello") || q.contains("hi") || q.contains("eat") || q.contains("workout") || q.contains("injury")

        val reply = if (isEn) {
            when {
                q.contains("hello") || q.contains("hi") || q.contains("hey") || q.contains("bonjour") || q.contains("salut") -> {
                    "Hello! I am your **Ashes AI Coach**. How can I help you today? Would you like to review your program, optimize nutrition, or adapt exercises to an injury?"
                }
                q.contains("eat") || q.contains("meal") || q.contains("food") || q.contains("nutrition") || q.contains("manger") || q.contains("repas") -> {
                    val goalTitle = profile.goal.title
                    "For your **$goalTitle** objective:\n\n" +
                            "• **Daily Target Calories**: ~${report.targetDailyCalories} kcal\n" +
                            "• **Target Protein**: ~${report.targetDailyProteinG.roundToInt()}g / day (${profile.goal.recommendedProteinPerKg}g/kg)\n\n" +
                            "💡 **Optimized Meal Idea**: 150g grilled chicken breast or salmon + 80g (dry) basmati rice / sweet potato + 200g broccoli or green beans with a drizzle of extra virgin olive oil."
                }
                q.contains("program") || q.contains("workout") || q.contains("routine") || q.contains("exercise") || q.contains("programme") || q.contains("séance") -> {
                    "Your current program is tailored for **${profile.daysPerWeek} sessions per week** focusing on (${profile.preferredSports.joinToString(", ")}).\n\n" +
                            "✅ You completed **${report.completedWorkoutsThisWeek} / ${report.plannedWorkoutsThisWeek} sessions** this week.\n\n" +
                            "Would you like me to regenerate a fresh weekly routine or adjust specific days?"
                }
                q.contains("injury") || q.contains("pain") || q.contains("hurt") || q.contains("back") || q.contains("knee") || q.contains("shoulder") || q.contains("blessure") || q.contains("mal") -> {
                    "I noted your health restrictions (${profile.healthIssuesAndInjuries.ifEmpty { listOf("no restrictions saved") }.joinToString(", ")}).\n\n" +
                            "🛡️ **Ashes Safety Rules**:\n" +
                            "1. Never train through acute sharp joint pain.\n" +
                            "2. Replace heavy axial spinal loads with supported machine movements (leg press, hip thrust, chest-supported rows).\n" +
                            "3. 8-10 mins dynamic warm-up with resistance bands before every session."
                }
                q.contains("archery") || q.contains("bow") || q.contains("tir à l'arc") -> {
                    "🏹 Archery requires optimal **scapular stability**, **anti-rotation core strength** and **steady breath control**.\n\n" +
                            "Key exercises built into your program:\n" +
                            "• Cable Face Pulls & DB Rear Delt Flyes (rotator cuff reinforcement)\n" +
                            "• Side Plank & Pallof Press (torso stability under string draw)\n" +
                            "• Horizontal Cable Rows with 2s peak contraction hold."
                }
                else -> {
                    "As your Ashes AI Coach, I'm here to guide you. Your overall alignment score is **${report.overallScore}/100**.\n\n" +
                            "Feel free to ask for meal ideas, exercise substitutions, or tap 'Generate My Program' to build a custom routine!"
                }
            }
        } else {
            when {
                q.contains("bonjour") || q.contains("salut") || q.contains("hey") || q.contains("hello") || q.contains("hi") -> {
                    "Bonjour ! Je suis ton **Coach Ashes**. Comment puis-je t'aider aujourd'hui ? Tu veux faire le point sur ton programme, ton alimentation ou tes blessures ?"
                }
                q.contains("manger") || q.contains("repas") || q.contains("nutrition") || q.contains("faim") || q.contains("eat") || q.contains("food") -> {
                    val goalText = profile.goal.title
                    "Pour ton objectif de **$goalText** :\n\n" +
                            "• **Cible calorique quotidienne** : ~${report.targetDailyCalories} kcal\n" +
                            "• **Protéines recommandées** : ~${report.targetDailyProteinG.roundToInt()}g / jour (${profile.goal.recommendedProteinPerKg}g/kg)\n\n" +
                            "💡 **Idée de repas optimisé** : 150g de filet de poulet ou saumon grillé + 80g (cru) de riz basmati ou quinoa + 200g de haricots verts ou brocolis avec 1 filet d'huile d'olive."
                }
                q.contains("programme") || q.contains("séance") || q.contains("sport") || q.contains("exercice") || q.contains("workout") -> {
                    "Ton programme actuel est configuré pour **${profile.daysPerWeek} séances par semaine** avec focus sur tes sports préférés (${profile.preferredSports.joinToString(", ")}).\n\n" +
                            "✅ Tu as validé **${report.completedWorkoutsThisWeek} / ${report.plannedWorkoutsThisWeek} séances** cette semaine.\n\n" +
                            "Souhaites-tu régénérer un programme complet ou ajuster un jour précis ?"
                }
                q.contains("blessure") || q.contains("mal") || q.contains("douleur") || q.contains("dos") || q.contains("genou") || q.contains("épaule") || q.contains("injury") -> {
                    "J'ai bien noté tes limitations (${profile.healthIssuesAndInjuries.ifEmpty { listOf("aucune restriction enregistrée") }.joinToString(", ")}).\n\n" +
                            "🛡️ **Règles d'or appliquées par le Coach Ashes** :\n" +
                            "1. Ne jamais forcer sur une douleur articulaire aiguë.\n" +
                            "2. Remplacer les charges axiales et squats lourds par des exercices guidés (presse, hip thrust, fentes).\n" +
                            "3. Échauffement systématique de 8-10 minutes avec élastiques."
                }
                q.contains("tir à l'arc") || q.contains("arc") || q.contains("flèche") || q.contains("archery") -> {
                    "🏹 Le tir à l'arc requiert un équilibre parfait entre **stabilité scapulaire** (dos), **gainage anti-rotation** et **calme respiratoire**.\n\n" +
                            "Les exercices clés intégrés à ton programme :\n" +
                            "• Face Pulls & Oiseau haltères (renforcement de la coiffe des rotateurs)\n" +
                            "• Gainage latéral (stabilité de la colonne sous tension)\n" +
                            "• Tirage horizontal avec pause isométrique 2s en contraction."
                }
                else -> {
                    "En tant que Coach Ashes, je suis là pour t'accompagner. Selon tes données actuelles, ton score de cohérence est de **${report.overallScore}/100**.\n\n" +
                            "N'hésite pas à me demander des conseils sur ton alimentation, des ajustements d'exercices ou une nouvelle génération de programme !"
                }
            }
        }

        val quickActions = if (isEn) {
            when {
                q.contains("eat") || q.contains("food") || q.contains("nutrition") -> listOf("What to eat tonight?", "Weekly review", "Adjust my program")
                q.contains("program") || q.contains("workout") -> listOf("Regenerate my program", "Recovery tips", "Check my macros")
                else -> listOf("What should I eat today?", "Review my week", "Archery & Strength tips")
            }
        } else {
            when {
                q.contains("manger") || q.contains("nutrition") -> listOf("Que manger ce soir ?", "Analyser ma semaine", "Ajuster mon programme")
                q.contains("programme") -> listOf("Régénérer mon programme", "Conseils récupération", "Analyser mes macros")
                else -> listOf("Que manger aujourd'hui ?", "Analyser ma semaine", "Conseils Tir à l'arc & Muscu")
            }
        }

        return ChatMessage(
            isFromCoach = true,
            content = reply,
            quickActions = quickActions
        )
    }
}
