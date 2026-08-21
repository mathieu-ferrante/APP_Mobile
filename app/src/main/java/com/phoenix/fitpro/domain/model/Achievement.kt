package com.phoenix.fitpro.domain.model

import java.time.LocalDate

data class Achievement(
    val id: String,
    val titleFr: String,
    val titleEn: String,
    val descriptionFr: String,
    val descriptionEn: String,
    val iconEmoji: String,
    val xpReward: Int,
    val tier: AchievementTier = AchievementTier.BRONZE,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDate? = null
)

enum class AchievementTier { BRONZE, SILVER, GOLD }

/** All achievement definitions in the app */
object AchievementDefinitions {
    val all = listOf(
        // ── First steps ──────────────────────────────────────────────────────
        Achievement(
            id = "first_workout",
            titleFr = "Premier Pas",
            titleEn = "First Step",
            descriptionFr = "Complète ta première séance de sport",
            descriptionEn = "Complete your first workout",
            iconEmoji = "👟",
            xpReward = 50,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "first_meal",
            titleFr = "À Table !",
            titleEn = "First Meal",
            descriptionFr = "Enregistre ton premier repas",
            descriptionEn = "Log your first meal",
            iconEmoji = "🍽️",
            xpReward = 30,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "first_sport_created",
            titleFr = "Créateur",
            titleEn = "Creator",
            descriptionFr = "Crée ton premier sport personnalisé",
            descriptionEn = "Create your first custom sport",
            iconEmoji = "⚡",
            xpReward = 40,
            tier = AchievementTier.BRONZE
        ),

        // ── Streak achievements ───────────────────────────────────────────────
        Achievement(
            id = "streak_3",
            titleFr = "Sur la Lancée",
            titleEn = "On a Roll",
            descriptionFr = "3 jours consécutifs d'activité",
            descriptionEn = "3 consecutive days of activity",
            iconEmoji = "🔥",
            xpReward = 100,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "streak_7",
            titleFr = "Semaine de Feu",
            titleEn = "Fire Week",
            descriptionFr = "7 jours consécutifs d'activité",
            descriptionEn = "7 consecutive days of activity",
            iconEmoji = "🔥🔥",
            xpReward = 250,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "streak_30",
            titleFr = "Mois Légendaire",
            titleEn = "Legendary Month",
            descriptionFr = "30 jours consécutifs d'activité",
            descriptionEn = "30 consecutive days of activity",
            iconEmoji = "🏆",
            xpReward = 1000,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "streak_100",
            titleFr = "Centurion",
            titleEn = "Centurion",
            descriptionFr = "100 jours consécutifs — tu es une machine !",
            descriptionEn = "100 consecutive days — you're a machine!",
            iconEmoji = "💎",
            xpReward = 5000,
            tier = AchievementTier.GOLD
        ),

        // ── Workout count achievements ─────────────────────────────────────────
        Achievement(
            id = "workouts_5",
            titleFr = "Habitude en Formation",
            titleEn = "Habit Forming",
            descriptionFr = "5 séances complétées",
            descriptionEn = "5 workouts completed",
            iconEmoji = "💪",
            xpReward = 100,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "workouts_25",
            titleFr = "Athlète en Devenir",
            titleEn = "Rising Athlete",
            descriptionFr = "25 séances complétées",
            descriptionEn = "25 workouts completed",
            iconEmoji = "🥉",
            xpReward = 300,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "workouts_50",
            titleFr = "Demi-Centenaire",
            titleEn = "Half Century",
            descriptionFr = "50 séances complétées",
            descriptionEn = "50 workouts completed",
            iconEmoji = "🥈",
            xpReward = 500,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "workouts_100",
            titleFr = "Centenaire",
            titleEn = "Centurion",
            descriptionFr = "100 séances complétées — tu es une légende !",
            descriptionEn = "100 workouts completed — you're a legend!",
            iconEmoji = "🥇",
            xpReward = 1500,
            tier = AchievementTier.GOLD
        ),

        // ── Multiple workouts per day ─────────────────────────────────────────
        Achievement(
            id = "double_day",
            titleFr = "Double Session",
            titleEn = "Double Day",
            descriptionFr = "2 séances en un seul jour",
            descriptionEn = "2 workouts in a single day",
            iconEmoji = "⚡",
            xpReward = 150,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "triple_day",
            titleFr = "Machine de Guerre",
            titleEn = "War Machine",
            descriptionFr = "3 séances en un seul jour",
            descriptionEn = "3 workouts in a single day",
            iconEmoji = "🚀",
            xpReward = 400,
            tier = AchievementTier.GOLD
        ),

        // ── Nutrition achievements ────────────────────────────────────────────
        Achievement(
            id = "nutrition_7_days",
            titleFr = "Discipline Alimentaire",
            titleEn = "Food Discipline",
            descriptionFr = "Suivi nutrition 7 jours d'affilée",
            descriptionEn = "Tracked nutrition for 7 days in a row",
            iconEmoji = "🥗",
            xpReward = 200,
            tier = AchievementTier.SILVER
        ),

        // ── Volume achievements ───────────────────────────────────────────────
        Achievement(
            id = "volume_1000kg",
            titleFr = "Tonne Franchie",
            titleEn = "Tonne Crossed",
            descriptionFr = "1 000 kg de volume total soulevé",
            descriptionEn = "1,000 kg total volume lifted",
            iconEmoji = "🏋️",
            xpReward = 300,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "volume_10000kg",
            titleFr = "Hercule",
            titleEn = "Hercules",
            descriptionFr = "10 000 kg de volume total soulevé",
            descriptionEn = "10,000 kg total volume lifted",
            iconEmoji = "⚓",
            xpReward = 1000,
            tier = AchievementTier.GOLD
        ),

        // ── Sport variety ─────────────────────────────────────────────────────
        Achievement(
            id = "sports_3_types",
            titleFr = "Touche-à-Tout",
            titleEn = "All-Rounder",
            descriptionFr = "Pratique 3 sports différents",
            descriptionEn = "Practice 3 different sports",
            iconEmoji = "🎯",
            xpReward = 200,
            tier = AchievementTier.SILVER
        ),

        // ── Early bird ────────────────────────────────────────────────────────
        Achievement(
            id = "early_bird",
            titleFr = "Lève-Tôt",
            titleEn = "Early Bird",
            descriptionFr = "Séance avant 7h du matin",
            descriptionEn = "Workout before 7 AM",
            iconEmoji = "🌅",
            xpReward = 150,
            tier = AchievementTier.BRONZE
        ),

        // ── Night owl ─────────────────────────────────────────────────────────
        Achievement(
            id = "night_owl",
            titleFr = "Oiseau de Nuit",
            titleEn = "Night Owl",
            descriptionFr = "Séance après 21h",
            descriptionEn = "Workout after 9 PM",
            iconEmoji = "🦉",
            xpReward = 150,
            tier = AchievementTier.BRONZE
        ),

        // ── Level up ──────────────────────────────────────────────────────────
        Achievement(
            id = "level_5",
            titleFr = "Niveau 5",
            titleEn = "Level 5",
            descriptionFr = "Atteins le niveau 5",
            descriptionEn = "Reach level 5",
            iconEmoji = "⭐",
            xpReward = 0,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "level_10",
            titleFr = "Elite",
            titleEn = "Elite",
            descriptionFr = "Atteins le niveau 10",
            descriptionEn = "Reach level 10",
            iconEmoji = "🌟",
            xpReward = 0,
            tier = AchievementTier.GOLD
        ),
    )

    fun findById(id: String) = all.find { it.id == id }
}
