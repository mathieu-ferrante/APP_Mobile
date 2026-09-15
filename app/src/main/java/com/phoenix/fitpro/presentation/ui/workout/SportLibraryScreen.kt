package com.phoenix.fitpro.presentation.ui.workout

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.ai.AiService
import com.phoenix.fitpro.domain.model.Sport
import com.phoenix.fitpro.domain.model.ExerciseGuide
import com.phoenix.fitpro.domain.model.SportGuides
import com.phoenix.fitpro.domain.model.SportLevel
import com.phoenix.fitpro.domain.model.SportCategory
import com.phoenix.fitpro.presentation.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportLibraryScreen(
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<SportCategory?>(null) }
    var selectedSportForDetail by remember { mutableStateOf<Sport?>(null) }

    if (showAddDialog) {
        AddSportDialog(
            onAdd = { name, category ->
                viewModel.addCustomSport(name, category)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (selectedSportForDetail != null) {
        SportDetailBottomSheet(
            sport = selectedSportForDetail!!,
            onDismiss = { selectedSportForDetail = null }
        )
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Bibliothèque de sports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, null, tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Guide interactif par niveau", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                            Text("Clique sur un sport pour voir les séances Débutant, Intermédiaire, Confirmé et les tutoriels vidéo/écrits IA.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Category filter chips
            item {
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = if (selectedCategory == it) null else it }
                )
            }

            val filteredSports = if (selectedCategory != null) {
                state.sports.filter { it.category == selectedCategory }
            } else state.sports

            items(filteredSports) { sport ->
                SportCard(
                    sport = sport,
                    onClick = { selectedSportForDetail = sport },
                    onDelete = if (!sport.isDefault) ({ viewModel.deleteSport(sport) }) else null
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
                ) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Créer un sport personnalisé")
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: SportCategory?,
    onSelectCategory: (SportCategory) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SportCategory.values().take(4).forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(category.labelFr, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricBlueAlpha15,
                    selectedLabelColor = ElectricBlue
                )
            )
        }
    }
}

@Composable
private fun SportCard(
    sport: Sport,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = if (!sport.isDefault) null else BorderStroke(1.dp, Color(sport.colorArgb).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(sport.colorArgb).copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sport.emoji,
                        fontSize = 22.sp
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sport.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${sport.category.labelFr} · Appuie pour voir les guides",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
            if (onDelete != null) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportDetailBottomSheet(
    sport: Sport,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedLevel by remember { mutableStateOf(SportLevel.BEGINNER) }
    var selectedExerciseForTutorial by remember { mutableStateOf<ExerciseGuide?>(null) }
    var aiAdviceText by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricBlueAlpha15,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(sport.emoji, fontSize = 26.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(sport.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Catégorie : ${sport.category.labelFr}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Level tabs
            TabRow(
                selectedTabIndex = selectedLevel.ordinal,
                containerColor = DarkSurfaceVariant,
                contentColor = ElectricBlue
            ) {
                SportLevel.values().forEach { level ->
                    Tab(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(level.emoji, fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    level.label,
                                    color = if (selectedLevel == level) NeonGreen else TextSecondary,
                                    fontWeight = if (selectedLevel == level) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val exercises = remember(sport.name, selectedLevel) {
                SportGuides.forSport(sport.name, selectedLevel)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "📋 Programme suggéré — Niveau ${selectedLevel.label}",
                        style = MaterialTheme.typography.titleSmall,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(exercises) { ex ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, DarkOutline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // weight + fill = false : le nom s'enroule sur
                                // plusieurs lignes au lieu de repousser le badge
                                // series/reps hors de l'ecran.
                                Text(
                                    ex.name.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ElectricBlueAlpha15
                                ) {
                                    Text(ex.setsReps.value, style = MaterialTheme.typography.labelSmall, color = ElectricBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(ex.description.value, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { selectedExerciseForTutorial = ex },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 36.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f))
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Tuto écrit", fontSize = 11.sp, color = ElectricBlue)
                                }

                                Button(
                                    onClick = {
                                        // Video precise quand elle est connue et
                                        // verifiee ; sinon recherche ciblee, ce qui
                                        // evite un lien mort si elle disparait.
                                        val url = ex.videoId?.let { "https://www.youtube.com/watch?v=$it" }
                                            ?: "https://www.youtube.com/results?search_query=" +
                                            Uri.encode("${sport.name} ${ex.searchKeyword}")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 36.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.8f))
                                ) {
                                    Icon(Icons.Rounded.PlayCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Tuto Vidéo", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // AI Advice button
                item {
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            isLoadingAi = true
                            scope.launch {
                                val prompt = "Donne 3 conseils clés d'expert et une progression pour progresser en ${sport.name} au niveau ${selectedLevel.label}. Sois très précis et synthétique."
                                val res = AiService.generate(prompt)
                                aiAdviceText = res
                                isLoadingAi = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                    ) {
                        if (isLoadingAi) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Conseils Coach IA pour ce sport", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (aiAdviceText != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NeonGreenAlpha15),
                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✨", fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Conseils IA — ${sport.name}", fontWeight = FontWeight.Bold, color = NeonGreen, style = MaterialTheme.typography.titleSmall)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(aiAdviceText!!, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedExerciseForTutorial != null) {
        AlertDialog(
            onDismissRequest = { selectedExerciseForTutorial = null },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = ElectricBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedExerciseForTutorial!!.name.value, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text("💡 Exécution & Technique :", fontWeight = FontWeight.Bold, color = NeonGreen, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(selectedExerciseForTutorial!!.writtenTutorial.value, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val query = Uri.encode("tutoriel ${sport.name} ${selectedExerciseForTutorial!!.searchKeyword}")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Icon(Icons.Rounded.PlayCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Voir la vidéo")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedExerciseForTutorial = null }) {
                    Text("Fermer", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AddSportDialog(
    onAdd: (String, SportCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SportCategory.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Nouveau sport", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du sport") },
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Catégorie", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                SportCategory.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category.labelFr, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricBlueAlpha15,
                                    selectedLabelColor = ElectricBlue
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name, selectedCategory) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextSecondary) }
        }
    )
}
