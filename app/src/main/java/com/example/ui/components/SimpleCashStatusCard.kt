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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BusinessType
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.RiskLevel
import com.example.domain.model.UserProfile
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky

/**
 * Clean, Accessible Cash & Health Card with explicit status text + colors.
 * Adapts labels dynamically based on User Profile (Employee vs Business).
 */
@Composable
fun SimpleCashStatusCard(
    profile: UserProfile,
    metrics: FinancialMetrics,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isEmployee = profile.businessType == BusinessType.EMPLOYEE

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

    // Explicit Textual Status for Accessibility & Clarity (Never rely on color alone)
    val healthSummary = when (metrics.overallRiskLevel) {
        RiskLevel.LOW -> "وضعك المالي مستقر ومطمئن"
        RiskLevel.MODERATE -> "يحتاج متابعة وترشيد بسيط"
        RiskLevel.HIGH -> "ضغط مرتفع على السيولة"
        RiskLevel.CRITICAL -> "وضع حرج يتطلب تدخلاً فورياً"
    }

    val statusBadgeTitle = when (metrics.overallRiskLevel) {
        RiskLevel.LOW -> "وضع مستقر"
        RiskLevel.MODERATE -> "يحتاج متابعة"
        RiskLevel.HIGH -> "ضغط سيولة"
        RiskLevel.CRITICAL -> "حرج جداً"
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
            // Header: Status title + Explicit Text Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            text = if (isEmployee) "الرصيد والمدخرات المتاحة" else "السيولة النقدية المتاحة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = healthSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Explicit Status Badge with clear text
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusBadgeTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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

            // 3 Essential Adapted Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubMetricBox(
                    title = if (isEmployee) "الراتب / الدخل" else "الإيراد الشهري",
                    amount = String.format("%,.0f %s", metrics.totalMonthlyRevenue, currencySymbol),
                    icon = Icons.Default.ArrowUpward,
                    accentColor = MasarEmerald,
                    modifier = Modifier.weight(1f)
                )

                SubMetricBox(
                    title = if (isEmployee) "المصاريف المعيشية" else "تكاليف التشغيل",
                    amount = String.format("%,.0f %s", metrics.totalMonthlyExpenses, currencySymbol),
                    icon = Icons.Default.ArrowDownward,
                    accentColor = MasarRose,
                    modifier = Modifier.weight(1f)
                )

                SubMetricBox(
                    title = if (isEmployee) {
                        if (metrics.netMonthlyCashFlow >= 0) "فائض الادخار" else "العجز الشهري"
                    } else {
                        if (metrics.netMonthlyCashFlow >= 0) "صافي الربح" else "صافي العجز"
                    },
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
