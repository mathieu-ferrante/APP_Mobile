package com.phoenix.fitpro.presentation.ui.nutrition

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.model.FoodItem
import com.phoenix.fitpro.domain.model.MealType
import com.phoenix.fitpro.domain.model.StapleFoodDatabase
import com.phoenix.fitpro.presentation.theme.*
import com.phoenix.fitpro.presentation.ui.workout.fitMotionTextFieldColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onBack: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.addState.collectAsStateWithLifecycle()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.toPickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.setMealDate(millis.toLocalDate())
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

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.clearSaveSuccess()
            onBack()
        }
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un repas", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date du repas", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateShortcut(
                                label = "Hier",
                                selected = state.selectedDate == today.minusDays(1),
                                onClick = { viewModel.setMealDate(today.minusDays(1)) }
                            )
                            DateShortcut(
                                label = "Aujourd'hui",
                                selected = state.selectedDate == today,
                                onClick = { viewModel.setMealDate(today) }
                            )
                            DateShortcut(
                                label = "Demain",
                                selected = state.selectedDate == today.plusDays(1),
                                onClick = { viewModel.setMealDate(today.plusDays(1)) }
                            )
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Rounded.CalendarMonth, "Choisir une date", tint = NeonGreen)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = state.selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonGreen
                        )
                    }
                }
            }

            // ── Meal type selector ────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Type de repas", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MealType.values().sortedBy { it.order }.forEach { type ->
                                val selected = state.selectedMealType == type
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) NeonGreenAlpha15 else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (selected) NeonGreen.copy(0.5f) else DarkOutline,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.setMealType(type) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(type.emoji, fontSize = 24.sp)
                                    Text(
                                        text = type.labelFr.take(8),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) NeonGreen else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Food search ───────────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rechercher un aliment", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            label = { Text("🔍 Ex: Poulet, Riz, Saumon, Pomme, Pizza…") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = fitMotionTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (state.isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = ElectricBlue,
                                        strokeWidth = 2.dp
                                    )
                                } else if (state.searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Rounded.Clear, null, tint = TextSecondary)
                                    }
                                }
                            }
                        )

                        // AI Dish estimator button if query is typed
                        if (state.searchQuery.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.estimateDishWithAi(state.searchQuery) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreenAlpha15, contentColor = NeonGreen),
                                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
                                enabled = !state.isAiEstimating
                            ) {
                                if (state.isAiEstimating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonGreen)
                                } else {
                                    Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Estimer \"${state.searchQuery}\" avec l'IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Quick Staples row
                        if (state.searchQuery.isBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text("⚡ Ingrédients populaires rapides :", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            val staples = remember {
                                StapleFoodDatabase.items.take(10)
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(staples) { staple ->
                                    SuggestionChip(
                                        onClick = { viewModel.addFoodFromSearch(staple) },
                                        label = { Text("+ ${staple.name.take(20)}", fontSize = 11.sp, color = NeonGreen) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceVariant),
                                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Search results ────────────────────────────────────────────────
            if (state.searchResults.isNotEmpty()) {
                item {
                    Text("Résultats trouvés (${state.searchResults.size})", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
                items(state.searchResults.take(10)) { food ->
                    FoodResultCard(food = food, onAdd = { viewModel.addFoodFromSearch(food) })
                }
            }

            // ── Manual entry toggle ───────────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = viewModel::toggleManualForm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkOutline)
                ) {
                    Icon(
                        if (state.showManualForm) Icons.Rounded.ExpandLess else Icons.Rounded.Edit,
                        null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.showManualForm) "Masquer la saisie manuelle" else "Ajouter manuellement")
                }
            }

            // ── Manual form ───────────────────────────────────────────────────
            if (state.showManualForm) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Saisie manuelle", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                            OutlinedTextField(
                                value = state.manualFoodName,
                                onValueChange = viewModel::setManualFoodName,
                                label = { Text("Nom de l'aliment *") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = fitMotionTextFieldColors(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.manualCalories,
                                    onValueChange = viewModel::setManualCalories,
                                    label = { Text("Calories (opt.)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = fitMotionTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = state.manualQuantity,
                                    onValueChange = viewModel::setManualQuantity,
                                    label = { Text("Quantité") },
                                    modifier = Modifier.weight(1f),
                                    colors = fitMotionTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.manualProtein,
                                    onValueChange = viewModel::setManualProtein,
                                    label = { Text("Prot. (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    colors = fitMotionTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = state.manualCarbs,
                                    onValueChange = viewModel::setManualCarbs,
                                    label = { Text("Glu. (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    colors = fitMotionTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = state.manualFat,
                                    onValueChange = viewModel::setManualFat,
                                    label = { Text("Lip. (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    colors = fitMotionTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Button(
                                onClick = viewModel::addManualFood,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Icon(Icons.Rounded.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ajouter l'aliment")
                            }
                        }
                    }
                }
            }

            // ── Foods in this meal ────────────────────────────────────────────
            if (state.foods.isNotEmpty()) {
                item {
                    Text(
                        "Aliments dans ce repas (${state.foods.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                }
                items(state.foods.size) { i ->
                    val food = state.foods[i]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥘", fontSize = 20.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    buildString {
                                        append(food.quantity)
                                        food.calories?.let { append(" · $it kcal") }
                                        food.proteinG?.let { append(" · ${it.toInt()}g prot") }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            IconButton(onClick = { viewModel.removeFood(i) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Close, null, tint = TextDisabled, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // ── Error & Save ──────────────────────────────────────────────────
            item {
                state.error?.let {
                    Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = viewModel::saveMeal,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isSaving && state.foods.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = Color.Black,
                        disabledContainerColor = DarkOutline
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Rounded.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enregistrer le repas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DateShortcut(
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

@Composable
private fun FoodResultCard(food: FoodItem, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🍽️", fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    food.calories?.let {
                        Text("$it kcal", style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                    }
                    food.proteinG?.let {
                        Text("${it.toInt()}g P", style = MaterialTheme.typography.bodySmall, color = ElectricBlue)
                    }
                    food.carbsG?.let {
                        Text("${it.toInt()}g G", style = MaterialTheme.typography.bodySmall, color = AccentOrange)
                    }
                    food.fatG?.let {
                        Text("${it.toInt()}g L", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Rounded.AddCircle, null,
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
