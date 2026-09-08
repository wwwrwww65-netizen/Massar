package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BusinessType
import com.example.domain.model.UserProfile
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onWipeData: () -> Unit
) {
    MasarRtlProvider {
        var showWipeConfirm by remember { mutableStateOf(false) }
        var showSaveSuccessDialog by remember { mutableStateOf(false) }

        // Form State initialized from current profile
        var nameInput by remember(state.profile) { mutableStateOf(state.profile.businessName) }
        var selectedRole by remember(state.profile) { mutableStateOf(state.profile.businessType) }
        var selectedCurrency by remember(state.profile) { mutableStateOf(state.profile.baseCurrency) }
        var capitalInput by remember(state.profile) { mutableStateOf(state.profile.currentCapital.toInt().toString()) }
        var incomeSourceInput by remember(state.profile) { mutableStateOf(state.profile.incomeSourceDescription) }
        var revenueInput by remember(state.profile) { mutableStateOf(state.profile.monthlyRevenue.toInt().toString()) }
        var expensesInput by remember(state.profile) { mutableStateOf(state.profile.monthlyExpenses.toInt().toString()) }
        var hasFrozenAssets by remember(state.profile) { mutableStateOf(state.profile.hasFrozenAssets) }
        var frozenAssetsValueInput by remember(state.profile) {
            mutableStateOf(if (state.profile.frozenAssetsValue > 0) state.profile.frozenAssetsValue.toInt().toString() else "")
        }

        var apiKeyInput by remember { mutableStateOf(com.example.data.remote.GeminiService.getActiveApiKey()) }
        var isApiKeySaved by remember { mutableStateOf(com.example.data.remote.GeminiService.getActiveApiKey().isNotBlank()) }

        val availableGoals = listOf(
            "بناء صندوق طوارئ (Runway)",
            "زيادة الادخار والاستثمار الشهري",
            "ترشيد المصاريف وإلغاء الهدر",
            "تسييل أصول أو بناء مصادر دخل جديدة",
            "سداد ديون والتزامات",
            "شراء أصل كبير (سيارة / عقار / معدات)",
            "توسيع نشاط تجاري أو توظيف"
        )
        val selectedGoals = remember(state.profile) {
            mutableStateListOf<String>().apply { addAll(state.profile.selectedGoals) }
        }

        val availableCurrencies = listOf(
            "USD" to "دولار أمريكي ($)",
            "SAR" to "ريال سعودي (ر.س)",
            "AED" to "درهم إماراتي (د.إ)",
            "EGP" to "جنيه مصري (ج.م)",
            "KWD" to "دينار كويتي (د.ك)",
            "IQD" to "دينار عراقي (د.ع)",
            "JOD" to "دينار أردني (د.أ)",
            "QAR" to "ريال قطري (ر.ق)"
        )

        val roleOptions = listOf(
            BusinessType.EMPLOYEE to ("موظف (دخل ثابت / راتب شهري)" to Icons.Default.Person),
            BusinessType.FREELANCER to ("عمل حر / مستقل (Freelancer)" to Icons.Default.LaptopMac),
            BusinessType.STORE to ("متجر / شركة / نشاط تجاري" to Icons.Default.Storefront),
            BusinessType.STARTUP to ("مشروع ناشئ / Startup" to Icons.Default.RocketLaunch),
            BusinessType.UNEMPLOYED to ("باحث عن عمل / بدون وظيفة حالياً" to Icons.Default.HourglassEmpty),
            BusinessType.INVESTOR to ("مستثمر / أصول وعقارات" to Icons.Default.AccountBalance),
            BusinessType.SERVICES to ("خدمات واستشارات" to Icons.Default.BusinessCenter)
        )

        Scaffold(
            topBar = {
                MasarHeader(
                    title = "الإعدادات والملف المالي",
                    subtitle = "تخصيص وضعك (موظف / مشروع)، مصادر الدخل، والأصول",
                    currentScreen = AppScreen.SETTINGS,
                    onNavigate = onNavigate
                )
            },
            bottomBar = {
                MasarBottomNavigation(
                    currentScreen = AppScreen.SETTINGS,
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
                // Header Intro Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MasarEmerald.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MasarEmerald.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MasarEmerald),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = Color.White)
                            }
                            Column {
                                Text(
                                    text = "تخصيص الحساب المالي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "حدد وضعك سواء كنت موظفاً براتب ثابت أو صاحب مشروع، ليتكيف مرشد مسار الذكي ومحرك الحسابات مع حالتك بدقة.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Section 1: Employment / Role Selection (Mandatory)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1. الوضع المهني ونوع الحساب",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                MandatoryBadge()
                            }

                            Text(
                                text = "اختر وضعك الحالي ليتم توجيه الاستشارات والتحليلات بما يناسبك:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                roleOptions.forEach { (role, data) ->
                                    val (title, icon) = data
                                    val isSelected = selectedRole == role
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MasarEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MasarEmerald else Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedRole = role
                                                if (role == BusinessType.EMPLOYEE && (incomeSourceInput.isBlank() || incomeSourceInput == "مشروعي")) {
                                                    incomeSourceInput = "راتب شهري ثابت"
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MasarEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MasarEmerald else MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "محدد",
                                                    tint = MasarEmerald,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Personal / Business Name & Currency (Mandatory)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2. الاسم والعملة الأساسية",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                MandatoryBadge()
                            }

                            // Name field
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = {
                                    Text(
                                        if (selectedRole == BusinessType.EMPLOYEE) "اسمك أو اسم الحساب الشخصي *"
                                        else "اسم النشاط / المتجر / اسمك *"
                                    )
                                },
                                placeholder = { Text(if (selectedRole == BusinessType.EMPLOYEE) "مثال: حساب محمد المالي" else "مثال: متجر الأفق") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MasarEmerald,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            // Currency Selection
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "العملة الأساسية للحساب *",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    availableCurrencies.forEach { (code, label) ->
                                        val isSelected = selectedCurrency == code
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedCurrency = code },
                                            label = { Text(label, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MasarEmerald.copy(alpha = 0.15f),
                                                selectedLabelColor = MasarEmerald
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = if (isSelected) MasarEmerald else MaterialTheme.colorScheme.outlineVariant,
                                                selectedBorderColor = MasarEmerald,
                                                enabled = true,
                                                selected = isSelected
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Cash & Monthly Flow (Mandatory & Optional)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "3. السيولة والتدفق المالي الشهري",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Current Capital / Liquid Savings (Mandatory)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الرصيد النقدي / المدخرات المتاحة فوراً",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    MandatoryBadge()
                                }
                                OutlinedTextField(
                                    value = capitalInput,
                                    onValueChange = { capitalInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    placeholder = { Text("مثال: 5000") },
                                    trailingIcon = { Text(selectedCurrency, modifier = Modifier.padding(end = 12.dp), fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MasarEmerald,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                                Text(
                                    text = "المبلغ المتاح في البنك أو كاش للطوارئ والصرف المباشر.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Income source description (Optional)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "مصدر وطبيعة الدخل",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    OptionalBadge()
                                }
                                OutlinedTextField(
                                    value = incomeSourceInput,
                                    onValueChange = { incomeSourceInput = it },
                                    placeholder = {
                                        Text(
                                            if (selectedRole == BusinessType.EMPLOYEE) "مثال: راتب شهري في شركة"
                                            else "مثال: مبيعات متجر وعقود مشاريع"
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Expected Monthly Income (Optional)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedRole == BusinessType.EMPLOYEE) "الراتب / الدخل الشهري المتوقع" else "إجمالي الإيرادات الشهرية المتوقعة",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    OptionalBadge()
                                }
                                OutlinedTextField(
                                    value = revenueInput,
                                    onValueChange = { revenueInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    placeholder = { Text("مثال: 2000") },
                                    trailingIcon = { Text(selectedCurrency, modifier = Modifier.padding(end = 12.dp)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Monthly Expenses (Optional)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedRole == BusinessType.EMPLOYEE) "المصاريف والالتزامات الشهرية الثابتة" else "تكاليف التشغيل والمصاريف الشهرية",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    OptionalBadge()
                                }
                                OutlinedTextField(
                                    value = expensesInput,
                                    onValueChange = { expensesInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    placeholder = { Text("مثال: 1400") },
                                    trailingIcon = { Text(selectedCurrency, modifier = Modifier.padding(end = 12.dp)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                // Section 4: Frozen / Illiquid Assets (Optional)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "4. الأصول والممتلكات المجمدة",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                OptionalBadge()
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "هل لديك أصول مجمدة لا تستطيع تشغيلها حالياً؟",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "مثل: عقار، أرض، معدات، أو استثمارات مغلقة يصعب تسييلها فوراً.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = hasFrozenAssets,
                                    onCheckedChange = { hasFrozenAssets = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MasarEmerald)
                                )
                            }

                            AnimatedVisibility(visible = hasFrozenAssets) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "القيمة التقديرية للأصول المجمدة:",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    OutlinedTextField(
                                        value = frozenAssetsValueInput,
                                        onValueChange = { frozenAssetsValueInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                        placeholder = { Text("مثال: 50000") },
                                        trailingIcon = { Text(selectedCurrency, modifier = Modifier.padding(end = 12.dp)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Text(
                                        text = "💡 سيقترح عليك المرشد الذكي خططاً لتسييلها أو الاستفادة منها تدريجياً.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MasarGold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 5: Financial Goals (Optional)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "5. أهدافك المالية الحالية",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                OptionalBadge()
                            }

                            Text(
                                text = "حدد ما ترغب في التركيز عليه خلال الفترة القادمة:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableGoals.forEach { goal ->
                                    val isSelected = selectedGoals.contains(goal)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) selectedGoals.remove(goal)
                                            else selectedGoals.add(goal)
                                        },
                                        label = { Text(goal, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MasarEmerald.copy(alpha = 0.15f),
                                            selectedLabelColor = MasarEmerald
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (isSelected) MasarEmerald else MaterialTheme.colorScheme.outlineVariant,
                                            selectedBorderColor = MasarEmerald,
                                            enabled = true,
                                            selected = isSelected
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 6: Gemini AI Key Configuration
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isApiKeySaved) MasarEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MasarGold, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "6. مفتاح الذكاء الاصطناعي (Gemini API)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isApiKeySaved) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MasarEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "متصل بنجاح ✓",
                                            color = MasarEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "يمكنك إدخال أو تعديل مفتاح Google Gemini API هنا لتفعيل التحليل الاستشاري المباشر:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { 
                                    apiKeyInput = it.trim()
                                },
                                placeholder = { Text("الصق مفتاح API هنا (مثل AQ... أو AIzaSy...)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MasarEmerald,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Button(
                                onClick = {
                                    com.example.data.remote.GeminiService.setCustomApiKey(apiKeyInput.trim())
                                    isApiKeySaved = apiKeyInput.trim().isNotBlank()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isApiKeySaved) MasarEmerald else MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isApiKeySaved) "تحديث وحفظ المفتاح" else "تفعيل مفتاح الذكاء الاصطناعي",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Save Changes Button (Primary Action)
                item {
                    Button(
                        onClick = {
                            val cap = capitalInput.toDoubleOrNull() ?: state.profile.currentCapital
                            val rev = revenueInput.toDoubleOrNull() ?: state.profile.monthlyRevenue
                            val exp = expensesInput.toDoubleOrNull() ?: state.profile.monthlyExpenses
                            val frozenVal = frozenAssetsValueInput.toDoubleOrNull() ?: 0.0

                            val updated = state.profile.copy(
                                businessName = nameInput.ifBlank { if (selectedRole == BusinessType.EMPLOYEE) "حسابي الشخصي" else "مشروعي" },
                                businessType = selectedRole,
                                baseCurrency = selectedCurrency,
                                currentCapital = cap,
                                monthlyRevenue = rev,
                                monthlyExpenses = exp,
                                hasFrozenAssets = hasFrozenAssets,
                                frozenAssetsValue = frozenVal,
                                incomeSourceDescription = incomeSourceInput.ifBlank { "دخل شهري" },
                                selectedGoals = selectedGoals.toList(),
                                onboardingCompleted = true
                            )
                            onUpdateProfile(updated)
                            showSaveSuccessDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حفظ وتحديث الإعدادات المالية",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Privacy, Security & Reset Section
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MasarSky, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "الخصوصية والأمان المحلي 100%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "• كافة بياناتك المالية مخزنة محلياً على جهازك ومحمية بالكامل.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• لا يتم مشاركة أرقامك أو تفاصيل حساباتك مع أي طرف ثالث إعلاني.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Wipe Data Button
                item {
                    Button(
                        onClick = { showWipeConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MasarRose.copy(alpha = 0.12f),
                            contentColor = MasarRose
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مسح كافة البيانات والبدء من جديد (Reset)", fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            if (showSaveSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveSuccessDialog = false },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MasarEmerald, modifier = Modifier.size(36.dp)) },
                    title = { Text("تم حفظ الإعدادات بنجاح", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("تم تحديث ملفك المالي وإعدادات الحساب. تم تكييف كافة التحليلات، التوصيات، ومرشد 'اسأل مسار' الذكي مع وضعك الجديد مباشرة.")
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSaveSuccessDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald)
                        ) {
                            Text("حسناً")
                        }
                    }
                )
            }

            if (showWipeConfirm) {
                AlertDialog(
                    onDismissRequest = { showWipeConfirm = false },
                    title = { Text("تأكيد مسح البيانات", fontWeight = FontWeight.Bold) },
                    text = { Text("هل أنت متأكد من رغبتك في حذف جميع المعاملات، الأصول، والملف المالي من جهازك نهائياً؟") },
                    confirmButton = {
                        Button(
                            onClick = {
                                onWipeData()
                                showWipeConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MasarRose)
                        ) {
                            Text("تأكيد المسح")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWipeConfirm = false }) { Text("إلغاء") }
                    }
                )
            }
        }
    }
}

@Composable
fun MandatoryBadge() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MasarEmerald.copy(alpha = 0.15f)
    ) {
        Text(
            text = "إجباري *",
            color = MasarEmerald,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun OptionalBadge() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = "اختياري",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
