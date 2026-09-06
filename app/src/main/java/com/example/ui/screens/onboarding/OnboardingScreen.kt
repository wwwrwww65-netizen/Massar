package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BusinessType
import com.example.ui.components.MasarRtlProvider
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarSky

@Composable
fun OnboardingScreen(
    onComplete: (
        businessName: String,
        businessType: BusinessType,
        currency: String,
        capital: Double,
        revenue: Double,
        expenses: Double,
        goals: List<String>
    ) -> Unit
) {
    MasarRtlProvider {
        var currentStep by remember { mutableStateOf(0) }

        var businessName by remember { mutableStateOf("مشروعي الرقمي") }
        var selectedBusinessType by remember { mutableStateOf(BusinessType.FREELANCER) }
        var baseCurrency by remember { mutableStateOf("USD") }
        var capitalInput by remember { mutableStateOf("9500") }
        var revenueInput by remember { mutableStateOf("3600") }
        var expensesInput by remember { mutableStateOf("2100") }

        val availableGoals = listOf(
            "المحافظة على السيولة لأكثر من 12 شهراً",
            "ترشيد المصاريف وإلغاء الهدر",
            "محاكاة قرارات التوظيف والتوسع",
            "تسييل الأصول وبناء مصادر دخل جديدة",
            "الاستعداد لجولة استثمارية أو تمويل"
        )
        val selectedGoals = remember { mutableStateListOf("المحافظة على السيولة لأكثر من 12 شهراً", "محاكاة قرارات التوظيف والتوسع") }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i <= currentStep) MasarEmerald else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier.weight(1f),
                    label = "OnboardingContent"
                ) { step ->
                    when (step) {
                        0 -> StepValueProposition(
                            onNext = { currentStep = 1 }
                        )
                        1 -> StepBusinessType(
                            businessName = businessName,
                            onNameChange = { businessName = it },
                            selectedType = selectedBusinessType,
                            onTypeSelect = { selectedBusinessType = it },
                            currency = baseCurrency,
                            onCurrencySelect = { baseCurrency = it }
                        )
                        2 -> StepFinancialBaseline(
                            capital = capitalInput,
                            onCapitalChange = { capitalInput = it },
                            revenue = revenueInput,
                            onRevenueChange = { revenueInput = it },
                            expenses = expensesInput,
                            onExpensesChange = { expensesInput = it },
                            currency = baseCurrency
                        )
                        3 -> StepGoalsAndFinish(
                            goals = availableGoals,
                            selectedGoals = selectedGoals,
                            onToggleGoal = { g ->
                                if (selectedGoals.contains(g)) selectedGoals.remove(g) else selectedGoals.add(g)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("السابق")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                val cap = capitalInput.toDoubleOrNull() ?: 9500.0
                                val rev = revenueInput.toDoubleOrNull() ?: 3600.0
                                val exp = expensesInput.toDoubleOrNull() ?: 2100.0
                                onComplete(
                                    businessName,
                                    selectedBusinessType,
                                    baseCurrency,
                                    cap,
                                    rev,
                                    exp,
                                    selectedGoals.toList()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = if (currentStep == 3) "بدء التحليل والانطلاق 🚀" else "المتابعة",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepValueProposition(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MasarEmerald.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MasarEmerald,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "مرحباً بك في منصة مسار 2.0",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "منصة ذكاء القرار المالي ومحاكاة مستقبل أعمالك بدقة علمية قبل اتخاذ أي خطوة.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        ValuePropCard(
            icon = Icons.Default.AutoAwesome,
            title = "محاكي السيناريوهات (What-If)",
            description = "جرب قرارات التوظيف، الشراء، أو رفع الأسعار وشاهد أثرها على السيولة قبل التنفيذ."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ValuePropCard(
            icon = Icons.Default.Speed,
            title = "حساب الـ Runway ومعدل الحرق",
            description = "محرك حسابي دقيق يوضح كم شهراً تستطيع الصمود وحساب نقاط الاسترداد (Payback)."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ValuePropCard(
            icon = Icons.Default.Psychology,
            title = "مرشد الأعمال الذكي (اسأل مسار)",
            description = "استشارات مالية مخصصة لتحليل قراراتك واكتشاف فرص ترشيد وتسييل الأصول."
        )
    }
}

@Composable
fun ValuePropCard(icon: ImageVector, title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StepBusinessType(
    businessName: String,
    onNameChange: (String) -> Unit,
    selectedType: BusinessType,
    onTypeSelect: (BusinessType) -> Unit,
    currency: String,
    onCurrencySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "هوية ونوع النشاط",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "حدد طبيعة عملك لتخصيص حسابات المحاكاة والتقييم.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = onNameChange,
            label = { Text("اسم المشروع أو النشاط") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "نوع العمل التجاري:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val types = BusinessType.values().take(8)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            types.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTypeSelect(type) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = type.titleAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "العملة الأساسية للحسابات:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val currencies = listOf("USD" to "دولار أمريكي ($)", "SAR" to "ريال سعودي (ر.س)", "AED" to "درهم إماراتي (د.إ)", "EGP" to "جنيه مصري (ج.م)")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currencies.forEach { (code, label) ->
                val isSelected = currency == code
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MasarEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MasarEmerald else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCurrencySelect(code) }
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MasarEmerald else MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StepFinancialBaseline(
    capital: String,
    onCapitalChange: (String) -> Unit,
    revenue: String,
    onRevenueChange: (String) -> Unit,
    expenses: String,
    onExpensesChange: (String) -> Unit,
    currency: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "خط الأساس المالي",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "أدخل أرقامك الحالية (فعلية أو تقديرية) لبناء نموذج المحاكاة الأولي.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = capital,
            onValueChange = onCapitalChange,
            label = { Text("الرصيد النقدي المتاح حالياً (Cash)") },
            suffix = { Text(currency) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = revenue,
            onValueChange = onRevenueChange,
            label = { Text("متوسط الإيراد الشهري") },
            suffix = { Text("$currency/شهر") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = expenses,
            onValueChange = onExpensesChange,
            label = { Text("متوسط المصروفات الشهرية") },
            suffix = { Text("$currency/شهر") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick live calculation box
        val capVal = capital.toDoubleOrNull() ?: 0.0
        val revVal = revenue.toDoubleOrNull() ?: 0.0
        val expVal = expenses.toDoubleOrNull() ?: 0.0
        val net = revVal - expVal
        val runway = if (net < 0) capVal / -net else 999.0

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "المؤشرات الأولية المحسوبة:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "صافي التدفق: ${String.format("%+,.0f", net)} $currency/ش", style = MaterialTheme.typography.bodySmall, color = if (net >= 0) MasarEmerald else Color(0xFFEF4444))
                    Text(text = "الـ Runway: ${if (runway >= 900) "مستدام (+)" else "${String.format("%.1f", runway)} شهر"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StepGoalsAndFinish(
    goals: List<String>,
    selectedGoals: List<String>,
    onToggleGoal: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "أهدافك المالية والاستراتيجية",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اختر ما ترغب بالتركيز عليه ليقترح المحاكي البدائل المناسبة:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        goals.forEach { goal ->
            val isSelected = selectedGoals.contains(goal)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleGoal(goal) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = goal,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
