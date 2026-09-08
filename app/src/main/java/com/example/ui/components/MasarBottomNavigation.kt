package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MasarEmerald
import com.example.ui.viewmodel.AppScreen

sealed class BottomNavItem(
    val screen: AppScreen,
    val titleAr: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem(AppScreen.DASHBOARD, "الرئيسية", Icons.Default.Dashboard)
    object Transactions : BottomNavItem(AppScreen.TRANSACTIONS, "المعاملات", Icons.Default.ReceiptLong)
    object Simulator : BottomNavItem(AppScreen.SIMULATOR, "المحاكي", Icons.Default.AutoAwesome)
    object Advisor : BottomNavItem(AppScreen.ADVISOR, "اسأل مسار", Icons.Default.Psychology)
    object Settings : BottomNavItem(AppScreen.SETTINGS, "الإعدادات", Icons.Default.Settings)
}

/**
 * Standard Material 3 NavigationBar with automatic system window insets
 * to prevent clipping by system navigation bar or gesture pill.
 */
@Composable
fun MasarBottomNavigation(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Simulator,
        BottomNavItem.Advisor,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.titleAr,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MasarEmerald,
                    selectedTextColor = MasarEmerald,
                    indicatorColor = MasarEmerald.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )
        }
    }
}
