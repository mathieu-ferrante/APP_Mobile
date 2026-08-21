package com.phoenix.fitpro.presentation.ui.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GradientStart.copy(alpha = 0.2f), DeepNavy.copy(alpha = 0f))
                    )
                )
                .padding(top = 52.dp, start = 16.dp, end = 16.dp, bottom = 20.dp)
        ) {
            Column {
                Text(
                    text = "\uD83D\uDCCA Statistiques",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Suis ta progression",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Period selector
        PeriodSelector(
            selected = state.selectedPeriod,
            onSelect = viewModel::selectPeriod,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Key metrics row 1
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                emoji = "\uD83D\uDCAA",
                value = state.totalWorkouts.toString(),
                label = "Total s\u00e9ances",
                color = ElectricBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                emoji = "\uD83D\uDD25",
                value = state.currentStreak.toString(),
                label = "Streak actuel",
                color = FireOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                emoji = "\uD83C\uDFC6",
                value = state.bestStreak.toString(),
                label = "Meilleur streak",
                color = GoldColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Key metrics row 2
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                emoji = "\uD83D\uDCC5",
                value = state.weeklyWorkouts.toString(),
                label = "Cette semaine",
                color = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                emoji = "\uD83D\uDDD3\uFE0F",
                value = state.monthlyWorkouts.toString(),
                label = "Ce mois",
                color = GradientMid,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                emoji = "\uD83C\uDFCB\uFE0F",
                value = run {
                    val t = state.totalVolumeKg
                    if (t >= 1000f) "${"%.1f".format(t / 1000f)}t" else "${t.toInt()}kg"
                },
                label = "Volume total",
                color = SilverColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Workout frequency chart
        ChartCard(
            title = "\uD83D\uDDD3\uFE0F Fr\u00e9quence d\u2019entra\u00eenement",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (state.workoutFrequency.isEmpty()) {
                EmptyChartPlaceholder("Pas encore de donn\u00e9es d\u2019entra\u00eenement")
            } else {
                BarChart(
                    data = state.workoutFrequency,
                    barColor = ElectricBlue,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Weekly calories chart
        ChartCard(
            title = "\uD83D\uDD25 Calories (7 derniers jours)",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (state.weeklyCalories.isEmpty() || state.weeklyCalories.all { it.second == 0 }) {
                EmptyChartPlaceholder("Aucun repas enregistr\u00e9 cette semaine")
            } else {
                BarChart(
                    data = state.weeklyCalories,
                    barColor = AccentOrange,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Weight evolution chart
        ChartCard(
            title = "\u2696\uFE0F \u00c9volution du poids",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (state.weightEntries.size < 2) {
                EmptyChartPlaceholder("Ajoute plusieurs pes\u00e9es pour voir l\u2019\u00e9volution")
            } else {
                LineChart(
                    data = state.weightEntries,
                    lineColor = NeonGreen,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PeriodSelector(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatsPeriod.values().forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) ElectricBlue else Color.Transparent)
                    .clickable { onSelect(period) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.labelFr,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun StatCard(
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
            Text(text = emoji, fontSize = 22.sp)
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
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BarChart(
    data: List<Pair<LocalDate, Int>>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOf { it.second }.coerceAtLeast(1).toFloat()
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_anim"
    )
    val formatter = DateTimeFormatter.ofPattern("dd/MM")

    Canvas(modifier = modifier) {
        val barCount = data.size
        val totalWidth = size.width
        val totalHeight = size.height - 20.dp.toPx()
        val barWidth = (totalWidth / barCount) * 0.6f
        val barSpacing = totalWidth / barCount

        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value / maxVal) * totalHeight * animProgress
            val left = index * barSpacing + (barSpacing - barWidth) / 2f
            val top = totalHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                    startY = top,
                    endY = top + barHeight * 0.4f
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        data.forEach { (date, _) ->
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LineChart(
    data: List<Pair<LocalDate, Float>>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val minVal = data.minOf { it.second }
    val maxVal = data.maxOf { it.second }
    val range = (maxVal - minVal).coerceAtLeast(0.5f)
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "line_anim"
    )
    val formatter = DateTimeFormatter.ofPattern("dd/MM")

    Canvas(modifier = modifier) {
        val pointCount = (data.size * animProgress).toInt().coerceAtLeast(2)
        val visibleData = data.take(pointCount)
        val totalHeight = size.height - 20.dp.toPx()
        val stepX = size.width / (data.size - 1).coerceAtLeast(1).toFloat()

        val points = visibleData.mapIndexed { index, (_, value) ->
            Offset(
                x = index * stepX,
                y = totalHeight - ((value - minVal) / range) * totalHeight
            )
        }

        if (points.size >= 2) {
            val fillPath = Path().apply {
                moveTo(points.first().x, totalHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, totalHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f, endY = totalHeight
                )
            )
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            points.forEach { point ->
                drawCircle(color = lineColor, radius = 5.dp.toPx(), center = point)
                drawCircle(color = DeepNavy, radius = 3.dp.toPx(), center = point)
            }
        }
    }

    val step = (data.size / 5).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.filterIndexed { index, _ -> index % step == 0 || index == data.size - 1 }
            .forEach { (date, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${"%.1f".format(value)}kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        fontSize = 9.sp
                    )
                    Text(
                        text = date.format(formatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
    }
}
