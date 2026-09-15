package com.phoenix.fitpro.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Objectif de la semaine en cours.
 *
 * Les objectifs ne sont pas figes : ils se deduisent des sports reellement
 * pratiques au cours des quatre semaines precedentes. Quelqu'un qui fait du tir
 * a l'arc, de la musculation et de la marche recoit des objectifs sur ces trois
 * activites, pas sur une liste generique decidee a l'avance.
 */
data class WeeklyGoal(
    val id: String,
    val emoji: String,
    val title: Localized,
    val detail: Localized,
    val target: Int,
    val current: Int,
    val xpReward: Int
) {
    val isComplete: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
}

object WeeklyChallenges {

    /** Nombre de semaines d'historique servant a calibrer les objectifs. */
    private const val HISTORY_WEEKS = 4L

    /** Au-dela de trois objectifs par sport, la liste devient illisible. */
    private const val MAX_SPORT_GOALS = 3

    fun weekStart(today: LocalDate): LocalDate =
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /**
     * @param sessions toutes les seances des cinq dernieres semaines.
     *   Celles de la semaine en cours comptent pour l'avancement, les
     *   precedentes servent a calibrer les cibles.
     */
    fun generate(sessions: List<WorkoutSession>, today: LocalDate = LocalDate.now()): List<WeeklyGoal> {
        val start = weekStart(today)
        val historyStart = start.minusWeeks(HISTORY_WEEKS)

        val thisWeek = sessions.filter { !it.date.isBefore(start) && !it.date.isAfter(today) }
        val history = sessions.filter { it.date >= historyStart && it.date < start }

        val goals = mutableListOf<WeeklyGoal>()

        // ── Un objectif par sport regulier ────────────────────────────────────
        val perSport = history.groupingBy { it.sport.name }.eachCount()
        val topSports = perSport.entries
            .sortedByDescending { it.value }
            .take(MAX_SPORT_GOALS)

        for ((sportName, count) in topSports) {
            val averagePerWeek = count / HISTORY_WEEKS.toDouble()
            // Legerement au-dessus de l'habitude : un objectif deja atteint sans
            // rien changer n'a aucun interet, un objectif hors d'atteinte non plus.
            val target = ceil(averagePerWeek).toInt().coerceIn(1, 6).let {
                if (averagePerWeek >= it - 0.25) it + 1 else it
            }.coerceAtMost(7)

            val emoji = history.firstOrNull { it.sport.name == sportName }?.sport?.emoji ?: "🏅"
            goals.add(
                WeeklyGoal(
                    id = "sport_${sportName.lowercase().replace(' ', '_')}",
                    emoji = emoji,
                    title = Localized("$sportName × $target", "$sportName × $target"),
                    detail = Localized(
                        "Tu en fais environ ${averagePerWeek.roundToInt()} par semaine. Vise $target.",
                        "You average about ${averagePerWeek.roundToInt()} per week. Aim for $target."
                    ),
                    target = target,
                    current = thisWeek.count { it.sport.name == sportName },
                    xpReward = 40 + 20 * target
                )
            )
        }

        // ── Assiduite, independante du sport ──────────────────────────────────
        val historicActiveDays = history.map { it.date }.distinct().size
        val averageDays = (historicActiveDays / HISTORY_WEEKS.toDouble()).roundToInt()
        val dayTarget = (averageDays + 1).coerceIn(3, 6)
        goals.add(
            WeeklyGoal(
                id = "active_days",
                emoji = "🔥",
                title = Localized("$dayTarget jours actifs", "$dayTarget active days"),
                detail = Localized(
                    "Bouge au moins $dayTarget jours differents cette semaine.",
                    "Train on at least $dayTarget different days this week."
                ),
                target = dayTarget,
                current = thisWeek.map { it.date }.distinct().size,
                xpReward = 40 + 20 * dayTarget
            )
        )

        // ── Demarrage : aucun historique exploitable ──────────────────────────
        if (goals.size == 1) {
            goals.add(0, starterGoal(thisWeek))
        }

        return goals
    }

    /** Premier objectif quand l'application ne connait pas encore les habitudes. */
    private fun starterGoal(thisWeek: List<WorkoutSession>) = WeeklyGoal(
        id = "starter_sessions",
        emoji = "🚀",
        title = Localized("3 séances", "3 workouts"),
        detail = Localized(
            "Enregistre 3 séances pour que le coach apprenne tes habitudes.",
            "Log 3 workouts so the coach can learn your habits."
        ),
        target = 3,
        current = thisWeek.size,
        xpReward = 100
    )
}
