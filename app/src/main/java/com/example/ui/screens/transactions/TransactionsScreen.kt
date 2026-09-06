package com.example.ui.screens.transactions

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BudgetItem
import com.example.domain.model.ExpenseNature
import com.example.domain.model.ExpensePriority
import com.example.domain.model.RecurrenceType
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.ValueType
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState

@Composable
fun TransactionsScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit,
    onAddTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    MasarRtlProvider {
        var selectedFilter by remember { mutableStateOf("ALL") }
        var showAddDialog by remember { mutableStateOf(false) }

        val filteredList = when (selectedFilter) {
            "INCOME" -> state.transactions.filter { it.type == TransactionType.INCOME }
            "EXPENSE" -> state.transactions.filter { it.type == TransactionType.EXPENSE }
            "RECURRING" -> state.transactions.filter { it.recurrence != RecurrenceType.NONE }
            else -> state.transactions
        }

        Scaffold(
            topBar = {
                MasarHeader(
                    title = "المعاملات والميزانيات",
                    subtitle = "سجل التدفقات المالية وتتبع حدود الإنفاق",
                    currentScreen = AppScreen.TRANSACTIONS,
                    onNavigate = onNavigate
                )
            },
            bottomBar = {
                MasarBottomNavigation(
                    currentScreen = AppScreen.TRANSACTIONS,
                    onNavigate = onNavigate
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MasarEmerald,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة معاملة")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Budgets Section (Progress & Threshold warnings)
                if (state.budgets.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "متابعة حدود الميزانية الشهرية:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                state.budgets.forEach { b ->
                                    BudgetItemRow(budget = b, currencySymbol = state.currencySymbol)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                // Filter Chips
                item {
                    val filters = listOf(
                        "ALL" to "الكل (${state.transactions.size})",
                        "INCOME" to "الإيرادات",
                        "EXPENSE" to "المصروفات",
                        "RECURRING" to "المتكررة"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { (key, label) ->
                            val isSelected = selectedFilter == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable { selectedFilter = key }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Transactions List
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد معاملات مسجلة في هذا القسم",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        TransactionRowItem(
                            item = item,
                            currencySymbol = state.currencySymbol,
                            onDelete = { onDeleteTransaction(item.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            if (showAddDialog) {
                AddTransactionDialog(
                    currency = state.profile.baseCurrency,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { tx ->
                        onAddTransaction(tx)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun BudgetItemRow(budget: BudgetItem, currencySymbol: String) {
    val usage = budget.usagePercentage
    val progressColor = when {
        usage >= 100.0 -> MasarRose
        usage >= 90.0 -> Color(0xFFF97316)
        usage >= 70.0 -> MasarGold
        else -> MasarEmerald
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = budget.category, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${String.format("%,.0f", budget.currentSpent)} / ${String.format("%,.0f", budget.monthlyLimit)} $currencySymbol",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (usage / 100.0).coerceIn(0.0, 1.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        if (usage >= 70.0) {
            Text(
                text = budget.statusAlert,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = progressColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    item: TransactionItem,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val isIncome = item.type == TransactionType.INCOME
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isIncome) MasarEmerald.copy(alpha = 0.15f) else MasarRose.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isIncome) MasarEmerald else MasarRose,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(text = item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (item.recurrence != RecurrenceType.NONE) {
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(text = item.recurrence.titleAr, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}${String.format("%,.0f", item.amount)} $currencySymbol",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) MasarEmerald else MasarRose
                    )
                    Text(
                        text = if (item.valueType == ValueType.ESTIMATED) "تقديري" else "فعلي",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (TransactionItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf("عام") }
    var recurrence by remember { mutableStateOf(RecurrenceType.MONTHLY) }
    var priority by remember { mutableStateOf(ExpensePriority.IMPORTANT) }
    var isActual by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة تدفق مالي جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type Switcher
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { type = TransactionType.INCOME },
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.INCOME) MasarEmerald else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إيراد (+)", color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = { type = TransactionType.EXPENSE },
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.EXPENSE) MasarRose else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("مصروف (-)", color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("الوصف أو العنوان") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ") },
                    suffix = { Text(currency) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف (مكتب، سيرفرات، تسويق...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Recurrence
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("التكرار:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(RecurrenceType.NONE, RecurrenceType.MONTHLY).forEach { r ->
                            val isSel = recurrence == r
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { recurrence = r }
                            ) {
                                Text(r.titleAr, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(
                            TransactionItem(
                                title = title,
                                amount = amt,
                                type = type,
                                category = category.ifBlank { "عام" },
                                date = "2026-03-01",
                                currency = currency,
                                valueType = if (isActual) ValueType.ACTUAL else ValueType.ESTIMATED,
                                priority = priority,
                                recurrence = recurrence
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald)
            ) {
                Text("حفظ المعاملة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
