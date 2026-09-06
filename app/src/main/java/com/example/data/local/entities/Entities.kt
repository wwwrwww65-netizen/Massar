package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.domain.model.AssetCategory
import com.example.domain.model.AssetStatus
import com.example.domain.model.BusinessType
import com.example.domain.model.ExpenseNature
import com.example.domain.model.ExpensePriority
import com.example.domain.model.RecurrenceType
import com.example.domain.model.ScenarioType
import com.example.domain.model.TransactionType
import com.example.domain.model.ValueType

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default_user",
    val businessName: String,
    val businessType: String,
    val baseCurrency: String,
    val currentCapital: Double,
    val monthlyRevenue: Double,
    val monthlyExpenses: Double,
    val selectedGoals: String, // Comma separated
    val onboardingCompleted: Boolean,
    val isPro: Boolean
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val category: String,
    val date: String,
    val currency: String,
    val valueType: String, // ACTUAL, ESTIMATED
    val priority: String, // ESSENTIAL, IMPORTANT, OPTIONAL
    val nature: String, // FIXED, VARIABLE
    val recurrence: String, // NONE, DAILY, WEEKLY, MONTHLY, etc.
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "liabilities")
data class LiabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val originalAmount: Double,
    val remainingBalance: Double,
    val monthlyPayment: Double,
    val interestRate: Double,
    val dueDate: String,
    val totalInstallments: Int,
    val remainingInstallments: Int,
    val currency: String
)

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val purchasePrice: Double,
    val estimatedValue: Double,
    val monthlyIncome: Double,
    val rentalValue: Double,
    val status: String,
    val liquiditySpeedDays: Int,
    val conditionScore: Int,
    val currency: String
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val currentSpent: Double,
    val currency: String
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadlineMonths: Int,
    val monthlyContribution: Double,
    val currency: String
)

@Entity(tableName = "saved_scenarios")
data class SavedScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val scenarioType: String,
    val durationMonths: Int,
    val initialCost: Double,
    val recurringMonthlyExpense: Double,
    val recurringMonthlyRevenue: Double,
    val revenueChangePercent: Double,
    val expenseChangePercent: Double,
    val growthRateMonthly: Double,
    val probabilityOfSuccess: Double,
    val assumptionsJson: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "advisor"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isHelpful: Boolean? = null
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionName: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
