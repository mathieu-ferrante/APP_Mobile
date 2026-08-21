package com.phoenix.fitpro.presentation.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.model.WorkoutSession
import com.phoenix.fitpro.presentation.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    onAddWorkout: () -> Unit,
    onAddMeal: () -> Unit,
    onViewAchievements: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // ── Header gradient ───────────────────────────────────────────────────
        DashboardHeader(
            userName = state.profile.name,
            level = state.profile.level,
            xp = state.profile.xp,
            xpForNextLevel = state.profile.xpForNextLevel,
            xpInCurrentLevel = state.profile.xpInCurrentLevel
        )

        Spacer(Modifier.height(16.dp))

        // ── Streak card ───────────────────────────────────────────────────────
        StreakCard(
            streak = state.currentStreak,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ── Today summary ─────────────────────────────────────────────────────
        TodaySummaryRow(
            workoutCount = state.todayWorkouts.size,
            calories = state.todayCalories,
            weeklyWorkouts = state.weeklyWorkoutCount,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ── Quick actions ─────────────────────────────────────────────────────
        QuickActionsRow(
            onAddWorkout = onAddWorkout,
            onAddMeal = onAddMeal,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ── Today's workouts ──────────────────────────────────────────────────
        if (state.todayWorkouts.isNotEmpty()) {
            Text(
                text = "Séances d'aujourd'hui",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            state.todayWorkouts.forEach { session ->
                TodayWorkoutCard(session = session, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Motivational quote ────────────────────────────────────────────────
        Spacer(Modifier.height(8.dp))
        MotivationalQuoteCard(
            quote = state.motivationalQuote,
            onRefresh = viewModel::refreshQuote,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Recent achievement ────────────────────────────────────────────────
        state.recentAchievement?.let { achievement ->
            Spacer(Modifier.height(16.dp))
            RecentAchievementCard(
                achievement = achievement,
                onClick = onViewAchievements,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    level: Int,
    xp: Int,
    xpForNextLevel: Int,
    xpInCurrentLevel: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.25f),
                        DeepNavy.copy(alpha = 0f)
                    )
                )
            )
            .padding(top = 52.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        Column {
            val greeting = when (LocalTime.now().hour) {
                in 5..11  -> "Bonjour"
                in 12..17 -> "Bon après-midi"
                in 18..20 -> "Bonsoir"
                else      -> "Bonne nuit"
            }
            Text(
                text = "$greeting${if (userName.isNotBlank()) ", ${userName.split(" ").firstOrNull() ?: userName} 👋" else " 👋"}",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "🔥 Ashes · Renais plus fort que jamais",
                style = MaterialTheme.typography.bodyMedium,
                color = ElectricBlueLight
            )
            Spacer(Modifier.height(12.dp))

            // Level + XP bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElectricBlueAlpha15,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "⭐ Niv. $level",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBlueLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$xpInCurrentLevel / 500 XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (xpInCurrentLevel / 500f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = ElectricBlue,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakCard(streak: Int, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fire")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥",
                fontSize = (32 * if (streak > 0) scale else 1f).sp,
                modifier = if (streak > 0) Modifier.scale(scale) else Modifier
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Streak actuel",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = if (streak == 0) "Commence aujourd'hui !" else "$streak jours consécutifs",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (streak > 0) FireOrange else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (streak > 0) {
                Surface(
                    shape = CircleShape,
                    color = FireOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryRow(
    workoutCount: Int,
    calories: Int,
    weeklyWorkouts: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatMiniCard(
            emoji = "💪",
            value = workoutCount.toString(),
            label = "Séances\naujourd'hui",
            color = ElectricBlue,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            emoji = "🔥",
            value = if (calories > 0) "${calories} kcal" else "—",
            label = "Calories\naujourd'hui",
            color = AccentOrange,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            emoji = "📅",
            value = weeklyWorkouts.toString(),
            label = "Séances\ncette semaine",
            color = NeonGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatMiniCard(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddWorkout: () -> Unit,
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAddWorkout,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            Icon(Icons.Rounded.FitnessCenter, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("+ Séance", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onAddMeal,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
        ) {
            Icon(Icons.Rounded.Restaurant, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("+ Repas", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TodayWorkoutCard(session: WorkoutSession, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(session.sport.colorArgb).copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = Color(session.sport.colorArgb),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.sport.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = buildString {
                        append(session.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                        session.durationMinutes?.let { append(" · ${it}min") }
                        if (session.exercises.isNotEmpty()) append(" · ${session.exercises.size} ex.")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            // Intensity stars
            Row {
                repeat(session.intensity) {
                    Text("⭐", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MotivationalQuoteCard(
    quote: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElectricBlueAlpha15
        ),
        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "💬", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Refresh, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RecentAchievementCard(
    achievement: com.phoenix.fitpro.domain.model.Achievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NeonGreenAlpha15),
        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = achievement.iconEmoji, fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🏆 Badge débloqué !",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonGreen
                )
                Text(
                    text = achievement.titleFr,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.descriptionFr,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = TextSecondary)
        }
    }
}
