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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.domain.engine.ScenarioEngine
import com.example.domain.model.ScenarioParameters
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen

/**
 * Task-Oriented Quick Action Grid: "ماذا تريد أن تفعل الآن؟"
 * Connects user goals directly to the relevant simplified sub-flows.
 */
@Composable
fun TaskOrientedActionGrid(
    onNavigate: (AppScreen) -> Unit,
    onSelectScenario: (ScenarioParameters) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "ماذا تريد أن تفعل الآن؟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "إجراءات مباشرة",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Action 1: Test a decision / Purchase
                QuickActionRowItem(
                    title = "أفكر في قرار مالي أو شراء أصل",
                    subtitle = "احسب أثر الشراء أو التوظيف على رصيدك المستقبلي فوراً",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = MasarSky,
                    onClick = { onNavigate(AppScreen.SIMULATOR) }
                )

                // Action 2: Optimize & Cut unnecessary expenses
                QuickActionRowItem(
                    title = "أريد ترشيد مصاريفي وتوفير سيولة",
                    subtitle = "اكتشف الهدر واعرف كم شهراً إضافياً ستكسبه بتوفير ذكي",
                    icon = Icons.Default.Savings,
                    iconTint = MasarEmerald,
                    onClick = { onNavigate(AppScreen.BOOTSTRAPPING) }
                )

                // Action 3: Monetize & Value Assets
                QuickActionRowItem(
                    title = "أريد تقييم أصولي وتحويلها لنقد",
                    subtitle = "احسب قيمة مشاريعك أو مقتنياتك واستكشف خيارات بيعها",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = MasarGold,
                    onClick = { onNavigate(AppScreen.ASSETS) }
                )

                // Action 4: Ask Masar Advisor
                QuickActionRowItem(
                    title = "استشارة مرشد الأعمال الذكي",
                    subtitle = "اطرح أي استفسار تجاري أو مالي واحصل على خطة عملية مخصصة",
                    icon = Icons.Default.Psychology,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigate(AppScreen.ADVISOR) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
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

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
