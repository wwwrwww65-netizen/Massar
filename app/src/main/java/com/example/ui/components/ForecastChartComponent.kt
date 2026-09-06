package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ForecastPoint
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import kotlin.math.max
import kotlin.math.min

@Composable
fun ForecastChartComponent(
    points: List<ForecastPoint>,
    currencySymbol: String,
    title: String = "محاكاة التدفق والسيولة المتوقعة (12 شهراً)",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChartLegendItem(color = MasarEmerald, label = "المتفائل (Best)")
                ChartLegendItem(color = MasarSky, label = "الأساسي (Base)")
                ChartLegendItem(color = MasarRose, label = "المتشائم (Worst)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد بيانات توقع كافية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Draw dynamic multi-line chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val allValues = points.flatMap { listOf(it.baseCash, it.bestCash, it.worstCash) }
                    val minVal = min(0.0, allValues.minOrNull() ?: 0.0)
                    val maxVal = max(100.0, allValues.maxOrNull() ?: 1000.0)
                    val range = max(1.0, maxVal - minVal)

                    val xStep = w / max(1, points.size - 1)

                    // Draw zero baseline if minVal < 0
                    if (minVal < 0) {
                        val zeroY = (h - ((0.0 - minVal) / range * h)).toFloat()
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.4f),
                            start = Offset(0f, zeroY),
                            end = Offset(w, zeroY),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    // Best Case Path
                    val bestPath = Path()
                    points.forEachIndexed { i, pt ->
                        val x = i * xStep
                        val y = (h - ((pt.bestCash - minVal) / range * h)).toFloat()
                        if (i == 0) bestPath.moveTo(x, y) else bestPath.lineTo(x, y)
                    }
                    drawPath(
                        path = bestPath,
                        color = MasarEmerald,
                        style = Stroke(width = 4f)
                    )

                    // Base Case Path
                    val basePath = Path()
                    points.forEachIndexed { i, pt ->
                        val x = i * xStep
                        val y = (h - ((pt.baseCash - minVal) / range * h)).toFloat()
                        if (i == 0) basePath.moveTo(x, y) else basePath.lineTo(x, y)
                    }
                    drawPath(
                        path = basePath,
                        color = MasarSky,
                        style = Stroke(width = 6f)
                    )

                    // Worst Case Path
                    val worstPath = Path()
                    points.forEachIndexed { i, pt ->
                        val x = i * xStep
                        val y = (h - ((pt.worstCash - minVal) / range * h)).toFloat()
                        if (i == 0) worstPath.moveTo(x, y) else worstPath.lineTo(x, y)
                    }
                    drawPath(
                        path = worstPath,
                        color = MasarRose,
                        style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )

                    // Points on base path
                    points.forEachIndexed { i, pt ->
                        if (i % 2 == 0 || i == points.size - 1) {
                            val x = i * xStep
                            val y = (h - ((pt.baseCash - minVal) / range * h)).toFloat()
                            drawCircle(color = MasarSky, radius = 5f, center = Offset(x, y))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // X-Axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الآن", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("3 أشهر", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("6 أشهر", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("12 شهراً", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
