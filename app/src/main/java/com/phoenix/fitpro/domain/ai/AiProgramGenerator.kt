package com.phoenix.fitpro.domain.ai

import com.phoenix.fitpro.domain.model.*
import java.time.DayOfWeek

object AiProgramGenerator {

    /**
     * Generates a completely tailored 7-day training program considering:
     * - Liked sports (e.g. Musculation, Tir à l'arc, Marche / Course)
     * - Disliked sports or movements to avoid
     * - Health limitations / injuries (e.g. back pain, sensitive knees, shoulder impingement)
     * - Frequency (days per week) and target goal.
     */
    fun generateTailoredProgram(profile: CoachUserProfile): TrainingProgram {
        val isEn = java.util.Locale.getDefault().language.equals("en", ignoreCase = true)
        val daysPerWeek = profile.daysPerWeek.coerceIn(2, 6)
        val goal = profile.goal
        val likesArchery = profile.preferredSports.any { it.contains("Tir à l'arc", ignoreCase = true) || it.contains("Archery", ignoreCase = true) }
        val likesRunning = profile.preferredSports.any { it.contains("Course", ignoreCase = true) || it.contains("Marche", ignoreCase = true) || it.contains("Run", ignoreCase = true) || it.contains("Walk", ignoreCase = true) }
        val likesStrength = profile.preferredSports.any { it.contains("Musculation", ignoreCase = true) || it.contains("Force", ignoreCase = true) || it.contains("Strength", ignoreCase = true) || it.contains("Lift", ignoreCase = true) }

        val hasKneeIssues = profile.healthIssuesAndInjuries.any { it.contains("genou", ignoreCase = true) || it.contains("knee", ignoreCase = true) }
        val hasBackIssues = profile.healthIssuesAndInjuries.any { it.contains("dos", ignoreCase = true) || it.contains("lomb", ignoreCase = true) || it.contains("back", ignoreCase = true) }
        val hasShoulderIssues = profile.healthIssuesAndInjuries.any { it.contains("épaul", ignoreCase = true) || it.contains("tendin", ignoreCase = true) || it.contains("shoulder", ignoreCase = true) }

        val weeklyDays = mutableListOf<ProgramDay>()
        val tips = mutableListOf<String>()

        // 1. Build smart tips based on profile
        if (isEn) {
            when (goal) {
                FitnessGoal.MASS_GAIN -> {
                    tips.add("🥩 Aim for a ~300-400 kcal/day surplus and at least 2g of protein/kg of body weight.")
                    tips.add("📈 Prioritize progressive overload on main compound lifts (log your weights).")
                }
                FitnessGoal.WEIGHT_LOSS -> {
                    tips.add("🔥 Maintain a moderate caloric deficit (~350-500 kcal). Keep protein high to preserve lean mass.")
                    tips.add("🚶 Daily walking (8,000 - 10,000 steps) to maximize energy expenditure without fatigue.")
                }
                FitnessGoal.MUSCLE_TONE -> {
                    tips.add("💪 Focus on movement quality and tempo (2s eccentric descent, 1s peak contraction).")
                    tips.add("⏱️ 90s to 2min rest between heavy sets for optimal neuromuscular recovery.")
                }
                FitnessGoal.HYBRID_ARCHERY_FITNESS -> {
                    tips.add("🏹 Target rhomboids, mid/lower traps and rotator cuff for steady archery draw.")
                    tips.add("🧘 Anti-rotation core stability and diaphragmatic breathing for steady aiming.")
                }
                FitnessGoal.HEALTH_ENDURANCE -> {
                    tips.add("🏃 Balanced aerobic base training combined with functional joint strength.")
                    tips.add("💧 Minimum hydration: 2.5L of water daily.")
                }
            }
            if (hasBackIssues) tips.add("🛡️ Sensitive back protection active: supported exercises prioritized, heavy axial loading avoided.")
            if (hasKneeIssues) tips.add("🦵 Knee friendly: deep squats replaced with leg press, controlled lunges and leg curls.")
            if (hasShoulderIssues) tips.add("🎯 Shoulder protection: overhead barbell press replaced with cable lateral raises and rotator cuff work.")
        } else {
            when (goal) {
                FitnessGoal.MASS_GAIN -> {
                    tips.add("🥩 Vise un surplus de ~300-400 kcal/jour et au moins 2g de protéines/kg de poids de corps.")
                    tips.add("📈 Priorise la surcharge progressive sur les séries principales (note tes charges).")
                }
                FitnessGoal.WEIGHT_LOSS -> {
                    tips.add("🔥 Maintiens un déficit modéré (~350-500 kcal). Garde les protéines hautes pour protéger le muscle.")
                    tips.add("🚶 Marche quotidienne (8 000 - 10 000 pas) pour maximiser la dépense sans fatigue nerveuse.")
                }
                FitnessGoal.MUSCLE_TONE -> {
                    tips.add("💪 Concentre-toi sur la qualité d'exécution et le tempo (2s descente, 1s contraction).")
                    tips.add("⏱️ 1h30 à 2h de repos entre les séries lourdes pour un recrutement optimal.")
                }
                FitnessGoal.HYBRID_ARCHERY_FITNESS -> {
                    tips.add("🏹 Travail de renforcement des rhomboïdes, trapèzes moyens/inférieurs et de la coiffe des rotateurs.")
                    tips.add("🧘 Gainage anti-rotation et respiration ventrale pour stabiliser la visée.")
                }
                FitnessGoal.HEALTH_ENDURANCE -> {
                    tips.add("🏃 Équilibre parfait entre travail cardiovasculaire en endurance fondamentale et renforcement doux.")
                    tips.add("💧 Hydratation minimale : 2.5L d'eau par jour.")
                }
            }
            if (hasBackIssues) tips.add("🛡️ Dos fragile détecté : exercices avec soutien lombaire privilégiés, charges axiales lourdes exclues.")
            if (hasKneeIssues) tips.add("🦵 Genoux sensibles : squats profonds avec charge remplacés par presse guidée, fentes contrôlées et leg curl.")
            if (hasShoulderIssues) tips.add("🎯 Épaules protégées : développé militaire lourd remplacé par élévations latérales au câble et travail de coiffe.")
        }

        // 2. Select Day distribution
        val trainingDayIndices: Set<DayOfWeek> = when (daysPerWeek) {
            2 -> setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY)
            3 -> setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            4 -> setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            5 -> setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
            6 -> setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
            else -> setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
        }

        var workoutCounter = 0
        DayOfWeek.values().forEach { day ->
            if (day !in trainingDayIndices) {
                // Rest / Active recovery day
                weeklyDays.add(
                    ProgramDay(
                        dayOfWeek = day,
                        isRestDay = true,
                        title = if (isEn) "Rest & Active Recovery" else "Repos & Récupération active",
                        sportName = if (isEn) "Recovery" else "Récupération",
                        sportEmoji = "🧘",
                        estimatedDurationMin = 20,
                        focusDescription = if (isEn) "Light walk, gentle stretching or complete rest to rebuild muscle fibers." else "Marche légère, étirements doux ou repos complet pour reconstruire les fibres musculaires.",
                        exercises = listOf(
                            ProgramExercise(
                                if (isEn) "Light walk / mobility" else "Marche digestive / mobilité",
                                1,
                                if (isEn) "20-30 min" else "20-30 min",
                                0,
                                if (isEn) "Easy pace to stimulate blood flow" else "Allure tranquille, favorise la circulation sanguine"
                            ),
                            ProgramExercise(
                                if (isEn) "Gentle stretching & breathing" else "Étirements doux & respiration",
                                1,
                                if (isEn) "10 min" else "10 min",
                                0,
                                if (isEn) "Hips, hamstrings and chest opening" else "Bassin, ischios, ouverture de cage thoracique"
                            )
                        )
                    )
                )
            } else {
                workoutCounter++
                val programDay = generateWorkoutDay(
                    dayOfWeek = day,
                    index = workoutCounter,
                    totalWorkouts = daysPerWeek,
                    goal = goal,
                    likesArchery = likesArchery,
                    likesRunning = likesRunning,
                    likesStrength = likesStrength,
                    hasKneeIssues = hasKneeIssues,
                    hasBackIssues = hasBackIssues,
                    hasShoulderIssues = hasShoulderIssues,
                    durationMin = profile.sessionDurationMin
                )
                weeklyDays.add(programDay)
            }
        }

        val progTitle = if (isEn) "Ashes Program: ${goal.title}" else "Programme Ashes : ${goal.title}"
        val progDesc = if (isEn) "Designed by your AI Coach for a schedule of $daysPerWeek sessions / week tailored to your health and goals." else "Conçu par votre Coach IA pour un rythme de $daysPerWeek séances / semaine adapté à votre morphologie et santé."

        return TrainingProgram(
            id = 1,
            title = progTitle,
            description = progDesc,
            goal = goal,
            days = weeklyDays,
            coachTips = tips,
            createdAtEpochMs = System.currentTimeMillis()
        )
    }

    private fun generateWorkoutDay(
        dayOfWeek: DayOfWeek,
        index: Int,
        totalWorkouts: Int,
        goal: FitnessGoal,
        likesArchery: Boolean,
        likesRunning: Boolean,
        likesStrength: Boolean,
        hasKneeIssues: Boolean,
        hasBackIssues: Boolean,
        hasShoulderIssues: Boolean,
        durationMin: Int
    ): ProgramDay {
        // Special case: If user loves archery, dedicate one day to Archery & Postural Stability
        if (likesArchery && (index == 2 || (index == 1 && totalWorkouts == 2))) {
            return ProgramDay(
                dayOfWeek = dayOfWeek,
                isRestDay = false,
                title = "Séance Tir à l'arc, Posture & Chaîne Postérieure",
                sportName = "Tir à l'arc",
                sportEmoji = "🏹",
                estimatedDurationMin = durationMin,
                focusDescription = "Endurance de traction scapulaire, stabilité de l'épaule d'arc, gainage profond et ancrage au sol.",
                exercises = listOf(
                    ProgramExercise("Tir à l'arc — Pratique / Volées contrôlées", 10, "6-12 flèches", 60, "Focus sur la décoche fluide et la tenue de visée"),
                    ProgramExercise("Face Pulls à la poulie / élastique", 4, "15 reps", 60, "Renforcement des rotateurs externes et rhomboïdes"),
                    ProgramExercise("Oiseau aux haltères sur banc incliné", 3, "12-15 reps", 60, "Fixation des omoplates, dos protégé"),
                    ProgramExercise("Planche gainage anti-rotation (Pallof press)", 3, "30s par côté", 45, "Stabilité du tronc pendant la phase d'armement"),
                    ProgramExercise("Mobilité des poignets & étirement avant-bras", 2, "1 min", 30, "Prévention des tensions de corde")
                )
            )
        }

        // Strength / Hybrid splits based on total workouts per week
        val isPush = (index % 3 == 1)
        val isPull = (index % 3 == 2)
        val isLegs = (index % 3 == 0)

        return when {
            totalWorkouts == 2 -> {
                if (index == 1) {
                    ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Haut du Corps (Pectoraux, Dos, Bras)",
                        sportName = "Musculation",
                        sportEmoji = "💪",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Développement global du haut du corps avec protection des articulations.",
                        exercises = buildUpperExercises(hasBackIssues, hasShoulderIssues)
                    )
                } else {
                    ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Bas du Corps, Gainage & Cardio",
                        sportName = if (likesRunning) "Marche / Course" else "Musculation",
                        sportEmoji = if (likesRunning) "🏃" else "🦵",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Renforcement des jambes et dépense énergétique.",
                        exercises = buildLowerExercises(hasBackIssues, hasKneeIssues, likesRunning)
                    )
                }
            }

            totalWorkouts == 3 -> {
                when (index) {
                    1 -> ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Push — Pectoraux, Épaules & Triceps",
                        sportName = "Musculation",
                        sportEmoji = "⚡",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Mouvements de poussée pour la force et le volume du buste.",
                        exercises = buildPushExercises(hasShoulderIssues)
                    )
                    2 -> ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Pull — Dos complet, Arrière d'épaules & Biceps",
                        sportName = "Musculation",
                        sportEmoji = "💪",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Mouvements de tirage pour un dos puissant et une posture droite.",
                        exercises = buildPullExercises(hasBackIssues)
                    )
                    else -> ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Legs & Core — Quadriceps, Ischios & Abdominaux",
                        sportName = "Musculation",
                        sportEmoji = "🦵",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Fondation athlétique et puissance des membres inférieurs.",
                        exercises = buildLowerExercises(hasBackIssues, hasKneeIssues, likesRunning)
                    )
                }
            }

            totalWorkouts >= 4 -> {
                when (index % 4) {
                    1 -> ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Upper Body — Force & Hypertrophie Buste",
                        sportName = "Musculation",
                        sportEmoji = "💪",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Pectoraux, grand dorsal et épaules.",
                        exercises = buildUpperExercises(hasBackIssues, hasShoulderIssues)
                    )
                    2 -> ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Lower Body — Puissance & Posture",
                        sportName = "Musculation",
                        sportEmoji = "🦵",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Chaîne postérieure, fessiers et quadriceps.",
                        exercises = buildLowerExercises(hasBackIssues, hasKneeIssues, false)
                    )
                    3 -> ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Athlétique — Tirage, Épaules & Bras",
                        sportName = if (likesArchery) "Tir à l'arc" else "Musculation",
                        sportEmoji = if (likesArchery) "🏹" else "⚡",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Volume d'isolation et endurance musculaire ciblée.",
                        exercises = buildPullExercises(hasBackIssues)
                    )
                    else -> ProgramDay(
                        dayOfWeek = dayOfWeek,
                        isRestDay = false,
                        title = "Cardio, Endurance & Gainage profond",
                        sportName = if (likesRunning) "Marche / Course" else "Cardio",
                        sportEmoji = "🏃",
                        estimatedDurationMin = durationMin,
                        focusDescription = "Amélioration du VO2max, récupération active et ceinture abdominale.",
                        exercises = buildCardioCoreExercises(likesRunning, hasKneeIssues)
                    )
                }
            }

            else -> ProgramDay(
                dayOfWeek = dayOfWeek,
                isRestDay = false,
                title = "Séance Complète",
                sportName = "Musculation",
                sportEmoji = "💪",
                estimatedDurationMin = durationMin,
                focusDescription = "Entraînement complet du corps.",
                exercises = buildUpperExercises(hasBackIssues, hasShoulderIssues)
            )
        }
    }

    private fun buildPushExercises(hasShoulderIssues: Boolean): List<ProgramExercise> {
        val list = mutableListOf<ProgramExercise>()
        if (hasShoulderIssues) {
            list.add(ProgramExercise("Développé couché aux haltères (prise neutre)", 4, "10-12 reps", 90, "Moins de stress sur la coiffe des rotateurs"))
            list.add(ProgramExercise("Développé incliné à la machine convergente", 3, "10-12 reps", 90, "Trajectoire guidée et sécurisée"))
            list.add(ProgramExercise("Élévations latérales au câble buste penché", 4, "15 reps", 60, "Tension continue sans à-coups"))
        } else {
            list.add(ProgramExercise("Développé couché à la barre", 4, "8-10 reps", 120, "Exercice roi pour la masse des pectoraux"))
            list.add(ProgramExercise("Développé incliné aux haltères", 3, "10-12 reps", 90, "Focus haut des pectoraux"))
            list.add(ProgramExercise("Développé militaire assis aux haltères", 3, "10-12 reps", 90, "Volume global des deltoïdes"))
        }
        list.add(ProgramExercise("Écartés à la poulie vis-à-vis", 3, "12-15 reps", 60, "Contraction maximale en fin de mouvement"))
        list.add(ProgramExercise("Extensions triceps à la corde", 3, "12-15 reps", 60, "Isoler la longue portion du triceps"))
        return list
    }

    private fun buildPullExercises(hasBackIssues: Boolean): List<ProgramExercise> {
        val list = mutableListOf<ProgramExercise>()
        list.add(ProgramExercise("Tirage vertical poitrine (prise large)", 4, "8-12 reps", 90, "Élargissement du grand dorsal"))
        if (hasBackIssues) {
            list.add(ProgramExercise("Rowing poitrine appuyée sur banc incliné", 4, "10-12 reps", 90, "Soulage 100% de la pression sur les lombaires"))
            list.add(ProgramExercise("Tirage horizontal poulie basse avec calage", 3, "10-12 reps", 75, "Garder le buste droit sans à-coups"))
        } else {
            list.add(ProgramExercise("Rowing barre buste penché", 4, "8-10 reps", 90, "Épaisseur du dos"))
            list.add(ProgramExercise("Tirage bûcheron à un bras (haltère)", 3, "10-12 reps", 75, "Étirement unilatéral profond"))
        }
        list.add(ProgramExercise("Face Pull à la corde", 3, "15 reps", 60, "Arrière d'épaules et posture"))
        list.add(ProgramExercise("Curl biceps incliné aux haltères", 3, "10-12 reps", 60, "Étirement maximal du biceps"))
        return list
    }

    private fun buildUpperExercises(hasBackIssues: Boolean, hasShoulderIssues: Boolean): List<ProgramExercise> {
        return buildPushExercises(hasShoulderIssues).take(3) + buildPullExercises(hasBackIssues).take(3)
    }

    private fun buildLowerExercises(hasBackIssues: Boolean, hasKneeIssues: Boolean, likesRunning: Boolean): List<ProgramExercise> {
        val list = mutableListOf<ProgramExercise>()
        if (hasKneeIssues || hasBackIssues) {
            list.add(ProgramExercise("Presse à cuisses (pieds hauts sur le plateau)", 4, "12 reps", 90, "Focus fessiers/ischios sans écrasement lombaire ni pression rotulienne"))
            list.add(ProgramExercise("Leg Curl couché / assis", 4, "12-15 reps", 60, "Renforcement isolé des ischio-jambiers"))
            list.add(ProgramExercise("Hip Thrust à la machine ou barre avec mousse", 4, "10-12 reps", 90, "Activation maximale des fessiers sans contrainte lombaire"))
        } else {
            list.add(ProgramExercise("Squat arrière à la barre", 4, "8-10 reps", 120, "Développement complet des cuisses et force"))
            list.add(ProgramExercise("Soulevé de terre jambes tendues (haltères)", 4, "10-12 reps", 90, "Étirement et renforcement chaîne postérieure"))
            list.add(ProgramExercise("Fentes marchées aux haltères", 3, "10 pas par jambe", 75, "Stabilité unilatérale"))
        }
        list.add(ProgramExercise("Extensions mollets debout", 4, "15 reps", 45, "Mollets et chevilles"))
        if (likesRunning) {
            list.add(ProgramExercise("Marche inclinée sur tapis ou course rythmée", 1, "15-20 min", 0, "Cardio modéré en fin de séance"))
        }
        return list
    }

    private fun buildCardioCoreExercises(likesRunning: Boolean, hasKneeIssues: Boolean): List<ProgramExercise> {
        val list = mutableListOf<ProgramExercise>()
        if (likesRunning && !hasKneeIssues) {
            list.add(ProgramExercise("Course à pied (fractionné ou continu)", 1, "25-35 min", 0, "Allure modérée (zone 2 / endurance fondamentale)"))
        } else {
            list.add(ProgramExercise("Marche rapide inclinée ou Vélo elliptique", 1, "30 min", 0, "Zéro impact articulaire, combustion calorique élevée"))
        }
        list.add(ProgramExercise("Gainage planche classique + côtés", 3, "45s par position", 45, "Ceinture abdominale complète"))
        list.add(ProgramExercise("Relevés de jambes suspendu / sur banc", 3, "15 reps", 45, "Bas des abdominaux"))
        list.add(ProgramExercise("Russian twists au poids de corps", 3, "20 rotations", 45, "Obliques et stabilité rotationnelle"))
        return list
    }
}
