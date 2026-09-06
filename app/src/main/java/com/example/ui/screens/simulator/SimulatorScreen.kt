package com.example.ui.screens.simulator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.ScenarioEngine
import com.example.domain.model.ScenarioParameters
import com.example.ui.components.ForecastChartComponent
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.components.RiskBadge
import com.example.ui.components.SimpleDecisionReportCard
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState

/**
 * Redesigned Simulator Screen:
 * Simple 3-step natural workflow:
 * 1. "ما هو القرار الذي تفكر فيه؟" (Text or Predefined Chips)
 * 2. Confirmed extracted parameters in plain language (with optional adjust)
 * 3. Immediate "Bottom-Line" results + optional chart and deep Decision Intelligence report.
 */
@Composable
fun SimulatorScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit,
    onSimulateScenario: (ScenarioParameters) -> Unit,
    onParseNaturalLanguage: (String) -> Unit,
    onSaveScenario: () -> Unit
) {
    MasarRtlProvider {
        var nlpText by remember { mutableStateOf("") }
        var showCustomizer by remember { mutableStateOf(false) }
        var showChart by remember { mutableStateOf(false) }

        val active = state.activeScenario
        val result = state.activeSimulationResult

        var initialCost by remember(active) { mutableStateOf(active.initialCost.toString()) }
        var monthlyExp by remember(active) { mutableStateOf(active.recurringMonthlyExpense.toString()) }
        var monthlyRev by remember(active) { mutableStateOf(active.recurringMonthlyRevenue.toString()) }
        var duration by remember(active) { mutableStateOf(active.durationMonths.toFloat()) }
        var growthRate by remember(active) { mutableStateOf(active.growthRateMonthly.toFloat()) }

        Scaffold(
            topBar = {
                MasarHeader(
                    title = "تجربة القرارات المالية",
                    subtitle = "اكتشف نتيجة أي قرار قبل أن تدفع ريالاً واحداً",
                    currentScreen = AppScreen.SIMULATOR,
                    onNavigate = onNavigate
                )
            },
            bottomBar = {
                MasarBottomNavigation(
                    currentScreen = AppScreen.SIMULATOR,
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
                // Step 1: Natural Language Conversational Input Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MasarSky.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MasarSky, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = "صف قرارك أو ما تفكر بشرائه:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "المحرك الذكي سيفهم الأرقام ويحللها تلقائياً",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = nlpText,
                                onValueChange = { nlpText = it },
                                placeholder = { Text("مثال: أريد توظيف مبرمج بـ 800$ شهرياً وأتوقع دخل إضافي 1500$...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (nlpText.isNotBlank()) {
                                                onParseNaturalLanguage(nlpText)
                                                nlpText = ""
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "محاكاة", tint = MasarEmerald)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Or choose from popular decisions
                            Text(
                                text = "أو اختر قراراً شائعاً لتجربته بنقرة واحدة:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val templates = ScenarioEngine.getPredefinedTemplates()
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(templates) { template ->
                                    val isSelected = active.name == template.name
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { onSimulateScenario(template) }
                                    ) {
                                        Text(
                                            text = template.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 2: Active Scenario Overview & Optional Tweaks
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "القرار قيد التجربة: ${active.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "تكلفة أولية: ${active.initialCost.toInt()} ${state.currencySymbol} | شهري: +${active.recurringMonthlyRevenue.toInt()} / -${active.recurringMonthlyExpense.toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { showCustomizer = !showCustomizer }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = MasarSky, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = if (showCustomizer) "إغلاق" else "تعديل الأرقام",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Expandable sliders/fields only if user clicks "تعديل الأرقام"
                            AnimatedVisibility(visible = showCustomizer) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = initialCost,
                                            onValueChange = { initialCost = it },
                                            label = { Text("تكلفة أولية") },
                                            suffix = { Text(state.currencySymbol) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = monthlyExp,
                                            onValueChange = { monthlyExp = it },
                                            label = { Text("مصروف شهري") },
                                            suffix = { Text(state.currencySymbol) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = monthlyRev,
                                            onValueChange = { monthlyRev = it },
                                            label = { Text("إيراد شهري متوقع") },
                                            suffix = { Text(state.currencySymbol) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "فترة المحاكاة: ${duration.toInt()} أشهر",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Slider(
                                        value = duration,
                                        onValueChange = { duration = it },
                                        valueRange = 3f..36f,
                                        steps = 33
                                    )

                                    Button(
                                        onClick = {
                                            val updated = active.copy(
                                                initialCost = initialCost.toDoubleOrNull() ?: 0.0,
                                                recurringMonthlyExpense = monthlyExp.toDoubleOrNull() ?: 0.0,
                                                recurringMonthlyRevenue = monthlyRev.toDoubleOrNull() ?: 0.0,
                                                durationMonths = duration.toInt(),
                                                growthRateMonthly = growthRate.toDouble()
                                            )
                                            onSimulateScenario(updated)
                                            showCustomizer = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text("تطبيق المحاكاة بالأرقام الجديدة", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 3: Simple, Direct Outcome Card (Bottom Line)
                if (result != null) {
                    item {
                        val runwayChange = result.projectedRunwayMonths - state.metrics.runwayMonths
                        val runwayImpactText = if (result.projectedRunwayMonths >= 900) {
                            "يحافظ على استدامة أموالك بالكامل"
                        } else if (runwayChange >= 0) {
                            "يمد فترة أمانك المالي بمقدار +${String.format("%.0f", runwayChange)} شهر"
                        } else {
                            "يقلص فترة أمانك المالي بمقدار ${String.format("%.0f", -runwayChange)} شهر"
                        }

                        val isPositive = result.projectedCashAfterDuration >= state.metrics.currentBalance

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "النتيجة المباشرة للقرار:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    RiskBadge(level = result.riskLevel)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Simple Human Highlights
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Cash Impact
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("الرصيد بعد ${result.scenario.durationMonths} شهراً", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = String.format("%,.0f %s", result.projectedCashAfterDuration, state.currencySymbol),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPositive) MasarEmerald else MasarRose
                                            )
                                        }
                                    }

                                    // Safety duration Impact
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("فترة أمان أموالك", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (result.projectedRunwayMonths >= 900) "مستدامة (+)" else "${String.format("%.0f", result.projectedRunwayMonths)} شهر",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (result.projectedRunwayMonths >= 12) MasarEmerald else MasarRose
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Recovery & ROI Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("استرداد التكلفة بعد", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (result.recoveryPointMonth > 0) "${result.recoveryPointMonth} أشهر" else "فوري",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MasarGold
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("العائد التقديري", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${String.format("%.0f", result.roiPercentage)}%",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (result.roiPercentage >= 20) MasarEmerald else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Chart Toggle for Progressive Disclosure
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showChart = !showChart }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (showChart) "إخفاء الرسم البياني للمسار" else "عرض رسم مسار السيولة عبر الزمن",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = if (showChart) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                AnimatedVisibility(visible = showChart) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        ForecastChartComponent(
                                            points = result.forecastTimeline,
                                            currencySymbol = state.currencySymbol,
                                            title = "مسار الرصيد الشهري المتوقع"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Step 4: Simple Decision Intelligence Report
                    if (state.activeDecisionAnalysis != null) {
                        item {
                            SimpleDecisionReportCard(
                                analysis = state.activeDecisionAnalysis,
                                currencySymbol = state.currencySymbol
                            )
                        }
                    }

                    // Save scenario CTA
                    item {
                        Button(
                            onClick = onSaveScenario,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("حفظ هذا القرار في سجلي لمراجعته لاحقاً", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
