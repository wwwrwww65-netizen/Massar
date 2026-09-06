package com.example.ui.screens.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GeminiService
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

@Composable
fun SettingsScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onWipeData: () -> Unit
) {
    MasarRtlProvider {
        var showWipeConfirm by remember { mutableStateOf(false) }
        var showEditProfile by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                MasarHeader(
                    title = "الإعدادات والخصوصية والأمان",
                    subtitle = "تخصيص الحساب والعملة وإدارة الأمان والبيانات",
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
                // Profile & Business Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Column {
                                        Text(text = state.profile.businessName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "${state.profile.businessType.titleAr} • العملة: ${state.profile.baseCurrency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MasarEmerald.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { showEditProfile = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = MasarEmerald, modifier = Modifier.size(14.dp))
                                        Text("تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MasarEmerald)
                                    }
                                }
                            }
                        }
                    }
                }

                // Currency Preference
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "تغيير العملة الأساسية:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            val currencies = listOf("USD" to "دولار أمريكي ($)", "SAR" to "ريال سعودي (ر.س)", "AED" to "درهم إماراتي (د.إ)", "EGP" to "جنيه مصري (ج.م)", "KWD" to "دينار كويتي (د.ك)")
                            currencies.forEach { (code, name) ->
                                val isSelected = state.profile.baseCurrency == code
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onUpdateProfile(state.profile.copy(baseCurrency = code))
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = name, style = MaterialTheme.typography.bodyMedium)
                                    if (isSelected) {
                                        Text(text = "✓ نشطة", color = MasarEmerald, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Gemini AI Integration Status
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MasarEmerald.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MasarEmerald)
                                Text(
                                    text = "مرشد الأعمال والذكاء الاصطناعي (Gemini AI)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "تطبيق مسار متصل برمجياً بنماذج Google Gemini للتحليل المالي المتطور وتقديم توصيات ذكية وفورية داخل قسم 'اسأل مسار'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MasarEmerald.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MasarEmerald)
                                    )
                                    Text(
                                        text = "الذكاء الاصطناعي نشط ومترابط مع أرقامك المالية",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MasarEmerald
                                    )
                                }
                            }
                        }
                    }
                }

                // Privacy, Security & OWASP Standards
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MasarSky)
                                Text(text = "معايير الأمان والخصوصية (OWASP Compliance):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "• جميع العمليات الحسابية تنفذ محلياً عبر محرك Kotlin الرياضي الصارم.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "• البيانات مشفرة ومخزنة على جهازك في قاعدة بيانات Room مشفرة.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "• لا يتم مشاركة أرقامك المالية مع أي طرف ثالث إعلاني.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Data Deletion & Factory Reset
                item {
                    Button(
                        onClick = { showWipeConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MasarRose.copy(alpha = 0.15f), contentColor = MasarRose),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("مسح وحذف كافة البيانات من الجهاز (Reset)", fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            if (showWipeConfirm) {
                AlertDialog(
                    onDismissRequest = { showWipeConfirm = false },
                    title = { Text("تأكيد مسح البيانات", fontWeight = FontWeight.Bold) },
                    text = { Text("هل أنت متأكد من رغبتك في حذف جميع المعاملات، الأصول، السيناريوهات والملف الشخصي من جهازك نهائياً؟") },
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

            if (showEditProfile) {
                EditProfileDialog(
                    currentProfile = state.profile,
                    onDismiss = { showEditProfile = false },
                    onConfirm = { updated ->
                        onUpdateProfile(updated)
                        showEditProfile = false
                    }
                )
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.businessName) }
    var capital by remember { mutableStateOf(currentProfile.currentCapital.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل بيانات النشاط", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المشروع") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capital,
                    onValueChange = { capital = it },
                    label = { Text("الرصيد المتاح الأساسي") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capital.toDoubleOrNull() ?: currentProfile.currentCapital
                    onConfirm(currentProfile.copy(businessName = name, currentCapital = cap))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
