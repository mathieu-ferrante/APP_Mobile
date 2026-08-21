package com.phoenix.fitpro.presentation.ui.nutrition

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.ai.ChatMessage
import com.phoenix.fitpro.domain.model.MealEntry
import com.phoenix.fitpro.domain.model.MealType
import com.phoenix.fitpro.presentation.theme.*
import com.phoenix.fitpro.presentation.ui.workout.fitMotionTextFieldColors
import java.time.LocalDate

@Composable
fun NutritionScreen(
    onAddMeal: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DeepNavy,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMeal,
                containerColor = NeonGreen,
                contentColor = Color.Black,
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Ajouter un repas", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Calories & Macros
            item {
                NutritionHeader(
                    totalCalories = state.totalCalories,
                    mealCount = state.todayMeals.size,
                    proteinG = state.totalProteinG,
                    carbsG = state.totalCarbsG,
                    fatG = state.totalFatG
                )
            }

            // Weekly calorie chart
            if (state.weeklyCalories.isNotEmpty()) {
                item {
                    WeeklyCaloriesBar(
                        data = state.weeklyCalories,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Coach Nutrition IA Card
            item {
                NutritionCoachCard(
                    messages = state.coachMessages,
                    input = state.coachInput,
                    isThinking = state.isCoachThinking,
                    onInputChange = viewModel::setCoachInput,
                    onSendMessage = { viewModel.sendCoachMessage() },
                    onQuickActionClick = { viewModel.sendCoachMessage(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Meals header
            item {
                PaddingValues(horizontal = 16.dp).let {
                    Text(
                        "🍽️ Repas enregistrés aujourd'hui",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Meals by type
            MealType.values().sortedBy { it.order }.forEach { mealType ->
                val mealsOfType = state.todayMeals.filter { it.mealType == mealType }
                if (mealsOfType.isNotEmpty()) {
                    item {
                        MealTypeSectionHeader(mealType = mealType)
                    }
                    items(mealsOfType) { meal ->
                        MealCard(
                            meal = meal,
                            onDelete = { viewModel.deleteMeal(meal) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (state.todayMeals.isEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🥗", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Aucun repas enregistré aujourd'hui",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionHeader(
    totalCalories: Int,
    mealCount: Int,
    proteinG: Float,
    carbsG: Float,
    fatG: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NeonGreen.copy(alpha = 0.12f), DeepNavy.copy(alpha = 0f))
                )
            )
            .padding(top = 52.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Column {
            Text(
                text = "🥗 Nutrition",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Calories card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonGreenAlpha15),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🔥 Calories", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (totalCalories > 0) "$totalCalories" else "—",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text("kcal consommées", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                // Meals & Protein card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🥩 Protéines", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (proteinG > 0) "${proteinG.toInt()}g" else "—",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text("$mealCount repas", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            // Macro summary pills
            if (proteinG > 0 || carbsG > 0 || fatG > 0) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = ElectricBlueAlpha15) {
                        Text("🥩 Prot : ${proteinG.toInt()}g", color = ElectricBlue, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = AccentOrangeAlpha15) {
                        Text("🍚 Gluc : ${carbsG.toInt()}g", color = AccentOrange, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = DarkSurfaceVariant) {
                        Text("🥑 Lip : ${fatG.toInt()}g", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionCoachCard(
    messages: List<ChatMessage>,
    input: String,
    isThinking: Boolean,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onQuickActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = NeonGreenAlpha15,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("🥗", fontSize = 18.sp) }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Coach Nutrition IA", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Conseils en temps réel sur tes repas & macros", style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Display latest coach messages (max 3)
            val recentMessages = messages.takeLast(3)
            recentMessages.forEach { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.isFromCoach) DarkSurfaceVariant else ElectricBlue.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = (if (msg.isFromCoach) "🤖 " else "👤 ") + msg.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.isFromCoach) TextPrimary else ElectricBlue,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (isThinking) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = NeonGreen, strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Le coach nutrition réfléchit…", style = MaterialTheme.typography.labelSmall, color = NeonGreen)
                }
            }

            // Quick actions
            val latestQuickActions = messages.lastOrNull { it.isFromCoach }?.quickActions ?: emptyList()
            if (latestQuickActions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(latestQuickActions) { action ->
                        SuggestionChip(
                            onClick = { onQuickActionClick(action) },
                            label = { Text(action, fontSize = 11.sp, color = NeonGreen) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceVariant),
                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    placeholder = { Text("Pose une question au coach nutrition…", fontSize = 12.sp, color = TextDisabled) },
                    modifier = Modifier.weight(1f),
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onSendMessage,
                    modifier = Modifier
                        .size(40.dp)
                        .background(NeonGreen, CircleShape)
                ) {
                    Icon(Icons.Rounded.Send, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun WeeklyCaloriesBar(
    data: List<Pair<LocalDate, Int>>,
    modifier: Modifier = Modifier
) {
    val maxCal = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Apport calorique — 7 derniers jours",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val today = LocalDate.now()
                data.forEach { (date, cal) ->
                    val height = ((cal.toFloat() / maxCal) * 70).coerceAtLeast(4f)
                    val isToday = date == today
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isToday) NeonGreen else NeonGreen.copy(alpha = 0.4f))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) NeonGreen else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealTypeSectionHeader(mealType: MealType) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(mealType.emoji, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = mealType.labelFr,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MealCard(
    meal: MealEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${meal.foods.size} aliment${if (meal.foods.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    if (meal.hasCaloriesData) {
                        Text(
                            text = "${meal.totalCalories} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonGreen
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = TextDisabled, modifier = Modifier.size(18.dp))
                }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = DarkOutline)
                Spacer(Modifier.height(8.dp))
                meal.foods.forEach { food ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("•", color = NeonGreen, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        food.calories?.let {
                            Text("$it kcal", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
