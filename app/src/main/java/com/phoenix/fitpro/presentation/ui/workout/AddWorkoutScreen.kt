package com.phoenix.fitpro.presentation.ui.workout

import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.ai.GeminiAiService
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.presentation.theme.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.addState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.clearSaveSuccess()
            onBack()
        }
    }

    var showExerciseDialog by remember { mutableStateOf(false) }
    var isGeneratingAiExercises by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.toPickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.setDate(millis.toLocalDate())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showExerciseDialog) {
        AddExerciseDialog(
            sportName = state.selectedSport?.name ?: "Sport",
            onAdd = { ex ->
                viewModel.addExercise(ex)
                showExerciseDialog = false
            },
            onDismiss = { showExerciseDialog = false }
        )
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle séance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Sport selection ───────────────────────────────────────────────
            SportPickerSection(
                sports = state.sports,
                selectedSport = state.selectedSport,
                onSelectSport = viewModel::selectSport,
                onCreateSport = { name -> viewModel.addCustomSport(name, SportCategory.OTHER) }
            )

            // ── Date & Time (Editable with TimePickerDialog) ──────────────────
            SectionCard(title = "Date de la séance") {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkoutDateShortcut(
                        label = "Hier",
                        selected = state.date == today.minusDays(1),
                        onClick = { viewModel.setDate(today.minusDays(1)) }
                    )
                    WorkoutDateShortcut(
                        label = "Aujourd'hui",
                        selected = state.date == today,
                        onClick = { viewModel.setDate(today) }
                    )
                    WorkoutDateShortcut(
                        label = "Demain",
                        selected = state.date == today.plusDays(1),
                        onClick = { viewModel.setDate(today.plusDays(1)) }
                    )
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Rounded.CalendarMonth, "Choisir une date", tint = NeonGreen)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.date.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonGreen
                )
            }

            SectionCard(title = "Horaire") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val startFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val startStr = state.startTime.format(startFormatter)
                    val endStr = state.endTime?.format(startFormatter) ?: "Non défini"

                    // Start Time Picker Field
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val tpd = TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        viewModel.setStartTime(LocalTime.of(hour, minute))
                                    },
                                    state.startTime.hour,
                                    state.startTime.minute,
                                    true
                                )
                                tpd.show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, DarkOutline)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Début", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(startStr, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // End Time Picker Field
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val currentEnd = state.endTime ?: state.startTime.plusHours(1)
                                val tpd = TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        viewModel.setEndTime(LocalTime.of(hour, minute))
                                    },
                                    currentEnd.hour,
                                    currentEnd.minute,
                                    true
                                )
                                tpd.show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, DarkOutline)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Stop, null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Fin (opt.)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(endStr, style = MaterialTheme.typography.titleMedium, color = if (state.endTime != null) TextPrimary else TextDisabled, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Intensity ─────────────────────────────────────────────────────
            SectionCard(title = "⚡ Intensité") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..5).forEach { star ->
                        val selected = star <= state.intensity
                        Surface(
                            shape = CircleShape,
                            color = if (selected) ElectricBlue.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            modifier = Modifier.size(44.dp).clickable { viewModel.setIntensity(star) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (selected) "⭐" else "☆",
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (state.intensity) {
                        1 -> "Très léger"
                        2 -> "Léger"
                        3 -> "Modéré"
                        4 -> "Intense"
                        5 -> "Maximum 🔥"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // ── Recurrence ────────────────────────────────────────────────────
            SectionCard(title = "🔁 Récurrence") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    RecurrenceType.values().forEach { type ->
                        FilterChip(
                            selected = state.recurrenceType == type,
                            onClick = { viewModel.setRecurrenceType(type) },
                            label = { Text(type.labelFr, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlueAlpha15,
                                selectedLabelColor = ElectricBlue
                            )
                        )
                    }
                }
                if (state.recurrenceType == RecurrenceType.CUSTOM) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DayOfWeek.values().forEach { day ->
                            val selected = day in state.recurrenceDays
                            Surface(
                                shape = CircleShape,
                                color = if (selected) ElectricBlue else DarkSurfaceVariant,
                                modifier = Modifier.size(36.dp).clickable { viewModel.toggleRecurrenceDay(day) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = day.getDisplayName(TextStyle.NARROW, Locale.FRENCH),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Adaptive Exercises for Chosen Sport ────────────────────────────
            val sportName = state.selectedSport?.name ?: "Musculation"
            val suggestedPresets = remember(sportName) { getAdaptivePresetExercises(sportName) }

            SectionCard(title = "📋 Exercices adaptatifs — ${state.selectedSport?.name ?: "Général"}") {
                // Quick add chips based on active sport
                if (suggestedPresets.isNotEmpty()) {
                    Text("Suggestions rapides pour ${state.selectedSport?.name} :", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        lazyItems(suggestedPresets) { preset ->
                            SuggestionChip(
                                onClick = { viewModel.addExercise(preset) },
                                label = { Text("+ ${preset.exerciseName}", fontSize = 11.sp, color = NeonGreen) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceVariant),
                                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (state.exercises.isEmpty()) {
                    Text(
                        text = "Aucun exercice ajouté pour cette séance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    state.exercises.forEachIndexed { index, exercise ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("•", color = ElectricBlue, fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = exercise.exerciseName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = buildString {
                                    exercise.sets?.let { append("${it}×") }
                                    exercise.reps?.let { append("$it") }
                                    exercise.weightKg?.let { append(" @${it}kg") }
                                    exercise.durationSeconds?.let { append("${it / 60}min") }
                                    exercise.distanceKm?.let { append("${it}km") }
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            IconButton(onClick = { viewModel.removeExercise(index) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.Close, null, tint = TextDisabled, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showExerciseDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkOutline)
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ajouter", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            isGeneratingAiExercises = true
                            scope.launch {
                                val generated = generateAiSessionForSport(sportName)
                                generated.forEach { viewModel.addExercise(it) }
                                isGeneratingAiExercises = false
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreenAlpha15, contentColor = NeonGreen),
                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                    ) {
                        if (isGeneratingAiExercises) {
                            CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Séance IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Calories burned ───────────────────────────────────────────────
            SectionCard(title = "🔥 Calories brûlées (optionnel)") {
                OutlinedTextField(
                    value = state.caloriesBurned,
                    onValueChange = viewModel::setCaloriesBurned,
                    label = { Text("kcal estimées") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.LocalFireDepartment, null, tint = AccentOrange) }
                )
            }

            // ── Notes ─────────────────────────────────────────────────────────
            SectionCard(title = "📝 Notes") {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text("Remarques, ressenti…") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Save button ───────────────────────────────────────────────────
            state.error?.let {
                Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = viewModel::saveWorkout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    disabledContainerColor = DarkOutline
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Rounded.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enregistrer la séance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Adaptive exercise presets helper ──────────────────────────────────────────
@Composable
private fun WorkoutDateShortcut(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }
        } else null
    )
}

private fun LocalDate.toPickerMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun getAdaptivePresetExercises(sportName: String): List<ExerciseSet> {
    val s = sportName.lowercase()
    return when {
        s.contains("muscu") || s.contains("force") || s.contains("gym") -> listOf(
            ExerciseSet(exerciseName = "Développé couché", sets = 4, reps = 10, weightKg = 60f),
            ExerciseSet(exerciseName = "Squat barre", sets = 4, reps = 8, weightKg = 80f),
            ExerciseSet(exerciseName = "Tractions", sets = 3, reps = 8),
            ExerciseSet(exerciseName = "Rowing barre", sets = 3, reps = 10, weightKg = 50f)
        )
        s.contains("arc") || s.contains("tir") -> listOf(
            ExerciseSet(exerciseName = "Volées de tir 18m", sets = 10, reps = 3),
            ExerciseSet(exerciseName = "Face Pulls élastique", sets = 4, reps = 15),
            ExerciseSet(exerciseName = "Gainage anti-rotation", sets = 3, durationSeconds = 45),
            ExerciseSet(exerciseName = "Tirage scapulaire", sets = 3, reps = 12)
        )
        s.contains("course") || s.contains("run") || s.contains("jogging") -> listOf(
            ExerciseSet(exerciseName = "Échauffement progressif", durationSeconds = 600, distanceKm = 1.2f),
            ExerciseSet(exerciseName = "Fractionné 30s/30s", sets = 10, reps = 1),
            ExerciseSet(exerciseName = "Retour au calme", durationSeconds = 300, distanceKm = 0.5f)
        )
        s.contains("boxe") || s.contains("mma") || s.contains("combat") -> listOf(
            ExerciseSet(exerciseName = "Shadow boxing", sets = 3, durationSeconds = 180),
            ExerciseSet(exerciseName = "Sac de frappe (rounds)", sets = 5, durationSeconds = 180),
            ExerciseSet(exerciseName = "Corde à sauter", sets = 3, durationSeconds = 120)
        )
        s.contains("natation") || s.contains("swim") -> listOf(
            ExerciseSet(exerciseName = "Échauffement 4 nages", durationSeconds = 600, distanceKm = 0.4f),
            ExerciseSet(exerciseName = "Séries Crawl rapide", sets = 6, distanceKm = 0.1f),
            ExerciseSet(exerciseName = "Récupération dos crawlé", durationSeconds = 300, distanceKm = 0.2f)
        )
        s.contains("foot") || s.contains("basket") || s.contains("tennis") -> listOf(
            ExerciseSet(exerciseName = "Échauffement & Gammes", durationSeconds = 600),
            ExerciseSet(exerciseName = "Exercices techniques & tirs", sets = 4, reps = 10),
            ExerciseSet(exerciseName = "Match d'entraînement", durationSeconds = 2400)
        )
        else -> listOf(
            ExerciseSet(exerciseName = "Échauffement articulaire", durationSeconds = 300),
            ExerciseSet(exerciseName = "Bloc principal d'activité", durationSeconds = 1800),
            ExerciseSet(exerciseName = "Étirements et récupération", durationSeconds = 300)
        )
    }
}

private suspend fun generateAiSessionForSport(sportName: String): List<ExerciseSet> {
    return try {
        val prompt = "Génère 3 ou 4 exercices parfaits pour une séance de $sportName au format strict : Nom | Séries | Répétitions OU Durée min. Exemple : Squat | 4 | 10"
        val response = GeminiAiService.generate(prompt)
        val lines = response.lines().filter { it.contains("|") }
        if (lines.isNotEmpty()) {
            lines.mapNotNull { line ->
                val parts = line.split("|").map { it.trim().removePrefix("-").trim() }
                if (parts.isNotEmpty()) {
                    val name = parts.getOrNull(0)?.take(40) ?: return@mapNotNull null
                    val sets = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 3
                    val repsOrDur = parts.getOrNull(2)?.filter { it.isDigit() }?.toIntOrNull() ?: 10
                    ExerciseSet(
                        exerciseName = name,
                        sets = sets,
                        reps = repsOrDur
                    )
                } else null
            }
        } else {
            getAdaptivePresetExercises(sportName)
        }
    } catch (e: Exception) {
        getAdaptivePresetExercises(sportName)
    }
}

// ── Sport picker with free-text creation ─────────────────────────────────────
@Composable
private fun SportPickerSection(
    sports: List<Sport>,
    selectedSport: Sport?,
    onSelectSport: (Sport) -> Unit,
    onCreateSport: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, sports) {
        if (query.isBlank()) sports
        else sports.filter { it.name.contains(query, ignoreCase = true) }
    }
    val exactMatch = sports.any { it.name.equals(query.trim(), ignoreCase = true) }

    SectionCard(title = "🏋️ Sport") {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Rechercher ou créer un sport…") },
            placeholder = { Text("Ex: Padel, Danse, Tir à l'arc…", color = TextDisabled) },
            modifier = Modifier.fillMaxWidth(),
            colors = fitMotionTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextSecondary) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Clear, null, tint = TextSecondary)
                    }
                }
            },
            singleLine = true
        )

        selectedSport?.let { sport ->
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✅", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = sport.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (filtered.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lazyItems(filtered) { sport ->
                    val selected = sport.id == selectedSport?.id
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectSport(sport); query = "" },
                        label = { Text(sport.name, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlueAlpha15,
                            selectedLabelColor = ElectricBlue,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = ElectricBlue.copy(alpha = 0.5f),
                            borderColor = DarkOutline
                        )
                    )
                }
            }
        }

        if (query.isNotBlank() && !exactMatch) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    onCreateSport(query.trim())
                    query = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange.copy(alpha = 0.15f),
                    contentColor = AccentOrange
                )
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Créer \"${query.trim()}\"")
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AddExerciseDialog(
    sportName: String,
    onAdd: (ExerciseSet) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var isCardio by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Ajouter un exercice ($sportName)", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'exercice") },
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isCardio, onCheckedChange = { isCardio = it })
                    Spacer(Modifier.width(8.dp))
                    Text(if (isCardio) "Cardio / Durée" else "Musculation / Séries", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                if (!isCardio) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sets, onValueChange = { sets = it },
                            label = { Text("Séries") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fitMotionTextFieldColors(), shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = reps, onValueChange = { reps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fitMotionTextFieldColors(), shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = weight, onValueChange = { weight = it },
                        label = { Text("Poids (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = fitMotionTextFieldColors(), shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = duration, onValueChange = { duration = it },
                            label = { Text("Durée (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fitMotionTextFieldColors(), shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = distance, onValueChange = { distance = it },
                            label = { Text("Distance (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = fitMotionTextFieldColors(), shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(ExerciseSet(
                            exerciseName = name,
                            sets = sets.toIntOrNull(),
                            reps = reps.toIntOrNull(),
                            weightKg = weight.toFloatOrNull(),
                            durationSeconds = duration.toIntOrNull()?.times(60),
                            distanceKm = distance.toFloatOrNull()
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextSecondary) }
        }
    )
}

@Composable
fun fitMotionTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricBlue,
    unfocusedBorderColor = DarkOutline,
    focusedLabelColor = ElectricBlue,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = ElectricBlue,
    focusedContainerColor = DarkSurfaceVariant,
    unfocusedContainerColor = DarkSurfaceVariant
)
