package com.phoenix.fitpro.presentation.ui.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.model.Achievement
import com.phoenix.fitpro.domain.model.AchievementTier
import com.phoenix.fitpro.presentation.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val unlocked = state.achievements.filter { it.isUnlocked }.sortedByDescending { it.unlockedAt }
    val locked = state.achievements.filter { !it.isUnlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "\uD83C\uDFC6 Mes Badges",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy)
            )
        },
        containerColor = DeepNavy
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Progress header
            item {
                Spacer(Modifier.height(8.dp))
                AchievementProgressCard(
                    unlocked = unlocked.size,
                    total = state.achievements.size
                )
                Spacer(Modifier.height(20.dp))
            }

            // Unlocked section
            if (unlocked.isNotEmpty()) {
                item {
                    SectionHeader("\u2705 D\u00e9bloqu\u00e9s (${unlocked.size})")
                    Spacer(Modifier.height(8.dp))
                }
                itemsIndexed(unlocked) { index, achievement ->
                    AchievementCard(
                        achievement = achievement,
                        unlocked = true,
                        animDelay = index * 60
                    )
                    Spacer(Modifier.height(8.dp))
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // Locked section
            if (locked.isNotEmpty()) {
                item {
                    SectionHeader("\uD83D\uDD12 Verrouill\u00e9s (${locked.size})")
                    Spacer(Modifier.height(8.dp))
                }
                itemsIndexed(locked) { index, achievement ->
                    AchievementCard(
                        achievement = achievement,
                        unlocked = false,
                        animDelay = index * 40
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AchievementProgressCard(unlocked: Int, total: Int) {
    val progress = if (total > 0) unlocked.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$unlocked / $total",
                style = MaterialTheme.typography.displaySmall,
                color = GoldColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "badges d\u00e9bloqu\u00e9s",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = GoldColor,
                trackColor = DarkSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% compl\u00e9t\u00e9",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = TextSecondary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    unlocked: Boolean,
    animDelay: Int = 0
) {
    val tierColor = when (achievement.tier) {
        AchievementTier.GOLD   -> GoldColor
        AchievementTier.SILVER -> SilverColor
        AchievementTier.BRONZE -> BronzeColor
    }
    val tierLabel = when (achievement.tier) {
        AchievementTier.GOLD   -> "Or"
        AchievementTier.SILVER -> "Argent"
        AchievementTier.BRONZE -> "Bronze"
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(400)) + fadeIn(tween(400))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (unlocked) DarkSurface else DarkSurface.copy(alpha = 0.5f)
            ),
            border = if (unlocked) BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)) else null
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (unlocked)
                                Brush.radialGradient(listOf(tierColor.copy(alpha = 0.3f), tierColor.copy(alpha = 0.1f)))
                            else
                                Brush.radialGradient(listOf(DarkSurfaceVariant, DarkSurfaceVariant))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unlocked) achievement.iconEmoji else "\uD83D\uDD12",
                        fontSize = 26.sp
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = achievement.titleFr,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (unlocked) TextPrimary else TextDisabled,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(6.dp))
                        // Tier badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tierColor.copy(alpha = if (unlocked) 0.2f else 0.05f)
                        ) {
                            Text(
                                text = tierLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (unlocked) tierColor else TextDisabled,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        text = achievement.descriptionFr,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unlocked) TextSecondary else TextDisabled,
                        maxLines = 2
                    )
                    if (unlocked && achievement.unlockedAt != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "\uD83D\uDCC5 ${achievement.unlockedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tierColor.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // XP reward
                if (achievement.xpReward > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (unlocked) ElectricBlueAlpha15 else DarkSurfaceVariant
                    ) {
                        Text(
                            text = "+${achievement.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (unlocked) ElectricBlueLight else TextDisabled,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
