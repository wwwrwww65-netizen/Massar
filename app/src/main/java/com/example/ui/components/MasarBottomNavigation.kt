package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MasarEmerald
import com.example.ui.viewmodel.AppScreen

sealed class BottomNavItem(
    val screen: AppScreen,
    val titleAr: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Dashboard : BottomNavItem(AppScreen.DASHBOARD, "الرئيسية", Icons.Default.Dashboard)
    object Simulator : BottomNavItem(AppScreen.SIMULATOR, "المحاكي", Icons.Default.AutoAwesome)
    object Transactions : BottomNavItem(AppScreen.TRANSACTIONS, "المعاملات", Icons.Default.ReceiptLong)
    object Assets : BottomNavItem(AppScreen.ASSETS, "الأصول", Icons.Default.AccountBalance)
    object Advisor : BottomNavItem(AppScreen.ADVISOR, "اسأل مسار", Icons.Default.Psychology)
}

@Composable
fun MasarBottomNavigation(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Simulator,
        BottomNavItem.Transactions,
        BottomNavItem.Assets,
        BottomNavItem.Advisor
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
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
                        contentDescription = item.titleAr
                    )
                },
                label = {
                    Text(
                        text = item.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
