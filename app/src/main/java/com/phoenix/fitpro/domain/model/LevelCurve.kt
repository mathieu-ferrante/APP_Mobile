package com.phoenix.fitpro.domain.model

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Courbe de progression des niveaux.
 *
 * L'ancien systeme etait lineaire : `1 + xp / 500`, soit 500 XP par niveau du
 * premier au centieme. Passer niveau 40 coutait exactement autant que passer
 * niveau 2, ce qui retire tout relief a la progression.
 *
 * Le cout cumule suit desormais une puissance : `XP(niveau) = BASE × (n-1)^EXPOSANT`.
 * Les premiers niveaux tombent vite, ce qui accroche au demarrage, puis chaque
 * palier demande sensiblement plus que le precedent.
 *
 * Avec une pratique reguliere (4 seances et 7 jours de serie par semaine, soit
 * environ 270 XP hebdomadaires) :
 *
 * | Niveau | XP cumule | Delai approximatif |
 * |--------|-----------|--------------------|
 * | 2      | 150       | 4 jours            |
 * | 5      | 1 379     | 5 semaines         |
 * | 10     | 5 115     | 19 semaines        |
 * | 20     | 16 200    | 60 semaines        |
 *
 * Attention : la conversion XP → niveau change. Un profil existant peut perdre
 * un ou deux niveaux au premier lancement, son XP total restant intact.
 */
object LevelCurve {

    private const val BASE = 150.0
    private const val EXPONENT = 1.6

    const val MAX_LEVEL = 99

    /** XP cumule necessaire pour atteindre [level]. Vaut 0 au niveau 1. */
    fun totalXpForLevel(level: Int): Int {
        if (level <= 1) return 0
        val capped = level.coerceAtMost(MAX_LEVEL)
        return (BASE * (capped - 1).toDouble().pow(EXPONENT)).roundToInt()
    }

    /** Niveau atteint avec [xp] points au total. */
    fun levelForXp(xp: Int): Int {
        if (xp <= 0) return 1
        var level = 1
        while (level < MAX_LEVEL && xp >= totalXpForLevel(level + 1)) level++
        return level
    }

    /** Cout du passage de [level] au suivant. */
    fun xpSpanOfLevel(level: Int): Int =
        (totalXpForLevel(level + 1) - totalXpForLevel(level)).coerceAtLeast(1)

    /** XP deja acquis a l'interieur du niveau courant. */
    fun xpIntoCurrentLevel(xp: Int): Int =
        (xp - totalXpForLevel(levelForXp(xp))).coerceAtLeast(0)

    /** XP restant avant le niveau suivant, ou 0 au niveau maximal. */
    fun xpRemainingToNextLevel(xp: Int): Int {
        val level = levelForXp(xp)
        if (level >= MAX_LEVEL) return 0
        return (totalXpForLevel(level + 1) - xp).coerceAtLeast(0)
    }

    /** Avancement dans le niveau courant, entre 0 et 1. */
    fun progress(xp: Int): Float {
        val level = levelForXp(xp)
        if (level >= MAX_LEVEL) return 1f
        return (xpIntoCurrentLevel(xp).toFloat() / xpSpanOfLevel(level)).coerceIn(0f, 1f)
    }
}
