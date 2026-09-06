package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.components.RiskBadge
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit
) {
    MasarRtlProvider {
        Scaffold(
            topBar = {
                MasarHeader(
                    title = "التقارير وسجل التدقيق والامتثال",
                    subtitle = "التقرير التنفيذي الشامل وسجل العمليات للأمان والشفافية",
                    currentScreen = AppScreen.REPORTS,
                    onNavigate = onNavigate
                )
            },
            bottomBar = {
                MasarBottomNavigation(
                    currentScreen = AppScreen.REPORTS,
                    onNavigate = onNavigate
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Executive Financial Intelligence Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Assessment, contentDescription = null, tint = MasarEmerald)
                                    Text(
                                        text = "التقرير التنفيذي لسلامة القرار",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                RiskBadge(level = state.metrics.overallRiskLevel)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ReportDataRow("الرصيد النقدي الفعلي", "${String.format("%,.2f", state.metrics.currentBalance)} ${state.currencySymbol}")
                            ReportDataRow("الإيرادات الشهرية المتكررة", "${String.format("%,.2f", state.metrics.totalMonthlyRevenue)} ${state.currencySymbol}")
                            ReportDataRow("المصروفات الشهرية التشغيلية", "${String.format("%,.2f", state.metrics.totalMonthlyExpenses)} ${state.currencySymbol}")
                            ReportDataRow("صافي التدفق الشهري", "${String.format("%+,.2f", state.metrics.netMonthlyCashFlow)} ${state.currencySymbol}")
                            ReportDataRow("فترة الاستدامة (Runway)", if (state.metrics.runwayMonths >= 900) "مستدامة وفائضة" else "${String.format("%.1f", state.metrics.runwayMonths)} شهر")
                            ReportDataRow("نقطة التعادل التشغيلي", "${String.format("%,.2f", state.metrics.breakEvenRevenue)} ${state.currencySymbol}/شهر")
                            ReportDataRow("نسبة التكاليف الثابتة", "${String.format("%.1f", state.metrics.fixedExpensesRatio)}%")
                        }
                    }
                }

                // Risk Breakdown Matrix
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = MasarSky)
                                Text(
                                    text = "مصفوفة تحليل المخاطر التفصيلية:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            RiskMetricRow(title = "مخاطر السيولة المباشرة", score = state.riskBreakdown.liquidityRiskScore, level = state.riskBreakdown.liquidityRisk)
                            RiskMetricRow(title = "مخاطر قصر الـ Runway", score = state.riskBreakdown.runwayRiskScore, level = state.riskBreakdown.runwayRisk)
                            RiskMetricRow(title = "مخاطر ارتفاع التكاليف الثابتة", score = state.riskBreakdown.fixedCostRiskScore, level = state.riskBreakdown.fixedCostRisk)
                            RiskMetricRow(title = "مخاطر تركز الإيرادات والعملاء", score = state.riskBreakdown.concentrationRiskScore, level = state.riskBreakdown.concentrationRisk)
                        }
                    }
                }

                // Audit Log & Compliance Section
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MasarGold)
                        Text(
                            text = "سجل التدقيق والعمليات (Audit Log):",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (state.auditLogs.isEmpty()) {
                    item {
                        Text(
                            text = "لا توجد سجلات مسجلة بعد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(state.auditLogs.take(15)) { log ->
                        val dateFormatted = try {
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                        } catch (e: Exception) {
                            ""
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = log.actionName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text(text = log.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = dateFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun ReportDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RiskMetricRow(title: String, score: Int, level: com.example.domain.model.RiskLevel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "$score/100", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            RiskBadge(level = level)
        }
    }
}
