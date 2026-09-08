package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BusinessType
import com.example.domain.model.UserProfile
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen

/**
 * Task-Oriented Direct Action Buttons:
 * Concrete verbs: "خفّض مصاريفك", "حاكي شراء الأصل", "حسّن السيولة", "تسييل أصل"
 */
@Composable
fun TaskOrientedActionGrid(
    profile: UserProfile,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmployee = profile.businessType == BusinessType.EMPLOYEE

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قرارات وإجراءات مباشرة بنقرة واحدة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "اختر ما تريده",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Button 1: Optimize expenses
                DirectActionButton(
                    actionVerb = "خفّض مصاريفك ووفر سيولة",
                    description = if (isEmployee) "اكتشف بنود الصرف الزائدة ووفر 15-20% من راتبك" else "خطة ترشيد تكاليف التشغيل وإيقاف الهدر المالي",
                    icon = Icons.Default.Savings,
                    accentColor = MasarEmerald,
                    buttonLabel = "ترشيد الآن",
                    onClick = { onNavigate(AppScreen.BOOTSTRAPPING) }
                )

                // Button 2: Simulate Purchase / Expansion
                DirectActionButton(
                    actionVerb = if (isEmployee) "حاكي شراء أصل كبير (سيارة/عقار/أجهزة)" else "حاكي التوسع وشراء الأصول والمعدات",
                    description = "شاهد أثر الشراء أو التقسيط على رصيدك المستقبلي قبل دفع أي قرش",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = MasarSky,
                    buttonLabel = "محاكاة الشراء",
                    onClick = { onNavigate(AppScreen.SIMULATOR) }
                )

                // Button 3: Monetize & Value Assets
                DirectActionButton(
                    actionVerb = "تسييل أصل أو استغلال ممتلكاتك",
                    description = if (profile.hasFrozenAssets) "لديك أصول مجمدة، استكشف طرق بيعها أو تأجيرها لضخ نقد" else "قيّم مقتنياتك أو مهاراتك وافتح مصادر دخل إضافية",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = MasarGold,
                    buttonLabel = "تسييل الأصول",
                    onClick = { onNavigate(AppScreen.ASSETS) }
                )

                // Button 4: Improve liquidity & emergency fund
                DirectActionButton(
                    actionVerb = if (isEmployee) "حسّن الأمان المالي وصندوق الطوارئ" else "حسّن السيولة وزد فترة الـ Runway",
                    description = "توصيات علمية لرفع أشهر الصمود المالي وتأمين مستقبلك",
                    icon = Icons.Default.TrendingUp,
                    accentColor = MaterialTheme.colorScheme.primary,
                    buttonLabel = "تحسين السيولة",
                    onClick = { onNavigate(AppScreen.ADVISOR) }
                )
            }
        }
    }
}

@Composable
private fun DirectActionButton(
    actionVerb: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = actionVerb,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = buttonLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
