package com.phoenix.fitpro.presentation.ui.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.model.WorkoutSession
import com.phoenix.fitpro.presentation.theme.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WorkoutScreen(
    onAddWorkout: () -> Unit,
    onOpenSportLibrary: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DeepNavy,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddWorkout,
                containerColor = ElectricBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Nouvelle séance", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item {
                WorkoutScreenHeader(
                    totalCount = state.totalCount,
                    onOpenSportLibrary = onOpenSportLibrary
                )
            }

            // ── Today's sessions ──────────────────────────────────────────────
            item {
                SectionTitle(
                    title = "Aujourd'hui",
                    emoji = "⚡",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (state.todaySessions.isEmpty()) {
                item {
                    EmptyWorkoutCard(
                        message = "Pas encore de séance aujourd'hui — allez, c'est parti ! 💪",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(state.todaySessions) { session ->
                    WorkoutSessionCard(
                        session = session,
                        onDelete = { viewModel.deleteSession(session) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // ── History ───────────────────────────────────────────────────────
            if (state.recentSessions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle(
                        title = "Historique récent",
                        emoji = "📅",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(state.recentSessions.drop(state.todaySessions.size).take(10)) { session ->
                    WorkoutSessionCard(
                        session = session,
                        onDelete = { viewModel.deleteSession(session) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutScreenHeader(totalCount: Int, onOpenSportLibrary: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.15f), DeepNavy.copy(alpha = 0f))
                )
            )
            .padding(top = 52.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "💪 Mes Séances",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$totalCount séances au total",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            OutlinedButton(
                onClick = onOpenSportLibrary,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Sports, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sports", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun WorkoutSessionCard(
    session: WorkoutSession,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la séance ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Supprimer", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sport icon badge
                Surface(
                    shape = CircleShape,
                    color = Color(session.sport.colorArgb).copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (session.sport.category.name) {
                                "CARDIO" -> "🏃"
                                "STRENGTH" -> "💪"
                                "CYCLING" -> "🚴"
                                "WATER_SPORT" -> "🏊"
                                "FLEXIBILITY" -> "🧘"
                                "TEAM_SPORT" -> "⚽"
                                "MARTIAL_ARTS" -> "🥋"
                                else -> "🏋️"
                            },
                            fontSize = 22.sp
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.sport.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val dayStr = session.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                        val dateStr = session.date.format(DateTimeFormatter.ofPattern("d MMM"))
                        Text(
                            text = "$dayStr $dateStr · ${session.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        session.durationMinutes?.let {
                            Text("· ${it}min", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
                // Intensity
                Row {
                    repeat(session.intensity) { Text("⭐", fontSize = 11.sp) }
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = TextDisabled, modifier = Modifier.size(18.dp))
                }
            }

            // ── Expanded details ───────────────────────────────────────────────
            if (expanded && session.exercises.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = DarkOutline)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Exercices",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                session.exercises.forEach { exercise ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ElectricBlueAlpha15,
                            modifier = Modifier.width(4.dp).height(20.dp)
                        ) {}
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = exercise.exerciseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = buildString {
                                exercise.sets?.let { append("${it}×") }
                                exercise.reps?.let { append("$it reps") }
                                exercise.weightKg?.let { append(" @ ${it}kg") }
                                exercise.durationSeconds?.let { append("${it / 60}min") }
                                exercise.distanceKm?.let { append(" ${it}km") }
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue
                        )
                    }
                }
            }

            if (session.notes.isNotBlank() && expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "📝 ${session.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, emoji: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyWorkoutCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏋️", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
