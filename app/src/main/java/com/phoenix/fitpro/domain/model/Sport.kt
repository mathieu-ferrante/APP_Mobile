package com.phoenix.fitpro.domain.model

import androidx.compose.ui.graphics.Color
import com.phoenix.fitpro.presentation.theme.ElectricBlue

data class Sport(
    val id: Long = 0,
    val name: String,
    val category: SportCategory,
    val iconName: String = "fitness_center",
    val colorArgb: Long = 0xFF4C6EF5,
    val isDefault: Boolean = false   // built-in sports cannot be deleted
) {
    val color: Color get() = Color(colorArgb)

    /** Per-sport emoji — maps known names, falls back to category emoji for custom sports */
    val emoji: String get() = sportEmojiMap[name] ?: when (category) {
        SportCategory.CARDIO       -> "🏃"
        SportCategory.STRENGTH     -> "💪"
        SportCategory.CYCLING      -> "🚴"
        SportCategory.WATER_SPORT  -> "🏊"
        SportCategory.FLEXIBILITY  -> "🧘"
        SportCategory.TEAM_SPORT   -> "⚽"
        SportCategory.MARTIAL_ARTS -> "🥊"
        SportCategory.OTHER        -> "🏅"
    }
}

private val sportEmojiMap = mapOf(
    "Marche / Course" to "🏃",
    "Course à pied"   to "🏃",
    "Marche"          to "🚶",
    "Musculation"     to "💪",
    "Tir à l'arc"     to "🏹",
    "Cyclisme"        to "🚴",
    "Natation"        to "🏊",
    "Yoga"            to "🧘",
    "Stretching"      to "🤸",
    "Football"        to "⚽",
    "Basketball"      to "🏀",
    "Tennis"          to "🎾",
    "Boxe"            to "🥊",
    "Escalade"        to "🧗",
    "Randonnée"       to "🥾",
    "HIIT"            to "⚡",
    "Padel"           to "🎾",
    "Danse"           to "💃",
    "Crossfit"        to "💪",
    "Ski"             to "⛷️",
    "Surf"            to "🏄",
    "Golf"            to "⛳",
    "Rugby"           to "🏉",
    "Handball"        to "🥌",
    "Volleyball"      to "🏐",
    "Ping-pong"       to "🏓",
    "Judo"            to "🥋",
    "Karaté"          to "🥋",
    "Arts martiaux"   to "🥋",
    "Triathlon"       to "🏅",
    "Pilàtes"          to "🧘",
)

enum class SportCategory(val labelFr: String, val labelEn: String, val iconName: String) {
    CARDIO("Cardio", "Cardio", "directions_run"),
    STRENGTH("Musculation", "Strength", "fitness_center"),
    FLEXIBILITY("Flexibilité", "Flexibility", "self_improvement"),
    TEAM_SPORT("Sport collectif", "Team Sport", "sports_soccer"),
    MARTIAL_ARTS("Arts martiaux", "Martial Arts", "sports_martial_arts"),
    WATER_SPORT("Sport aquatique", "Water Sport", "pool"),
    CYCLING("Cyclisme", "Cycling", "directions_bike"),
    OTHER("Autre", "Other", "sports")
}

/** Pre-loaded default sports seeded in the database */
object DefaultSports {
    val list = listOf(
        // ── Tes sports principaux ─────────────────────────────────────────────
        Sport(id = 1,  name = "Marche / Course",  category = SportCategory.CARDIO,    iconName = "directions_run",   colorArgb = 0xFF39D98A, isDefault = true),
        Sport(id = 2,  name = "Musculation",       category = SportCategory.STRENGTH,  iconName = "fitness_center",   colorArgb = 0xFF4C6EF5, isDefault = true),
        Sport(id = 3,  name = "Tir à l'arc",       category = SportCategory.OTHER,     iconName = "sports",           colorArgb = 0xFFFFB300, isDefault = true),
        // ── Autres sports ─────────────────────────────────────────────────────
        Sport(id = 4,  name = "Cyclisme",          category = SportCategory.CYCLING,   iconName = "directions_bike",  colorArgb = 0xFFFF6B35, isDefault = true),
        Sport(id = 5,  name = "Natation",          category = SportCategory.WATER_SPORT, iconName = "pool",           colorArgb = 0xFF00BCD4, isDefault = true),
        Sport(id = 6,  name = "Yoga",              category = SportCategory.FLEXIBILITY, iconName = "self_improvement", colorArgb = 0xFF9C27B0, isDefault = true),
        Sport(id = 7,  name = "Stretching",        category = SportCategory.FLEXIBILITY, iconName = "self_improvement", colorArgb = 0xFFAB47BC, isDefault = true),
        Sport(id = 8,  name = "Football",          category = SportCategory.TEAM_SPORT, iconName = "sports_soccer",  colorArgb = 0xFF4CAF50, isDefault = true),
        Sport(id = 9,  name = "Basketball",        category = SportCategory.TEAM_SPORT, iconName = "sports_basketball", colorArgb = 0xFFFF9800, isDefault = true),
        Sport(id = 10, name = "Tennis",            category = SportCategory.OTHER,     iconName = "sports_tennis",    colorArgb = 0xFFFFC107, isDefault = true),
        Sport(id = 11, name = "Boxe",              category = SportCategory.MARTIAL_ARTS, iconName = "sports_mma",   colorArgb = 0xFFF44336, isDefault = true),
        Sport(id = 12, name = "Escalade",          category = SportCategory.OTHER,     iconName = "landscape",        colorArgb = 0xFF795548, isDefault = true),
        Sport(id = 13, name = "Randonnée",         category = SportCategory.CARDIO,    iconName = "hiking",           colorArgb = 0xFF8BC34A, isDefault = true),
        Sport(id = 14, name = "HIIT",              category = SportCategory.CARDIO,    iconName = "bolt",             colorArgb = 0xFFFF6B35, isDefault = true),
    )
}
