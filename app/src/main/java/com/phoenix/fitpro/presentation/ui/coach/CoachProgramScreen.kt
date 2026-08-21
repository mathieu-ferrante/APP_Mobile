package com.phoenix.fitpro.presentation.ui.coach

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.ai.*
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.presentation.theme.*
import com.phoenix.fitpro.presentation.ui.workout.fitMotionTextFieldColors
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachProgramScreen(
    viewModel: CoachViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }
    val textToSpeech = remember(context) { TextToSpeech(context, null) }

    DisposableEffect(textToSpeech) {
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        })
        textToSpeech.setSpeechRate(0.94f)
        textToSpeech.setPitch(0.98f)
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    fun speakMessage(message: String) {
        if (isSpeaking) {
            textToSpeech.stop()
            isSpeaking = false
            return
        }
        val languageResult = textToSpeech.setLanguage(Locale.FRENCH)
        if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) return
        textToSpeech.voices
            .orEmpty()
            .filter { it.locale.language == Locale.FRENCH.language }
            .maxWithOrNull(
                compareBy<android.speech.tts.Voice>(
                    { it.locale.country == Locale.FRANCE.country },
                    { it.quality },
                    { !it.isNetworkConnectionRequired }
                )
            )
            ?.let(textToSpeech::setVoice)
        textToSpeech.speak(
            message.toSpeechText(),
            TextToSpeech.QUEUE_FLUSH,
            null,
            UUID.randomUUID().toString()
        )
    }

    if (state.showGeneratorDialog) {
        ProgramGeneratorDialog(
            state = state,
            onGoalSelected = viewModel::setFormGoal,
            onPreferredSportsChange = viewModel::setFormPreferredSports,
            onDislikedSportsChange = viewModel::setFormDislikedSports,
            onHealthIssuesChange = viewModel::setFormHealthIssues,
            onDaysChange = viewModel::setFormDaysPerWeek,
            onDurationChange = viewModel::setFormDurationMin,
            onGenerate = viewModel::generateAndApplyProgram,
            onDismiss = viewModel::closeGeneratorDialog
        )
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥 Coach IA Ashes", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = viewModel::openGeneratorDialog,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElectricBlueAlpha15,
                            contentColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Générateur", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Objective banner ──────────────────────────────────────────────
            ObjectiveHeader(
                goal = state.profile.goal,
                score = state.report?.overallScore ?: 75,
                onEditGoal = viewModel::openGeneratorDialog
            )

            // ── Tab selector ──────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = DarkSurface,
                contentColor = ElectricBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab.ordinal]),
                        color = NeonGreen
                    )
                }
            ) {
                CoachTab.values().forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.emoji, fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    tab.title,
                                    color = if (state.selectedTab == tab) NeonGreen else TextSecondary,
                                    fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            // ── Tab content ───────────────────────────────────────────────────
            when (state.selectedTab) {
                CoachTab.PROGRAM -> ProgramTabContent(
                    program = state.program,
                    onOpenGenerator = viewModel::openGeneratorDialog
                )
                CoachTab.DIAGNOSTIC -> DiagnosticTabContent(
                    report = state.report,
                    profile = state.profile,
                    onOpenGenerator = viewModel::openGeneratorDialog
                )
                CoachTab.CHAT -> CoachChatContent(
                    messages = state.chatMessages,
                    input = state.chatInput,
                    isThinking = state.isThinking,
                    isSpeaking = isSpeaking,
                    onInputChange = viewModel::setChatInput,
                    onSendMessage = { viewModel.sendChatMessage() },
                    onQuickActionClick = { viewModel.sendChatMessage(it) },
                    onSpeakMessage = ::speakMessage
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Program Tab Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProgramTabContent(
    program: TrainingProgram?,
    onOpenGenerator: () -> Unit
) {
    if (program == null || program.days.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("🧠", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Aucun programme actif", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Laisse le Coach IA concevoir un programme sur-mesure selon tes sports préférés et tes contraintes.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onOpenGenerator,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Générer mon programme", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var selectedDayIndex by remember { mutableStateOf(0) }
    val days = program.days
    val currentDay = days.getOrNull(selectedDayIndex) ?: days.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Days of week selector ─────────────────────────────────────────────
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(days) { index, day ->
                    val isSelected = index == selectedDayIndex
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ElectricBlueAlpha15 else DarkSurface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ElectricBlue else DarkOutline
                        ),
                        modifier = Modifier
                            .clickable { selectedDayIndex = index }
                            .width(88.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ElectricBlue else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(day.sportEmoji, fontSize = 22.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (day.isRestDay) "Repos" else "${day.estimatedDurationMin}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Selected day detail card ──────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, if (currentDay.isRestDay) DarkOutline else NeonGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonGreenAlpha15,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(currentDay.sportEmoji, fontSize = 24.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentDay.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentDay.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() }} · ${currentDay.estimatedDurationMin} min estimées",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonGreen
                            )
                        }
                    }

                    if (currentDay.focusDescription.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = currentDay.focusDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // ── Exercises list ────────────────────────────────────────────────────
        item {
            Text(
                text = if (currentDay.isRestDay) "Conseils de récupération" else "Exercices du jour (${currentDay.exercises.size})",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        itemsIndexed(currentDay.exercises) { idx, ex ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ElectricBlueAlpha15,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${idx + 1}", color = ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ex.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${ex.targetSets} séries × ${ex.targetReps}", style = MaterialTheme.typography.labelSmall, color = NeonGreen)
                            if (ex.restSeconds > 0) {
                                Text("⏱️ ${ex.restSeconds}s repos", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                        if (ex.notesOrFormTip.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text("💡 ${ex.notesOrFormTip}", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                        }
                    }
                }
            }
        }

        // ── Coach program tips ────────────────────────────────────────────────
        if (program.coachTips.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧠", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Recommandations du Coach Ashes", style = MaterialTheme.typography.titleSmall, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        program.coachTips.forEach { tip ->
                            Text(
                                text = "• $tip",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Diagnostic & Alerts Tab Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DiagnosticTabContent(
    report: CoachAnalysisReport?,
    profile: CoachUserProfile,
    onOpenGenerator: () -> Unit
) {
    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonGreen)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Alignment score card ──────────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Score d'alignement", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text("${report.overallScore}/100", style = MaterialTheme.typography.headlineLarge, color = NeonGreen, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(report.scoreSummary, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                    Surface(
                        shape = CircleShape,
                        color = NeonGreenAlpha15,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎯", fontSize = 32.sp)
                        }
                    }
                }
            }
        }

        // ── Nutrition Targets vs Actuals ───────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🥗 Équilibre Nutrition / Objectif (${profile.goal.title})", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))

                    // Calories Bar
                    MetricComparisonRow(
                        label = "🔥 Calories quotidiennes",
                        target = "${report.targetDailyCalories} kcal",
                        actual = if (report.actualAvgCalories > 0) "${report.actualAvgCalories} kcal" else "Pas de repas",
                        progress = if (report.targetDailyCalories > 0) (report.actualAvgCalories.toFloat() / report.targetDailyCalories).coerceIn(0f, 1.2f) else 0f,
                        color = AccentOrange
                    )

                    Spacer(Modifier.height(14.dp))

                    // Protein Bar
                    MetricComparisonRow(
                        label = "🥩 Protéines",
                        target = "${report.targetDailyProteinG.toInt()}g (${profile.goal.recommendedProteinPerKg}g/kg)",
                        actual = if (report.actualAvgProteinG > 0) "${report.actualAvgProteinG.toInt()}g" else "—",
                        progress = if (report.targetDailyProteinG > 0) (report.actualAvgProteinG / report.targetDailyProteinG).coerceIn(0f, 1.2f) else 0f,
                        color = NeonGreen
                    )
                }
            }
        }

        // ── Smart Alerts ──────────────────────────────────────────────────────
        item {
            Text("🚨 Diagnostic & Alertes IA", style = MaterialTheme.typography.titleSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        }

        items(report.alerts) { alert ->
            val alertBorderColor = when (alert.type) {
                AlertType.WARNING -> AccentOrange
                AlertType.SUCCESS -> NeonGreen
                AlertType.INFO -> ElectricBlue
            }
            val alertBg = when (alert.type) {
                AlertType.WARNING -> AccentOrange.copy(alpha = 0.08f)
                AlertType.SUCCESS -> NeonGreenAlpha15
                AlertType.INFO -> ElectricBlueAlpha15
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = alertBg),
                border = BorderStroke(1.dp, alertBorderColor.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (alert.type) {
                                AlertType.WARNING -> "⚠️"
                                AlertType.SUCCESS -> "✅"
                                AlertType.INFO -> "ℹ️"
                            },
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(alert.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(alert.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👉 Action : ${alert.actionSuggestion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = alertBorderColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // ── Nutrition Advice List ─────────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 Conseils Nutritionnels Personnalisés", style = MaterialTheme.typography.titleSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    report.nutritionAdvice.forEach { adv ->
                        Text("• $adv", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun MetricComparisonRow(
    label: String,
    target: String,
    actual: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            Text("Cible: $target", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = DarkOutline
        )
        Spacer(Modifier.height(2.dp))
        Text("Actuel : $actual", style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Coach Chat Tab Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CoachChatContent(
    messages: List<ChatMessage>,
    input: String,
    isThinking: Boolean,
    isSpeaking: Boolean,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onQuickActionClick: (String) -> Unit,
    onSpeakMessage: (String) -> Unit
) {
    val listState = rememberLazyListState()
    var voiceRepliesEnabled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(messages.lastOrNull()?.id, voiceRepliesEnabled) {
        val latestMessage = messages.lastOrNull()
        if (voiceRepliesEnabled && latestMessage?.isFromCoach == true) {
            onSpeakMessage(latestMessage.content)
        }
    }

    LaunchedEffect(messages.size, isThinking) {
        val targetIndex = messages.size + if (isThinking) 1 else 0
        if (targetIndex > 0) {
            listState.animateScrollToItem(targetIndex - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    msg = msg,
                    isSpeaking = isSpeaking,
                    onQuickActionClick = onQuickActionClick,
                    onSpeakMessage = onSpeakMessage
                )
            }
            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonGreenAlpha15,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text("🔥", fontSize = 16.sp) }
                        }
                        Spacer(Modifier.width(8.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = NeonGreen,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Ashes réfléchit…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick prompts bar
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, DarkOutline)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = onInputChange,
                        placeholder = { Text("Pose une question à ton Coach Ashes…", color = TextDisabled, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = fitMotionTextFieldColors(),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = onSendMessage,
                        modifier = Modifier
                            .size(44.dp)
                            .background(NeonGreen, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Send, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = null,
                        tint = if (voiceRepliesEnabled) NeonGreen else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Lire les réponses automatiquement", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Switch(
                        checked = voiceRepliesEnabled,
                        onCheckedChange = { voiceRepliesEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    msg: ChatMessage,
    isSpeaking: Boolean,
    onQuickActionClick: (String) -> Unit,
    onSpeakMessage: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isFromCoach) Alignment.Start else Alignment.End
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (msg.isFromCoach) {
                Surface(
                    shape = CircleShape,
                    color = NeonGreenAlpha15,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("🔥", fontSize = 16.sp) }
                }
                Spacer(Modifier.width(8.dp))
            }
            Card(
                shape = RoundedCornerShape(
                    topStart = if (msg.isFromCoach) 4.dp else 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = if (msg.isFromCoach) 16.dp else 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.isFromCoach) DarkSurface else ElectricBlue
                ),
                border = if (msg.isFromCoach) BorderStroke(1.dp, DarkOutline) else null,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = msg.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (msg.isFromCoach) TextPrimary else Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
            if (msg.isFromCoach) {
                IconButton(
                    onClick = { onSpeakMessage(msg.content) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.Rounded.VolumeUp,
                        contentDescription = if (isSpeaking) "Arrêter la lecture" else "Écouter la réponse",
                        tint = if (isSpeaking) AccentOrange else NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick action chips below coach message
        if (msg.quickActions.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.padding(start = 40.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                msg.quickActions.forEach { action ->
                    SuggestionChip(
                        onClick = { onQuickActionClick(action) },
                        label = { Text(action, fontSize = 11.sp, color = NeonGreen) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

private fun String.toSpeechText(): String = replace(Regex("[*_`#>]"), "")
    .replace(Regex("\\s+"), " ")
    .trim()

// ─────────────────────────────────────────────────────────────────────────────
// 4. Header & Dialog Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ObjectiveHeader(
    goal: FitnessGoal,
    score: Int,
    onEditGoal: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.12f), DeepNavy.copy(alpha = 0f))
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onEditGoal() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(goal.emoji, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Objectif Actuel", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                    Text(goal.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.Edit, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NeonGreenAlpha15,
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡ $score%", style = MaterialTheme.typography.labelMedium, color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProgramGeneratorDialog(
    state: CoachProgramUiState,
    onGoalSelected: (FitnessGoal) -> Unit,
    onPreferredSportsChange: (String) -> Unit,
    onDislikedSportsChange: (String) -> Unit,
    onHealthIssuesChange: (String) -> Unit,
    onDaysChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit,
    onGenerate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧠 Générateur de Programme IA", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Fitness Goal Selection
                Text("1. Ton Objectif Principal", style = MaterialTheme.typography.labelMedium, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                FitnessGoal.values().forEach { goal ->
                    val isSelected = state.formGoal == goal
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) ElectricBlueAlpha15 else DarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) ElectricBlue else DarkOutline),
                        modifier = Modifier.fillMaxWidth().clickable { onGoalSelected(goal) }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(goal.emoji, fontSize = 20.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(goal.title, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(goal.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // 2. Preferred Sports
                Text("2. Sports que tu aimes & pratiques", style = MaterialTheme.typography.labelMedium, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = state.formPreferredSports,
                    onValueChange = onPreferredSportsChange,
                    label = { Text("Ex: Musculation, Tir à l'arc, Marche…") },
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Disliked sports / movements
                Text("3. Sports ou exercices à éviter", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = state.formDislikedSports,
                    onValueChange = onDislikedSportsChange,
                    label = { Text("Ex: Burpees, Course sur bitume… (opt.)") },
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Health issues & Injuries
                Text("4. Soucis de santé / Blessures / Douleurs", style = MaterialTheme.typography.labelMedium, color = AccentOrange, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = state.formHealthIssues,
                    onValueChange = onHealthIssuesChange,
                    label = { Text("Ex: Mal de dos/lombaires, Genou fragile…") },
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Weekly frequency
                Text("5. Fréquence : ${state.formDaysPerWeek} jours / semaine", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (2..6).forEach { days ->
                        val isSelected = state.formDaysPerWeek == days
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) NeonGreen else DarkSurfaceVariant,
                            modifier = Modifier.size(38.dp).clickable { onDaysChange(days) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("$days j", color = if (isSelected) Color.Black else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Générer le programme", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextSecondary) }
        }
    )
}
