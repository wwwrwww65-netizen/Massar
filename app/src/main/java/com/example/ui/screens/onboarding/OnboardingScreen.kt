package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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

/**
 * Modern Goal & Situation Adaptive Onboarding:
 * Adapts tools, labels, and financial models according to the user's real situation:
 * (Employee / Personal Finance vs Business / Commercial Venture)
 */
@Composable
fun OnboardingScreen(
    onComplete: (
        businessName: String,
        businessType: BusinessType,
        baseCurrency: String,
        capital: Double,
        revenue: Double,
        expenses: Double,
        goals: List<String>
    ) -> Unit
) {
    MasarRtlProvider {
        var currentStep by remember { mutableStateOf(0) }

        var profileName by remember { mutableStateOf("حسابي المالي") }
        var selectedSituation by remember { mutableStateOf(BusinessType.EMPLOYEE) }
        var baseCurrency by remember { mutableStateOf("USD") }
        var capitalInput by remember { mutableStateOf("12000") }
        var revenueInput by remember { mutableStateOf("4500") }
        var expensesInput by remember { mutableStateOf("2800") }

        val isEmployee = selectedSituation == BusinessType.EMPLOYEE

        val employeeGoals = listOf(
            "بناء صندوق طوارئ يغطي 6 أشهر",
            "ترشيد المصاريف اليومية وتوفير 20%",
            "محاكاة شراء أصل كبير (سيارة / عقار / زواج)",
            "سداد الالتزامات والأقساط بأسرع وقت",
            "استثمار الفائض الشهري وتنمية المدخرات"
        )

        val businessGoals = listOf(
            "المحافظة على السيولة ورفع الـ Runway لأكثر من 12 شهراً",
            "ترشيد تكاليف التشغيل وإلغاء الهدر",
            "محاكاة قرارات التوظيف وشراء المعدات والتوسع",
            "تسييل الأصول وبناء مصادر دخل جديدة",
            "الاستعداد لجولة استثمارية أو تمويل تجاري"
        )

        val selectedGoals = remember {
            mutableStateListOf("بناء صندوق طوارئ يغطي 6 أشهر", "ترشيد المصاريف اليومية وتوفير 20%")
        }

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
                        1 -> StepAdaptiveSituation(
                            profileName = profileName,
                            onNameChange = { profileName = it },
                            selectedSituation = selectedSituation,
                            onSituationSelect = { sit ->
                                selectedSituation = sit
                                selectedGoals.clear()
                                if (sit == BusinessType.EMPLOYEE) {
                                    profileName = "مصاريفي ومدخراتي الشخصية"
                                    selectedGoals.addAll(listOf("بناء صندوق طوارئ يغطي 6 أشهر", "ترشيد المصاريف اليومية وتوفير 20%"))
                                } else {
                                    profileName = "مشروعي ونشاطي التجاري"
                                    selectedGoals.addAll(listOf("المحافظة على السيولة ورفع الـ Runway لأكثر من 12 شهراً", "محاكاة قرارات التوظيف وشراء المعدات والتوسع"))
                                }
                            },
                            currency = baseCurrency,
                            onCurrencySelect = { baseCurrency = it }
                        )
                        2 -> StepAdaptiveBaseline(
                            isEmployee = isEmployee,
                            capital = capitalInput,
                            onCapitalChange = { capitalInput = it },
                            revenue = revenueInput,
                            onRevenueChange = { revenueInput = it },
                            expenses = expensesInput,
                            onExpensesChange = { expensesInput = it },
                            currency = baseCurrency
                        )
                        3 -> StepGoalsAndFinish(
                            isEmployee = isEmployee,
                            goals = if (isEmployee) employeeGoals else businessGoals,
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
                                val cap = capitalInput.toDoubleOrNull() ?: 12000.0
                                val rev = revenueInput.toDoubleOrNull() ?: 4500.0
                                val exp = expensesInput.toDoubleOrNull() ?: 2800.0
                                onComplete(
                                    profileName,
                                    selectedSituation,
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
            text = "مرحباً بك في مسار",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "مستشارك المالي الذكي لاتخاذ قرارات سليمة ومحاكاة مستقبلك المالي بأقل جهد.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        ValuePropCard(
            icon = Icons.Default.AutoAwesome,
            title = "محاكاة القرارات قبل اتخاذها",
            description = "جرّب أثر شراء أصل، قرض، أو توظيف على رصيدك المستقبلي قبل دفع أي قرش."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ValuePropCard(
            icon = Icons.Default.Speed,
            title = "فترة الأمان وصندوق الطوارئ",
            description = "احسب كم شهراً تكفيك مدخراتك وسيولتك إذا انقطع الدخل أو ارتفعت النفقات."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ValuePropCard(
            icon = Icons.Default.Psychology,
            title = "مرشد مالي ذكي واستباقي",
            description = "يقترح عليك بنود التوفير وتسييل الأصول والخطوات العملية فوراً."
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
fun StepAdaptiveSituation(
    profileName: String,
    onNameChange: (String) -> Unit,
    selectedSituation: BusinessType,
    onSituationSelect: (BusinessType) -> Unit,
    currency: String,
    onCurrencySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "هدفك ووضعك المالي",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اختر وضعك المالي لتتكيف واجهات مسار ومحركاته مع أهدافك الحقيقية.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = profileName,
            onValueChange = onNameChange,
            label = { Text("اسم الملف المالي أو النشاط") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "اختر طبيعة وضعك المالي:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Situation 1: Employee / Personal
        SituationCard(
            title = "موظف / إدارة مالية شخصية",
            subtitle = "أدوات مخصصة للراتب الشهري، ضبط المصاريف، بناء صندوق طوارئ، والادخار.",
            icon = Icons.Default.Badge,
            isSelected = selectedSituation == BusinessType.EMPLOYEE,
            onClick = { onSituationSelect(BusinessType.EMPLOYEE) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Situation 2: Freelancer
        SituationCard(
            title = "عمل حر / مستقل (Freelancer)",
            subtitle = "دخل متغير، تسعير خدمات، إدارة فترات الركود، وحماية السيولة.",
            icon = Icons.Default.Laptop,
            isSelected = selectedSituation == BusinessType.FREELANCER,
            onClick = { onSituationSelect(BusinessType.FREELANCER) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Situation 3: Business / Store / Startup
        SituationCard(
            title = "صاحب مشروع / نشاط تجاري",
            subtitle = "إيرادات، تكاليف تشغيل، حساب فترة الـ Runway، أصول، ومحاكاة التوظيف.",
            icon = Icons.Default.ShoppingBag,
            isSelected = selectedSituation == BusinessType.STORE || selectedSituation == BusinessType.STARTUP,
            onClick = { onSituationSelect(BusinessType.STORE) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Situation 4: Investor
        SituationCard(
            title = "مستثمر / أصول وعقارات",
            subtitle = "عائد على الاستثمار (ROI)، إدارة السيولة، تقييم وتسييل الأصول المجمدة.",
            icon = Icons.Default.MonetizationOn,
            isSelected = selectedSituation == BusinessType.INVESTOR,
            onClick = { onSituationSelect(BusinessType.INVESTOR) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "العملة الأساسية:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val currencies = listOf("USD" to "دولار ($)", "SAR" to "ريال (ر.س)", "AED" to "درهم (د.إ)", "EGP" to "جنيه (ج.م)")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currencies.forEach { (code, label) ->
                val isSelected = currency == code
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MasarEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MasarEmerald else MaterialTheme.colorScheme.outlineVariant),
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
private fun SituationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StepAdaptiveBaseline(
    isEmployee: Boolean,
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
            text = if (isEmployee) "أرقامك المالية الأساسية" else "خط الأساس المالي للنشاط",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isEmployee) "أدخل رصيدك الحالي وراتبك ومصاريفك التقديرية لبناء خطة الادخار." else "أدخل السيولة المتاحة والإيرادات وتكاليف التشغيل لحساب الـ Runway.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = capital,
            onValueChange = onCapitalChange,
            label = { Text(if (isEmployee) "الرصيد والمدخرات المتاحة حالياً (الكاش)" else "السيولة النقدية المتاحة (Cash)") },
            suffix = { Text(currency) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = revenue,
            onValueChange = onRevenueChange,
            label = { Text(if (isEmployee) "الراتب / إجمالي الدخل الشهري" else "متوسط الإيراد والمبيعات الشهرية") },
            suffix = { Text("$currency/شهر") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = expenses,
            onValueChange = onExpensesChange,
            label = { Text(if (isEmployee) "المصروفات والالتزامات الشهرية" else "تكاليف التشغيل والمصروفات الشهرية") },
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
        val runway = if (expVal > 0) capVal / expVal else 999.0

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "المؤشرات المبدئية لحالتك:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEmployee) "فائض الادخار: ${String.format("%+,.0f", net)} $currency/ش" else "صافي التدفق: ${String.format("%+,.0f", net)} $currency/ش",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (net >= 0) MasarEmerald else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "كفاية المدخرات: ${String.format("%.1f", runway)} أشهر",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StepGoalsAndFinish(
    isEmployee: Boolean,
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
            text = "أهدافك ذات الأولوية",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اختر ما ترغب بالتركيز عليه ليقترح مسار الإجراءات والحلول الأنسب:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        goals.forEach { goal ->
            val isSelected = selectedGoals.contains(goal)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
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
