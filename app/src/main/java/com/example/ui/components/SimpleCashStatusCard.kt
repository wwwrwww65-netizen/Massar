package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.RiskLevel
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky

/**
 * Clean, human-friendly Cash & Runway Status Card with Progressive Disclosure.
 * Shows high-level health first; expands for technical details on user demand.
 */
@Composable
fun SimpleCashStatusCard(
    metrics: FinancialMetrics,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val runwayText = if (metrics.runwayMonths >= 900) {
        "أموالك تغطي نفقاتك بالكامل ومستدامة"
    } else if (metrics.runwayMonths >= 12) {
        "أموالك تكفيك لمدة ${String.format("%.0f", metrics.runwayMonths)} شهراً بأمان"
    } else if (metrics.runwayMonths >= 6) {
        "أموالك تكفيك لمدة ${String.format("%.0f", metrics.runwayMonths)} شهراً (انتبه للسيولة)"
    } else {
        "تنبيه: السيولة المتاحة تكفيك لمدة ${String.format("%.1f", metrics.runwayMonths)} شهراً فقط"
    }

    val statusColor = when (metrics.overallRiskLevel) {
        RiskLevel.LOW -> MasarEmerald
        RiskLevel.MODERATE -> MasarGold
        RiskLevel.HIGH -> Color(0xFFF97316)
        RiskLevel.CRITICAL -> MasarRose
    }

    val healthSummary = when (metrics.overallRiskLevel) {
        RiskLevel.LOW -> "وضعك المالي ممتاز ومستقر"
        RiskLevel.MODERATE -> "وضعك المالي جيد، يحتاج متابعة بسيطة"
        RiskLevel.HIGH -> "هناك ضغط على السيولة يتطلب ترشيداً"
        RiskLevel.CRITICAL -> "وضعك المالي حرج ويتطلب تدخلاً فورياً"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Status title + Human Health Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "الرصيد المتاح",
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "رصيدك الحالي المتاح",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = healthSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                RiskBadge(level = metrics.overallRiskLevel)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Amount Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = String.format("%,.2f", metrics.currentBalance),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direct, Human-Readable Runway Statement Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = runwayText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Essential Metrics (Monthly In, Monthly Out, Net Savings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubMetricBox(
                    title = "يدخل شهرياً",
                    amount = String.format("%,.0f %s", metrics.totalMonthlyRevenue, currencySymbol),
                    icon = Icons.Default.ArrowUpward,
                    accentColor = MasarEmerald,
                    modifier = Modifier.weight(1f)
                )

                SubMetricBox(
                    title = "يخرج شهرياً",
                    amount = String.format("%,.0f %s", metrics.totalMonthlyExpenses, currencySymbol),
                    icon = Icons.Default.ArrowDownward,
                    accentColor = MasarRose,
                    modifier = Modifier.weight(1f)
                )

                SubMetricBox(
                    title = if (metrics.netMonthlyCashFlow >= 0) "الفائض الشهري" else "العجز الشهري",
                    amount = String.format("%+,.0f %s", metrics.netMonthlyCashFlow, currencySymbol),
                    icon = if (metrics.netMonthlyCashFlow >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    accentColor = if (metrics.netMonthlyCashFlow >= 0) MasarEmerald else MasarRose,
                    modifier = Modifier.weight(1f)
                )
            }

            // Progressive Disclosure Toggle
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "إخفاء التفاصيل المالية المتقدمة" else "عرض التفاصيل والمؤشرات الفنية (Runway / Burn Rate)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    val progress = (metrics.runwayMonths / 24.0).coerceIn(0.0, 1.0).toFloat()
                    Text(
                        text = "مؤشر مدة البقاء المالي (Runway Gauge):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "معدل الحرق الصافي: ${String.format("%,.0f", metrics.burnRate)} $currencySymbol/شهر",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (metrics.burnRate > 0) MasarRose else MasarEmerald
                        )
                        Text(
                            text = "مؤشر الأمان: +24 شهر",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
