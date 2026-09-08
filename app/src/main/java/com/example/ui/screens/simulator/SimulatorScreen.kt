package com.example.ui.screens.simulator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.ScenarioEngine
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.RiskLevel
import com.example.domain.model.ScenarioParameters
import com.example.domain.model.ScenarioResult
import com.example.ui.components.ForecastChartComponent
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.components.RiskBadge
import com.example.ui.components.SimpleDecisionReportCard
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarPurple
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState

/**
 * World-class Fintech Experience for the Decision Simulator.
 * Features high-contrast container identity, distinct color palettes,
 * crystal-clear visual hierarchy, and intuitive Before/After comparisons.
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
        var showMetricExplanations by remember { mutableStateOf(false) }

        val active = state.activeScenario
        val result = state.activeSimulationResult

        // Form state for fine-tuning
        var initialCost by remember(active) { mutableStateOf(active.initialCost.toInt().toString()) }
        var monthlyExp by remember(active) { mutableStateOf(active.recurringMonthlyExpense.toInt().toString()) }
        var monthlyRev by remember(active) { mutableStateOf(active.recurringMonthlyRevenue.toInt().toString()) }
        var duration by remember(active) { mutableFloatStateOf(active.durationMonths.toFloat()) }
        var growthRate by remember(active) { mutableFloatStateOf(active.growthRateMonthly.toFloat()) }

        // Quick prompts for natural language simulation
        val samplePrompts = listOf(
            "💻 شراء لابتوب للعمل بـ 1200$ كاش",
            "👥 توظيف مسوق بـ 600$ شهرياً وأتوقع دخل 1400$",
            "📣 حملة إعلانات بـ 400$ لزيادة المبيعات",
            "📉 تقليل المصاريف والاشتراكات بنسبة 20%"
        )

        Scaffold(
            topBar = {
                MasarHeader(
                    title = "محاكي القرارات المالية",
                    subtitle = "اكتشف نتيجة أي قرار قبل أن تنفق قرشاً واحداً",
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // CONTAINER 1: SMART INPUT & FAST PRESETS (PURPLE / INDIGO ACCENT)
                // ==========================================
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.5.dp, MasarPurple.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MasarPurple.copy(alpha = 0.08f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            // Section Header with Distinct Glow Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MasarPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MasarPurple,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "صف قرارك باللغة الطبيعية",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "المحرك الذكي يستخرج التكاليف والمدة ويحسب النتيجة تلقائياً",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Text Input Field with High Contrast
                            OutlinedTextField(
                                value = nlpText,
                                onValueChange = { nlpText = it },
                                placeholder = {
                                    Text(
                                        "اكتب أي فكرة: مثلاً «شراء معدات بـ 2000$ وأتوقع دخل شهري 600$»...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MasarPurple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (nlpText.isNotBlank()) {
                                                onParseNaturalLanguage(nlpText)
                                                nlpText = ""
                                            }
                                        },
                                        enabled = nlpText.isNotBlank()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (nlpText.isNotBlank()) MasarPurple else MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Send,
                                                contentDescription = "محاكاة",
                                                tint = if (nlpText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Clickable sample prompt chips with distinct styling
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(samplePrompts) { prompt ->
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = MasarPurple.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, MasarPurple.copy(alpha = 0.25f)),
                                        modifier = Modifier.clickable {
                                            onParseNaturalLanguage(prompt)
                                        }
                                    ) {
                                        Text(
                                            text = prompt,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // One-Tap Popular Decision Templates
                            Text(
                                text = "أو اختر قراراً جاهزاً بنقرة واحدة:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val templates = ScenarioEngine.getPredefinedTemplates()
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(templates) { template ->
                                    val isSelected = active.name == template.name
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MasarEmerald else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MasarEmerald else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.clickable { onSimulateScenario(template) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text(
                                                text = template.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // CONTAINER 2: ACTIVE SCENARIO & CUSTOMIZER (SKY BLUE ACCENT)
                // ==========================================
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.5.dp, MasarSky.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MasarSky.copy(alpha = 0.07f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp)
                                .animateContentSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MasarSky.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Timeline,
                                            contentDescription = null,
                                            tint = MasarSky,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "القرار قيد التحليل:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = active.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MasarSky
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "تكلفة أولية: ${String.format("%,.0f", active.initialCost)} ${state.currencySymbol} • شهري: +${String.format("%,.0f", active.recurringMonthlyRevenue)} / -${String.format("%,.0f", active.recurringMonthlyExpense)} ${state.currencySymbol}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (showCustomizer) MasarSky else MasarSky.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, MasarSky),
                                    modifier = Modifier.clickable { showCustomizer = !showCustomizer }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = if (showCustomizer) Color.White else MasarSky,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (showCustomizer) "إخفاء" else "تعديل الأرقام",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (showCustomizer) Color.White else MasarSky
                                        )
                                    }
                                }
                            }

                            // Expandable sliders & numeric inputs
                            AnimatedVisibility(visible = showCustomizer) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "تخصيص مدخلات القرار بدقة:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = initialCost,
                                            onValueChange = { initialCost = it },
                                            label = { Text("تكلفة أولية (كاش)", fontSize = 12.sp) },
                                            suffix = { Text(state.currencySymbol, fontSize = 12.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MasarSky,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                        OutlinedTextField(
                                            value = monthlyExp,
                                            onValueChange = { monthlyExp = it },
                                            label = { Text("مصروف شهري متكرر", fontSize = 12.sp) },
                                            suffix = { Text(state.currencySymbol, fontSize = 12.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MasarSky,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = monthlyRev,
                                            onValueChange = { monthlyRev = it },
                                            label = { Text("إيراد شهري إضافي", fontSize = 12.sp) },
                                            suffix = { Text(state.currencySymbol, fontSize = 12.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MasarSky,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مدة المحاكاة:",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MasarSky.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${duration.toInt()} شهراً",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MasarSky,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Slider(
                                        value = duration,
                                        onValueChange = { duration = it },
                                        valueRange = 3f..36f,
                                        steps = 33,
                                        colors = SliderDefaults.colors(thumbColor = MasarSky, activeTrackColor = MasarSky)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

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
                                        colors = ButtonDefaults.buttonColors(containerColor = MasarSky),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text("تطبيق المحاكاة بالأرقام المعدلة", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // CONTAINER 3: THE DIRECT VERDICT (HERO RESULT)
                // ==========================================
                if (result != null) {
                    item {
                        DirectVerdictCard(
                            result = result,
                            baseMetrics = state.metrics,
                            currencySymbol = state.currencySymbol
                        )
                    }

                    // ==========================================
                    // CONTAINER 4: BEFORE VS AFTER COMPARISON MATRIX
                    // ==========================================
                    item {
                        BeforeAfterComparisonCard(
                            baseMetrics = state.metrics,
                            result = result,
                            currencySymbol = state.currencySymbol
                        )
                    }

                    // ==========================================
                    // CONTAINER 5: 3 DISTINCT KEY INDICATOR MINI-CARDS
                    // ==========================================
                    item {
                        KeyIndicatorsCard(
                            result = result,
                            baseMetrics = state.metrics,
                            currencySymbol = state.currencySymbol,
                            showExplanations = showMetricExplanations,
                            onToggleExplanations = { showMetricExplanations = !showMetricExplanations }
                        )
                    }

                    // ==========================================
                    // CONTAINER 6: CASHFLOW TIMELINE FORECAST CHART
                    // ==========================================
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showChart = !showChart },
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
                                                .background(MasarSky.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShowChart,
                                                contentDescription = null,
                                                tint = MasarSky,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "مسار السيولة عبر الأشهر (الرسم البياني)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "توقع رصيدك شهراً بشهر حتى نهاية فترة القرار",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (showChart) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                AnimatedVisibility(visible = showChart) {
                                    Column(modifier = Modifier.padding(top = 16.dp)) {
                                        ForecastChartComponent(
                                            points = result.forecastTimeline,
                                            currencySymbol = state.currencySymbol,
                                            title = "حركة الرصيد الصافي المتوقع"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // CONTAINER 7: STRATEGIC DECISION REPORT & ALTERNATIVES
                    // ==========================================
                    if (state.activeDecisionAnalysis != null) {
                        item {
                            SimpleDecisionReportCard(
                                analysis = state.activeDecisionAnalysis,
                                currencySymbol = state.currencySymbol
                            )
                        }
                    }

                    // ==========================================
                    // CONTAINER 8: ACTION BUTTONS (SAVE & ASK ADVISOR)
                    // ==========================================
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Primary: Save Decision
                            Button(
                                onClick = onSaveScenario,
                                colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("حفظ هذا القرار في سجل القرارات لمراجعته", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            // Secondary: Consult Advisor about this decision
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MasarSky.copy(alpha = 0.1f),
                                border = BorderStroke(1.5.dp, MasarSky),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate(AppScreen.ADVISOR) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = MasarSky, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("استشر «مرشد مسار» حول هذا القرار بالدردشة", color = MasarSky, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

/**
 * Direct Verdict Card: Gives an immediate, high-contrast, crystal-clear verdict
 * on whether the decision is Safe, Conditional, or High Risk.
 */
@Composable
private fun DirectVerdictCard(
    result: ScenarioResult,
    baseMetrics: FinancialMetrics,
    currencySymbol: String
) {
    val isSafe = result.riskLevel == RiskLevel.LOW
    val isWarning = result.riskLevel == RiskLevel.MODERATE || result.riskLevel == RiskLevel.HIGH
    val isCritical = result.riskLevel == RiskLevel.CRITICAL

    val verdictColor = when {
        isSafe -> MasarEmerald
        isWarning -> MasarGold
        else -> MasarRose
    }

    val verdictTitle = when {
        isSafe -> "القرار آمن وموصى به ✅"
        isWarning -> "مقبول بشروط ومخاطرة متوسطة ⚠️"
        else -> "عالي المخاطر وغير آمن حالياً ⛔"
    }

    val verdictIcon: ImageVector = when {
        isSafe -> Icons.Default.CheckCircle
        isWarning -> Icons.Default.Warning
        else -> Icons.Default.Shield
    }

    // Direct plain explanation
    val netCashChange = result.projectedCashAfterDuration - baseMetrics.currentBalance
    val explanationText = when {
        isSafe -> "إذا نفذت هذا القرار الآن: ستسترد كامل تكلفته في غضون ${if (result.recoveryPointMonth > 0) "${result.recoveryPointMonth} شهراً" else "فترة قياسية"}، وسينمو رصيدك بمقدار +${String.format("%,.0f", netCashChange)} $currencySymbol بنهاية المدة، مع بقاء فترة أمان السيولة مستقرة."
        isWarning -> "القرار يضيف إيراداً ولكن التكلفة الأولية تضغط على سيولتك؛ سيصل رصيدك في أدنى نقطة إلى ${String.format("%,.0f", result.lowestCashPoint)} $currencySymbol. نوصي بالتفاوض على دفعات أو بناء احتياطي إضافي قبل التنفيذ."
        else -> "القرار يستهلك جزءاً كبيراً من السيولة ويقلص فترة الأمان إلى ${String.format("%.1f", result.projectedRunwayMonths)} شهر. يُنصح بعدم التنفيذ كاش حالياً والبحث عن بدائل مثل التقسيط أو تقليص النطاق."
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(2.dp, verdictColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            verdictColor.copy(alpha = 0.15f),
                            verdictColor.copy(alpha = 0.03f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(verdictColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = verdictIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "الحكم المالي المباشر:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = verdictTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = verdictColor
                        )
                    }
                }

                RiskBadge(level = result.riskLevel)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, verdictColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = explanationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 23.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * Before vs After Live Comparison Matrix
 * Explicitly and colorfully compares the user's financial status before vs after the decision.
 */
@Composable
private fun BeforeAfterComparisonCard(
    baseMetrics: FinancialMetrics,
    result: ScenarioResult,
    currencySymbol: String
) {
    val durationMonths = result.scenario.durationMonths
    val balanceChange = result.projectedCashAfterDuration - baseMetrics.currentBalance
    val runwayChange = result.projectedRunwayMonths - baseMetrics.runwayMonths

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, MasarSky.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MasarSky.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MasarSky.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = MasarSky,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "مقارنة وضعك: قبل القرار ⬅️ بعد القرار",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "جدول المقارنة الحية بالأرقام والفوارق",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comparison Rows with high contrast visual boxes
            ComparisonRowItem(
                label = "الرصيد النقدي بعد $durationMonths شهراً",
                beforeValue = "${String.format("%,.0f", baseMetrics.currentBalance)} $currencySymbol",
                afterValue = "${String.format("%,.0f", result.projectedCashAfterDuration)} $currencySymbol",
                deltaText = if (balanceChange >= 0) "+${String.format("%,.0f", balanceChange)} $currencySymbol" else "${String.format("%,.0f", balanceChange)} $currencySymbol",
                isPositive = balanceChange >= 0
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            ComparisonRowItem(
                label = "فترة أمان السيولة (Runway)",
                beforeValue = if (baseMetrics.runwayMonths >= 900) "مستدامة" else "${String.format("%.0f", baseMetrics.runwayMonths)} شهر",
                afterValue = if (result.projectedRunwayMonths >= 900) "مستدامة" else "${String.format("%.0f", result.projectedRunwayMonths)} شهر",
                deltaText = if (result.projectedRunwayMonths >= 900) "أمان تام" else if (runwayChange >= 0) "+${String.format("%.0f", runwayChange)} شهر" else "${String.format("%.0f", runwayChange)} شهر",
                isPositive = runwayChange >= 0 || result.projectedRunwayMonths >= 12
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            val newMonthlyNet = (baseMetrics.totalMonthlyRevenue + result.scenario.recurringMonthlyRevenue) - (baseMetrics.totalMonthlyExpenses + result.scenario.recurringMonthlyExpense)
            val netFlowChange = newMonthlyNet - baseMetrics.netMonthlyCashFlow

            ComparisonRowItem(
                label = "صافي التدفق الشهري",
                beforeValue = "${String.format("%+,.0f", baseMetrics.netMonthlyCashFlow)} $currencySymbol",
                afterValue = "${String.format("%+,.0f", newMonthlyNet)} $currencySymbol",
                deltaText = if (netFlowChange >= 0) "+${String.format("%,.0f", netFlowChange)} $currencySymbol" else "${String.format("%,.0f", netFlowChange)} $currencySymbol",
                isPositive = netFlowChange >= 0
            )
        }
    }
}

@Composable
private fun ComparisonRowItem(
    label: String,
    beforeValue: String,
    afterValue: String,
    deltaText: String,
    isPositive: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = beforeValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPositive) MasarEmerald.copy(alpha = 0.15f) else MasarRose.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = afterValue,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) MasarEmerald else MasarRose,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isPositive) MasarEmerald else MasarRose
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = deltaText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 3 Distinct Metric Mini-Cards with individual accent colors (Gold, Emerald, Sky).
 */
@Composable
private fun KeyIndicatorsCard(
    result: ScenarioResult,
    baseMetrics: FinancialMetrics,
    currencySymbol: String,
    showExplanations: Boolean,
    onToggleExplanations: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MasarGold, modifier = Modifier.size(20.dp))
                    Text(
                        text = "مؤشرات الجدوى والعائد:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { onToggleExplanations() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = MasarGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (showExplanations) "إخفاء الشرح" else "ماذا تعني؟",
                            style = MaterialTheme.typography.labelSmall,
                            color = MasarGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 High-Contrast Distinct Indicator Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Payback Month (Amber / Gold)
                IndicatorMiniCard(
                    title = "فترة الاسترداد",
                    value = if (result.recoveryPointMonth > 0) "${result.recoveryPointMonth} شهر" else "فوري",
                    subtitle = "تعويض التكلفة",
                    accentColor = MasarGold,
                    modifier = Modifier.weight(1f)
                )

                // Card 2: ROI % (Emerald / Green)
                val roi = result.roiPercentage
                IndicatorMiniCard(
                    title = "العائد (ROI)",
                    value = "${String.format("%+.0f", roi)}%",
                    subtitle = if (roi > 0) "ربح إضافي" else "تكلفة صافية",
                    accentColor = if (roi >= 0) MasarEmerald else MasarRose,
                    modifier = Modifier.weight(1f)
                )

                // Card 3: Lowest Cash Dip (Sky / Blue)
                IndicatorMiniCard(
                    title = "أدنى رصيد",
                    value = "${String.format("%,.0f", result.lowestCashPoint)} $currencySymbol",
                    subtitle = "أصعب نقطة سيولة",
                    accentColor = MasarSky,
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable Explanations
            AnimatedVisibility(visible = showExplanations) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    ExplanationRow(
                        title = "فترة الاسترداد (Payback Period):",
                        desc = "عدد الأشهر اللازمة حتى يغطي العائد الإضافي من هذا القرار كامل التكلفة التي دفعتها مقدماً.",
                        color = MasarGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExplanationRow(
                        title = "العائد على الاستثمار (ROI):",
                        desc = "النسبة المئوية لصافي الربح المتولد عن هذا القرار مقارنة بالمبلغ المستثمر فيه.",
                        color = MasarEmerald
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExplanationRow(
                        title = "أدنى رصيد سيولة (Cash Dip):",
                        desc = "أقل نقطة ينخفض إليها حسابك البنكي خلال فترة تنفيذ القرار للتأكد من عدم التعرض للإفلاس.",
                        color = MasarSky
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorMiniCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.09f),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ExplanationRow(
    title: String,
    desc: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
                .align(Alignment.Top)
        )
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}
