package com.example.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.engine.ScenarioEngine
import com.example.domain.model.ScenarioParameters
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.components.PrimaryHeroActionCard
import com.example.ui.components.SimpleCashStatusCard
import com.example.ui.components.SimpleProjectionsSection
import com.example.ui.components.SimpleSmartAdviceCard
import com.example.ui.components.TaskOrientedActionGrid
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState

/**
 * Redesigned Dashboard Screen:
 * Simple on the outside, complex deterministic intelligence in the background.
 * Features:
 * 1. "ماذا أفعل الآن؟" Primary Hero Action Card
 * 2. Accessible Cash Status with explicit text and dynamic profile adaptation
 * 3. Direct Verb Buttons (خفّض مصاريفك, حاكي شراء الأصل, حسّن السيولة, تسييل أصل)
 */
@Composable
fun DashboardScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit,
    onSelectScenario: (ScenarioParameters) -> Unit
) {
    MasarRtlProvider {
        Scaffold(
            topBar = {
                MasarHeader(
                    title = "لوحة المتابعة",
                    subtitle = "نظرة سريعة على أموالك وخياراتك القادمة",
                    businessName = state.profile.businessName,
                    currentScreen = AppScreen.DASHBOARD,
                    onNavigate = onNavigate
                )
            },
            bottomBar = {
                MasarBottomNavigation(
                    currentScreen = AppScreen.DASHBOARD,
                    onNavigate = onNavigate
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onNavigate(AppScreen.SIMULATOR) },
                    containerColor = MasarEmerald,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Text("جرّب قراراً", fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. "ماذا أفعل الآن؟" - Primary Hero Recommendation Card
                item {
                    PrimaryHeroActionCard(
                        profile = state.profile,
                        metrics = state.metrics,
                        currencySymbol = state.currencySymbol,
                        onNavigate = onNavigate
                    )
                }

                // 2. Primary Cash & Health Card with explicit accessible text + adaptivity
                item {
                    SimpleCashStatusCard(
                        profile = state.profile,
                        metrics = state.metrics,
                        currencySymbol = state.currencySymbol
                    )
                }

                // 3. Task-Oriented Action Grid: Direct Concrete Verbs (خفّض مصاريفك، حاكي، تسييل)
                item {
                    TaskOrientedActionGrid(
                        profile = state.profile,
                        onNavigate = onNavigate
                    )
                }

                // 4. Quick Decision Ideas Carousel
                item {
                    Text(
                        text = "محاكاة قرارات بنقرة واحدة:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val templates = ScenarioEngine.getPredefinedTemplates()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(templates) { template ->
                            QuickScenarioChip(
                                template = template,
                                currencySymbol = state.currencySymbol,
                                onClick = {
                                    onSelectScenario(template)
                                    onNavigate(AppScreen.SIMULATOR)
                                }
                            )
                        }
                    }
                }

                // 5. Simple Smart Advice Card (Translates complex risks into human action items)
                item {
                    SimpleSmartAdviceCard(
                        riskBreakdown = state.riskBreakdown,
                        onAskAdvisor = { onNavigate(AppScreen.ADVISOR) }
                    )
                }

                // 6. Simple Projections Section (Expandable 12-month graph)
                item {
                    SimpleProjectionsSection(
                        points = state.forecastPoints,
                        currencySymbol = state.currencySymbol
                    )
                }

                // Space for FAB & navigation
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun QuickScenarioChip(
    template: ScenarioParameters,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MasarSky,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (template.initialCost > 0) "تكلفة: ${template.initialCost.toInt()} $currencySymbol" else "${template.durationMonths} أشهر",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
