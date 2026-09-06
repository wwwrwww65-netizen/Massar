package com.example.ui.screens.assets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.AssetCategory
import com.example.domain.model.AssetItem
import com.example.domain.model.AssetStatus
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState

/**
 * Redesigned Assets & Valuation Screen:
 * Simple, step-by-step monetization and valuation flow.
 * Shows fair market value, recommended path (sell vs rent), and immediate cash potential in plain language.
 */
@Composable
fun AssetsValuationScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit,
    onAddAsset: (AssetItem) -> Unit,
    onSelectAssetForValuation: (AssetItem) -> Unit,
    onUpdateStatus: (AssetItem, AssetStatus) -> Unit,
    onDeleteAsset: (Long) -> Unit
) {
    MasarRtlProvider {
        var showAddDialog by remember { mutableStateOf(false) }
        var showAllOptions by remember { mutableStateOf(false) }
        val valuation = state.activeValuationResult

        Scaffold(
            topBar = {
                MasarHeader(
                    title = "تقييم الأصول وتحويلها لسيولة",
                    subtitle = "اكتشف القيمة العادلة لمشاريعك وممتلكاتك وخيارات تسييلها",
                    currentScreen = AppScreen.ASSETS,
                    onNavigate = onNavigate
                )
            },
            bottomBar = {
                MasarBottomNavigation(
                    currentScreen = AppScreen.ASSETS,
                    onNavigate = onNavigate
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MasarEmerald,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة أصل")
                        Text("إضافة أصل", fontWeight = FontWeight.Bold)
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
                // Portfolio Summary Hero Card
                item {
                    val totalEstimatedValue = state.assets.sumOf { it.estimatedValue }
                    val totalAssetIncome = state.assets.sumOf { it.monthlyIncome }

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
                                    text = "إجمالي قيمة أصولك المسجلة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.assets.size} أصول",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

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
                                        Text("القيمة السوقية الكلية", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = String.format("%,.0f %s", totalEstimatedValue, state.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MasarEmerald
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("الدخل الشهري منها", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = String.format("+%,.0f %s", totalAssetIncome, state.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MasarSky
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Assets Horizontal Selector
                item {
                    Text(
                        text = "اختر أصلاً لمعرفة قيمته وأفضل طريقة لتحويله لنقد:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.assets.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "لم تسجل أي أصل بعد. اضغط على زر 'إضافة أصل' لإدخال متجرك، مشروعك الرقمي، أو معداتك.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.assets, key = { it.id }) { asset ->
                                val isSelected = valuation?.asset?.id == asset.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.clickable { onSelectAssetForValuation(asset) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = asset.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${String.format("%,.0f", asset.estimatedValue)} ${state.currencySymbol} • ${asset.category.titleAr}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Simplified Valuation & Recommended Monetization Plan
                if (valuation != null) {
                    item {
                        val recommendedOption = valuation.liquidityOptions.firstOrNull()

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "تقييم: ${valuation.asset.title}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "القيمة العادلة المقدرة: ${String.format("%,.0f", valuation.likelyEstimate)} ${state.currencySymbol}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MasarEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MasarEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "ثقة ${valuation.confidenceScore}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MasarEmerald,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Highlighted Best Monetization Recommendation
                                if (recommendedOption != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MasarEmerald.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, MasarEmerald.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MasarEmerald, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = "الخيار الأفضل المقترح:",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MasarEmerald
                                                    )
                                                }
                                                Text(
                                                    text = "+${String.format("%,.0f", recommendedOption.expectedCash)} ${state.currencySymbol}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MasarEmerald
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = recommendedOption.titleAr,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = recommendedOption.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "⏱ زمن التحصيل المتوقع: خلال ${recommendedOption.timeToCashDays} يوماً",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MasarSky,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progressive Disclosure Toggle for other liquidity options & estimates
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showAllOptions = !showAllOptions }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (showAllOptions) "إخفاء باقي الخيارات ونطاقات الأسعار" else "عرض باقي خيارات التسييل ونطاقات التقييم",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = if (showAllOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                AnimatedVisibility(visible = showAllOptions) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Valuation Range (Fast vs Fair vs High)
                                        Text(text = "نطاقات الأسعار المحتملة:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            ValuationMetricBox(
                                                title = "البيع السريع",
                                                value = String.format("%,.0f %s", valuation.lowEstimate, state.currencySymbol),
                                                color = MasarRose,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ValuationMetricBox(
                                                title = "السعر العادل",
                                                value = String.format("%,.0f %s", valuation.likelyEstimate, state.currencySymbol),
                                                color = MasarEmerald,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ValuationMetricBox(
                                                title = "الحد الأعلى",
                                                value = String.format("%,.0f %s", valuation.highEstimate, state.currencySymbol),
                                                color = MasarSky,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        // Secondary Liquidity Options
                                        if (valuation.liquidityOptions.size > 1) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(text = "بدائل تسييل أخرى:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(6.dp))

                                            valuation.liquidityOptions.drop(1).forEach { opt ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 3.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(text = opt.titleAr, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                            Text(text = "${String.format("%,.0f", opt.expectedCash)} ${state.currencySymbol}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MasarEmerald)
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(text = opt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            if (showAddDialog) {
                AddAssetDialog(
                    currency = state.profile.baseCurrency,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { asset ->
                        onAddAsset(asset)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ValuationMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AddAssetDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (AssetItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(AssetCategory.SAAS) }
    var estimatedValue by remember { mutableStateOf("") }
    var monthlyIncome by remember { mutableStateOf("") }
    var rentalValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة أصل جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الأصل (مشروع SaaS، جهاز، عقار...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estimatedValue,
                    onValueChange = { estimatedValue = it },
                    label = { Text("القيمة التقديرية الحالية") },
                    suffix = { Text(currency) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthlyIncome,
                    onValueChange = { monthlyIncome = it },
                    label = { Text("الدخل الشهري المتولد منه (إن وجد)") },
                    suffix = { Text("$currency/شهر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rentalValue,
                    onValueChange = { rentalValue = it },
                    label = { Text("القيمة الإيجارية الشهرية المقترحة") },
                    suffix = { Text("$currency/شهر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val est = estimatedValue.toDoubleOrNull() ?: 0.0
                    val inc = monthlyIncome.toDoubleOrNull() ?: 0.0
                    val rent = rentalValue.toDoubleOrNull() ?: 0.0

                    if (title.isNotBlank() && est > 0) {
                        onConfirm(
                            AssetItem(
                                title = title,
                                description = "أصل مسجل في مسار",
                                category = category,
                                purchasePrice = est,
                                estimatedValue = est,
                                monthlyIncome = inc,
                                rentalValue = rent,
                                status = AssetStatus.ACTIVE,
                                currency = currency
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald)
            ) {
                Text("حفظ وتقييم الأصل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
