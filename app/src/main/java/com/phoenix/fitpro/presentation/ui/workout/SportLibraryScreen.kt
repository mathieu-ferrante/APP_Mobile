package com.phoenix.fitpro.presentation.ui.workout

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.ai.GeminiAiService
import com.phoenix.fitpro.domain.model.Sport
import com.phoenix.fitpro.domain.model.SportCategory
import com.phoenix.fitpro.presentation.theme.*
import kotlinx.coroutines.launch

enum class SportLevel(val labelFr: String, val emoji: String) {
    BEGINNER("Débutant", "🌱"),
    INTERMEDIATE("Intermédiaire", "⚡"),
    ADVANCED("Confirmé", "🔥")
}

data class ExerciseGuide(
    val name: String,
    val setsReps: String,
    val description: String,
    val writtenTutorial: String,
    val searchKeyword: String
)

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
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary)
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
                                    level.labelFr,
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
                getExercisesForSport(sport.name, selectedLevel)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "📋 Programme suggéré — Niveau ${selectedLevel.labelFr}",
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
                                Text(ex.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ElectricBlueAlpha15
                                ) {
                                    Text(ex.setsReps, style = MaterialTheme.typography.labelSmall, color = ElectricBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(ex.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { selectedExerciseForTutorial = ex },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f))
                                ) {
                                    Icon(Icons.Rounded.MenuBook, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Tuto écrit", fontSize = 11.sp, color = ElectricBlue)
                                }

                                Button(
                                    onClick = {
                                        val query = Uri.encode("tutoriel ${sport.name} ${ex.searchKeyword}")
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query"))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
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
                                val prompt = "Donne 3 conseils clés d'expert et une progression pour progresser en ${sport.name} au niveau ${selectedLevel.labelFr}. Sois très précis et synthétique."
                                val res = GeminiAiService.generate(prompt)
                                aiAdviceText = res
                                isLoadingAi = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
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
                    Icon(Icons.Rounded.MenuBook, null, tint = ElectricBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedExerciseForTutorial!!.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text("💡 Exécution & Technique :", fontWeight = FontWeight.Bold, color = NeonGreen, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(selectedExerciseForTutorial!!.writtenTutorial, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
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

private fun getExercisesForSport(sportName: String, level: SportLevel): List<ExerciseGuide> {
    val s = sportName.lowercase()
    return when {
        s.contains("muscu") || s.contains("force") -> {
            when (level) {
                SportLevel.BEGINNER -> listOf(
                    ExerciseGuide("Squat au poids du corps / Goblet", "3 séries × 10-12 reps", "Apprentissage du mouvement de base pour les jambes et fessiers.", "Pieds écartés largeur d'épaules, poitrine fière, descendre jusqu'à avoir les cuisses parallèles au sol sans décoller les talons. Pousser sur les talons pour remonter.", "goblet squat debutant"),
                    ExerciseGuide("Développé couché avec haltères", "3 séries × 10 reps", "Renforcement des pectoraux, épaules et triceps.", "Allongé sur banc, haltères au niveau de la poitrine, pousser vers le haut en contrôlant la descente sur 2 secondes.", "developpe couche halteres"),
                    ExerciseGuide("Tirage horizontal poulie", "3 séries × 12 reps", "Renforcement du dos et de la posture.", "Buste droit, tirer la poignée vers le nombril en resserrant les omoplates en fin de mouvement.", "tirage horizontal poulie debutant")
                )
                SportLevel.INTERMEDIATE -> listOf(
                    ExerciseGuide("Squat à la barre guidée / libre", "4 séries × 8 reps", "Prise de force et de masse musculaire sur le bas du corps.", "Barre calée sur les trapèzes, inspirer et gainer la ceinture abdominale avant de descendre.", "squat barre libre technique"),
                    ExerciseGuide("Développé couché barre", "4 séries × 8 reps", "Exercice de référence pour la force du haut du corps.", "Prise un peu plus large que les épaules, descendre la barre au milieu des pectoraux avec contrôle.", "developpe couche barre execution"),
                    ExerciseGuide("Tractions pronation / supination", "4 séries × 6-8 reps", "Développement de la largeur du dos.", "Suspendu à la barre, tirer jusqu'à dépasser la barre avec le menton sans balancer le corps.", "tractions pronation technique")
                )
                SportLevel.ADVANCED -> listOf(
                    ExerciseGuide("Soulevé de terre lourd (Deadlift)", "5 séries × 5 reps", "Force globale, chaîne postérieure et gainage maximal.", "Barre collée aux tibias, dos parfaitement neutre, pousser le sol avec les jambes puis verrouiller le bassin.", "souleve de terre lourd execution"),
                    ExerciseGuide("Développé militaire barre debout (OHP)", "4 séries × 6 reps", "Force des épaules et stabilité du tronc.", "Pieds ancrés, fessiers serrés, presser la barre au-dessus de la tête sans cambrer excessivement le bas du dos.", "developpe militaire debout"),
                    ExerciseGuide("Dips lestés", "4 séries × 8 reps", "Intensité maximale sur les triceps et pectoraux bas.", "Corps légèrement incliné vers l'avant, descendre jusqu'à 90 degrés aux coudes puis remonter explosivement.", "dips lestes technique")
                )
            }
        }
        s.contains("arc") || s.contains("tir") -> {
            when (level) {
                SportLevel.BEGINNER -> listOf(
                    ExerciseGuide("Positionnement & Ancrage", "10 volées × 3 flèches", "Maîtrise de la posture stable et des repères au visage.", "Pieds perpendiculaires à la cible, épaules basses, corde venant toucher le bout du nez et le coin des lèvres.", "tir a l arc ancrage debutant"),
                    ExerciseGuide("Tirage avec élastique d'entraînement", "3 séries × 15 répétitions", "Renforcement des rhomboïdes sans fatigue articulaire.", "Fixer l'élastique, simuler l'armement en tirant avec le coude et les muscles du dos.", "entrainement elastique tir a l arc")
                )
                SportLevel.INTERMEDIATE -> listOf(
                    ExerciseGuide("Travail de visée et libération propre", "12 volées × 3 flèches à 18m", "Fluidité de la décoche et maintien de la visée après le tir.", "Ne pas bouger le bras d'arc après la décoche, laisser l'arc basculer naturellement.", "decoche tir a l arc fluide"),
                    ExerciseGuide("Face Pulls & Renforcement scapulaire", "4 séries × 12 reps", "Prévention des tendinites d'épaule et stabilité de la posture.", "Tirer la corde vers le visage en ouvrant les coudes vers le haut et l'arrière.", "face pull poulie tir a l arc")
                )
                SportLevel.ADVANCED -> listOf(
                    ExerciseGuide("Tir sous contrainte de temps (Rythme de match)", "15 volées de 3 flèches / 2 min chrono", "Simuler la pression de compétition et automatiser la routine.", "Garder une respiration diaphragmatique calme pendant toute la séquence de tir.", "preparation mentale competition tir a l arc"),
                    ExerciseGuide("Gainage anti-rotation & Stabilité unilatérale", "4 séries × 45s", "Stabilité du tronc sous tension de corde lourde.", "Gainage latéral avec élévation de jambe et Pallof Press à la poulie.", "gainage pallof press stabilite")
                )
            }
        }
        s.contains("course") || s.contains("run") -> {
            when (level) {
                SportLevel.BEGINNER -> listOf(
                    ExerciseGuide("Alternance Marche / Course (Walk-Run)", "30 min (2min course / 1min marche)", "Habituer les articulations et le système cardiovasculaire.", "Courir à une allure où l'on peut parler sans être essoufflé. Pas d'attaque talon trop violente.", "debuter la course a pied alternance marche course"),
                    ExerciseGuide("Renforcement mollets & chevilles", "3 séries × 15 montées sur pointes", "Prévention des périostites tibiales.", "Debout sur une marche, monter sur la pointe des pieds puis descendre sous l'horizontale.", "renforcement chevilles mollets course a pied")
                )
                SportLevel.INTERMEDIATE -> listOf(
                    ExerciseGuide("Fractionné court (30s / 30s)", "2 blocs de 8 × (30s vite / 30s trot)", "Augmentation de la VMA et de la capacité aérobie.", "30 secondes à allure soutenue (90-95% VMA) suivies de 30 secondes de trot de récupération.", "fractionne 30 30 course a pied"),
                    ExerciseGuide("Sortie longue en endurance fondamentale", "50 à 60 min à allure modérée", "Développement de la pompe cardiaque et de l'oxydation des lipides.", "Fréquence cardiaque basse (70-75% FCM). On doit pouvoir tenir une conversation sans forcer.", "endurance fondamentale course a pied")
                )
                SportLevel.ADVANCED -> listOf(
                    ExerciseGuide("Séance au seuil anaérobie (3 × 2000m)", "3 × 2000m allure 10km (2min repos)", "Recul du seuil d'accumulation des lactates.", "Maintenir une allure constante et rythmée sur chaque répétition sans faiblir.", "entrainement seuil course a pied"),
                    ExerciseGuide("Côtes courtes explosives", "10 × 100m en côte raide (retour trot)", "Puissance musculaire et foulée économique.", "Pousser fort sur les cuisses, monter les genoux et engager les bras activement.", "seance cotes course a pied puissance")
                )
            }
        }
        else -> {
            // General multi-sport fallback
            when (level) {
                SportLevel.BEGINNER -> listOf(
                    ExerciseGuide("Initiation aux fondamentaux", "30-40 min d'apprentissage", "Découverte des mouvements de base et échauffement articulaire complet.", "Prendre le temps d'assimiler les gestes techniques sans chercher la vitesse ou l'intensité.", "tutoriel debutant $sportName"),
                    ExerciseGuide("Échauffement dynamique spécifique", "10-15 min", "Préparation neuromusculaire et cardiovasculaire.", "Mobilisation de toutes les articulations sollicitées et montées progressives du cardio.", "echauffement specifique $sportName")
                )
                SportLevel.INTERMEDIATE -> listOf(
                    ExerciseGuide("Séance d'entraînement structurée", "45-60 min rythmées", "Développement de l'intensité et de l'endurance spécifique.", "Alterner les phases de travail technique et les séquences à intensité modérée.", "entrainement intermediaire $sportName"),
                    ExerciseGuide("Travail de précision et régularité", "4 séries de 5 min", "Répétition des gestes clés sous fatigue modérée.", "Se concentrer sur la fluidité et la qualité du mouvement.", "exercices progression $sportName")
                )
                SportLevel.ADVANCED -> listOf(
                    ExerciseGuide("Entraînement haute intensité & Performance", "60-75 min avec blocs intenses", "Optimisation des performances et résistance à l'effort maximal.", "Pousser l'intensité sur des blocs courts avec récupération active contrôlée.", "entrainement expert performance $sportName"),
                    ExerciseGuide("Préparation spécifique et récupération active", "20 min travail complémentaire", "Renforcement des points faibles et étirements post-séance.", "Cibler les chaînes musculaires antagonistes pour éviter les déséquilibres.", "preparation physique specifique $sportName")
                )
            }
        }
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
