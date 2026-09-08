package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BusinessType
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.RiskLevel
import com.example.domain.model.UserProfile
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen

/**
 * "ماذا أفعل الآن؟" - Primary Hero Recommendation Card on Dashboard.
 * Translates underlying calculations directly into a single, concrete, high-impact guidance card.
 */
@Composable
fun PrimaryHeroActionCard(
    profile: UserProfile,
    metrics: FinancialMetrics,
    currencySymbol: String,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmployee = profile.businessType == BusinessType.EMPLOYEE
    val isStartupOrStore = profile.businessType == BusinessType.STARTUP || profile.businessType == BusinessType.STORE

    // Deterministic intelligence based on state
    data class HeroActionContent(
        val badgeText: String,
        val title: String,
        val description: String,
        val primaryButtonText: String,
        val targetScreen: AppScreen,
        val accentColor: Color,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val heroContent = when {
        // Condition 1: Low runway / High risk -> Immediate Cash Optimization
        metrics.overallRiskLevel == RiskLevel.CRITICAL || (metrics.runwayMonths < 4 && metrics.burnRate > 0) -> {
            HeroActionContent(
                badgeText = "تنبيه سيولة عاجل",
                title = if (isEmployee) "مصاريفك تتجاوز دخلك هذا الشهر" else "فترة الأمان (Runway) منخفضة جداً",
                description = if (isEmployee) {
                    "ننصح بمراجعة بنود المصاريف لترشيد 15-20% وتفادي استنزاف مدخراتك السائلة."
                } else {
                    "السيولة الحالية تكفيك لأقل من ${String.format("%.1f", metrics.runwayMonths)} أشهر. ننصح بخطة فورية لخفض تكاليف التشغيل."
                },
                primaryButtonText = "خفّض مصاريفك الآن",
                targetScreen = AppScreen.BOOTSTRAPPING,
                accentColor = MasarRose,
                icon = Icons.Default.Warning
            )
        }

        // Condition 2: Has frozen assets -> Monetization opportunity
        profile.hasFrozenAssets && profile.frozenAssetsValue > 0 -> {
            HeroActionContent(
                badgeText = "فرصة تسييل وتوليد دخل",
                title = "لديك أصول مجمدة بقيمة ${String.format("%,.0f", profile.frozenAssetsValue)} $currencySymbol",
                description = "يمكنك تحويل جزء من هذه الأصول أو تأجيرها لضخ سيولة تزيد أمانك المالي وتولد تدفقاً شهرياً جديداً.",
                primaryButtonText = "تسييل واستثمار الأصل",
                targetScreen = AppScreen.ASSETS,
                accentColor = MasarGold,
                icon = Icons.Default.MonetizationOn
            )
        }

        // Condition 3: Good savings / runway -> Test expansion or purchase
        metrics.runwayMonths >= 6 && metrics.netMonthlyCashFlow > 0 -> {
            HeroActionContent(
                badgeText = "اقتراح مسار الآن",
                title = if (isEmployee) "وضعك المالي مستقر مع فائض ادخار" else "لديك سيولة تشغيلية ممتازة",
                description = if (isEmployee) {
                    "لديك فائض شهري يبلغ ${String.format("%,.0f", metrics.netMonthlyCashFlow)} $currencySymbol. يمكنك تجربة أثر شراء أصل أو استثمار هذا الفائض."
                } else {
                    "يمكنك محاكاة قرارات التوسع أو التوظيف أو شراء معدات جديدة ومعرفة أثرها على رصيدك المستقبلي بأمان."
                },
                primaryButtonText = if (isEmployee) "حاكي شراء أصل / استثمار" else "حاكي قرار التوسع / التوظيف",
                targetScreen = AppScreen.SIMULATOR,
                accentColor = MasarEmerald,
                icon = Icons.Default.AutoAwesome
            )
        }

        // Condition 4: Default smart recommendation
        else -> {
            HeroActionContent(
                badgeText = "اقتراح مسار الآن",
                title = if (isEmployee) "بناء صندوق طوارئ يغطي 6 أشهر" else "تحسين كفاءة التدفق النقدي",
                description = if (isEmployee) {
                    "الادخار المنتظم لـ 20% من راتبك يرفع مدة أمانك المالي إلى 6 أشهر ويحميك من أي تقلبات مفاجئة."
                } else {
                    "راجع بنود التكاليف والإيرادات لضمان استدامة العمل وبناء احتياطي طوارئ قوي."
                },
                primaryButtonText = "استكشف خطة مسار",
                targetScreen = AppScreen.BOOTSTRAPPING,
                accentColor = MasarSky,
                icon = Icons.Default.Lightbulb
            )
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = heroContent.accentColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.5.dp, heroContent.accentColor.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Tag + Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = heroContent.accentColor.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(heroContent.accentColor)
                        )
                        Text(
                            text = heroContent.badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = heroContent.accentColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(heroContent.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = heroContent.icon,
                        contentDescription = null,
                        tint = heroContent.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = heroContent.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description in plain language
            Text(
                text = heroContent.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Direct Action Button (One-click transition)
            Button(
                onClick = { onNavigate(heroContent.targetScreen) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = heroContent.accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = heroContent.primaryButtonText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
