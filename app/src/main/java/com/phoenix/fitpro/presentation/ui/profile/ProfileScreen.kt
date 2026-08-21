package com.phoenix.fitpro.presentation.ui.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.presentation.theme.*
import com.phoenix.fitpro.presentation.util.LanguageHelper

@Composable
fun ProfileScreen(
    onViewAchievements: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Form state
    var nameField by remember(state.profile.name) { mutableStateOf(state.profile.name) }
    var weightField by remember(state.profile.weightKg) { mutableStateOf(state.profile.weightKg?.toString() ?: "") }
    var heightField by remember(state.profile.heightCm) { mutableStateOf(state.profile.heightCm?.toString() ?: "") }
    var selectedObjective by remember(state.profile.objectiveType) { mutableStateOf(state.profile.objectiveType) }
    var language by remember(state.profile.preferredLanguage) { mutableStateOf(state.profile.preferredLanguage) }
    var notificationsEnabled by remember(state.profile.notificationsEnabled) { mutableStateOf(state.profile.notificationsEnabled) }

    // Save success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar("Profil enregistrÃ© !")
            viewModel.clearSaveSuccess()
        }
    }

    if (state.showAuthDialog) {
        AuthAccountDialog(
            state = state,
            onEmailChange = viewModel::setAuthInputEmail,
            onNameChange = viewModel::setAuthInputName,
            onSignIn = viewModel::signInWithEmail,
            onSwitchAccount = viewModel::switchAccount,
            onDeleteAccount = viewModel::deleteAccount,
            onSignOut = viewModel::signOut,
            onDismiss = viewModel::closeAuthDialog
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepNavy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientMid.copy(alpha = 0.3f), DeepNavy.copy(alpha = 0f))
                        )
                    )
                    .padding(top = 52.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(GradientStart, GradientEnd))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.profile.name.firstOrNull()?.uppercaseChar()?.toString() ?: "\uD83D\uDC64",
                            fontSize = 32.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.profile.name.ifBlank { "Mon Profil" },
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.profile.objectiveType.emoji} ${state.profile.objectiveType.labelFr}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        // XP Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ElectricBlueAlpha15
                        ) {
                            Text(
                                text = "\u2B50 Niveau ${state.profile.level} \u00b7 ${state.profile.xp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricBlueLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Edit / Done button
                    if (state.isEditing) {
                        IconButton(
                            onClick = { viewModel.cancelEditing() }
                        ) {
                            Icon(Icons.Rounded.Close, null, tint = TextSecondary)
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.startEditing() }
                        ) {
                            Icon(Icons.Rounded.Edit, null, tint = ElectricBlueLight)
                        }
                    }
                }
            }

            // XP Progress bar
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Progression niveau ${state.profile.level}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${state.profile.xpInCurrentLevel} / 500 XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlueLight
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { state.profile.xpProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = ElectricBlue,
                    trackColor = DarkSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // â”€â”€ Account / Email Status Card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, if (state.profile.email != null) NeonGreen.copy(alpha = 0.4f) else ElectricBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (state.profile.email != null) NeonGreenAlpha15 else ElectricBlueAlpha15,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (state.profile.email != null) "âœ‰ï¸" else "ðŸ‘¤", fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (state.profile.email != null) {
                            Text("Compte connectÃ©", style = MaterialTheme.typography.labelSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                            Text(state.profile.email!!, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Mode InvitÃ©", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("Non connectÃ©", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (state.profile.email != null) {
                        // Connected â†’ show disconnect button directly
                        Button(
                            onClick = { viewModel.signOut() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorRed.copy(alpha = 0.15f),
                                contentColor = ErrorRed
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("DÃ©connecter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Not connected â†’ show login button
                        Button(
                            onClick = { viewModel.openAuthDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Connexion", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // â”€â”€ Stats summary â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    emoji = "\uD83D\uDD25",
                    value = state.profile.currentStreak.toString(),
                    label = "Streak",
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    emoji = "\uD83C\uDFC6",
                    value = state.profile.longestStreak.toString(),
                    label = "Meilleur",
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    emoji = "\uD83C\uDFAF",
                    value = "${state.unlockedCount}/${state.achievements.size}",
                    label = "Badges",
                    modifier = Modifier.weight(1f)
                )
                state.profile.bmi?.let { bmi ->
                    ProfileStatCard(
                        emoji = "\u2696\uFE0F",
                        value = "${"%.1f".format(bmi)}",
                        label = "IMC",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // â”€â”€ Editable profile form â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            AnimatedVisibility(
                visible = state.isEditing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle("\u270F\uFE0F Modifier le profil")
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fitMotionTextFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightField,
                            onValueChange = { weightField = it },
                            label = { Text("Poids (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = fitMotionTextFieldColors()
                        )
                        OutlinedTextField(
                            value = heightField,
                            onValueChange = { heightField = it },
                            label = { Text("Taille (cm)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = fitMotionTextFieldColors()
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Objectif",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))

                    // Objective chips
                    ObjectiveType.entries.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { obj ->
                                val selected = obj == selectedObjective
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedObjective = obj },
                                    label = { Text("${obj.emoji} ${obj.labelFr}", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricBlueAlpha15,
                                        selectedLabelColor = ElectricBlueLight
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Language
                    Text(
                        "Langue",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("fr" to "\uD83C\uDDEB\uD83C\uDDF7 Fran\u00e7ais", "en" to "\uD83C\uDDEC\uD83C\uDDE7 English")
                            .forEach { (code, label) ->
                                val sel = code == language
                                FilterChip(
                                    selected = sel,
                                    onClick = { language = code },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricBlueAlpha15,
                                        selectedLabelColor = ElectricBlueLight
                                    )
                                )
                            }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Notifications toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Notifications, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Notifications",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ElectricBlue
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.saveProfile(
                                name = nameField,
                                weightKg = weightField.toFloatOrNull(),
                                heightCm = heightField.toFloatOrNull(),
                                objective = selectedObjective,
                                language = language,
                                notificationsEnabled = notificationsEnabled
                            )
                            LanguageHelper.setAppLanguage(context, language)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sauvegarder", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }

            // â”€â”€ Language Selector Card (Always Accessible) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ðŸŒ", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Langue de l'application / Language",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val currentLang = state.profile.preferredLanguage
                        // French Button
                        val isFr = currentLang.equals("fr", ignoreCase = true)
                        Button(
                            onClick = {
                                viewModel.saveProfile(
                                    name = state.profile.name,
                                    weightKg = state.profile.weightKg,
                                    heightCm = state.profile.heightCm,
                                    objective = state.profile.objectiveType,
                                    language = "fr",
                                    notificationsEnabled = state.profile.notificationsEnabled
                                )
                                LanguageHelper.setAppLanguage(context, "fr")
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFr) NeonGreen else DarkSurfaceVariant,
                                contentColor = if (isFr) Color.Black else TextPrimary
                            ),
                            border = if (isFr) null else BorderStroke(1.dp, DarkOutline)
                        ) {
                            Text("ðŸ‡«ðŸ‡· FranÃ§ais", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // English Button
                        val isEn = currentLang.equals("en", ignoreCase = true)
                        Button(
                            onClick = {
                                viewModel.saveProfile(
                                    name = state.profile.name,
                                    weightKg = state.profile.weightKg,
                                    heightCm = state.profile.heightCm,
                                    objective = state.profile.objectiveType,
                                    language = "en",
                                    notificationsEnabled = state.profile.notificationsEnabled
                                )
                                LanguageHelper.setAppLanguage(context, "en")
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEn) NeonGreen else DarkSurfaceVariant,
                                contentColor = if (isEn) Color.Black else TextPrimary
                            ),
                            border = if (isEn) null else BorderStroke(1.dp, DarkOutline)
                        ) {
                            Text("ðŸ‡¬ðŸ‡§ English", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // â”€â”€ Achievements shortcut â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clickable(onClick = onViewAchievements),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("ðŸ†", fontSize = 22.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Badges & Achievements",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${state.unlockedCount} dÃ©bloquÃ©(s) sur ${state.achievements.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(12.dp))

        }
    }
}

@Composable
private fun ProfileStatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun fitMotionTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricBlue,
    unfocusedBorderColor = DarkOutline,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = ElectricBlueLight,
    unfocusedLabelColor = TextSecondary,
    cursorColor = ElectricBlue
)

@Composable
private fun AuthAccountDialog(
    state: ProfileUiState,
    onEmailChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("âœ‰ï¸ Compte & Synchronisation", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Connectez-vous avec votre adresse email pour sauvegarder vos sÃ©ances, votre programme Coach IA et votre nutrition par profil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // 1. Email input
                OutlinedTextField(
                    value = state.authInputEmail,
                    onValueChange = onEmailChange,
                    label = { Text("Adresse email") },
                    placeholder = { Text("ex: mathieu@gmail.com", color = TextDisabled) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Name input
                OutlinedTextField(
                    value = state.authInputName,
                    onValueChange = onNameChange,
                    label = { Text("Nom ou Pseudo (optionnel)") },
                    placeholder = { Text("ex: Mathieu", color = TextDisabled) },
                    singleLine = true,
                    colors = fitMotionTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                state.authError?.let { err ->
                    Text(err, color = ErrorRed, style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                ) {
                    Icon(Icons.Rounded.Login, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Se connecter / Enregistrer ce compte", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // 3. Saved accounts list
                if (state.savedAccounts.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Comptes enregistrÃ©s sur cet appareil", style = MaterialTheme.typography.labelMedium, color = ElectricBlueLight, fontWeight = FontWeight.SemiBold)

                    state.savedAccounts.forEach { acc ->
                        val isCurrent = acc.email.equals(state.profile.email, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSwitchAccount(acc.email) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) ElectricBlueAlpha15 else DarkSurfaceVariant
                            ),
                            border = if (isCurrent) BorderStroke(1.dp, NeonGreen) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCurrent) NeonGreenAlpha15 else DarkOutline,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (isCurrent) "âœ…" else "ðŸ‘¤", fontSize = 14.sp)
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.displayName.ifBlank { acc.email },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(acc.email, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 11.sp)
                                }
                                if (isCurrent) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonGreenAlpha15,
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text("Actif", style = MaterialTheme.typography.labelSmall, color = NeonGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteAccount(acc.email) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // 4. Sign out button if currently logged in
                if (state.profile.email != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Se dÃ©connecter (Mode InvitÃ©)", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fermer", color = TextSecondary) }
        }
    )
}
