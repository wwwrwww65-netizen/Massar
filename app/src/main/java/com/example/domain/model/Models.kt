package com.example.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

enum class BusinessType(val titleAr: String, val iconName: String) {
    FREELANCER("مستقل / Freelancer", "person"),
    ECOMMERCE("تجارة إلكترونية", "shopping_cart"),
    STORE("متجر محلي / نقطة بيع", "storefront"),
    SAAS("برمجيات كخدمة SaaS", "cloud"),
    DIGITAL_PRODUCT("تطبيق / منتج رقمي", "phone_iphone"),
    SERVICES("مقدم خدمات", "business_center"),
    CONSULTING("استشارات وتدريب", "psychology"),
    MANUFACTURING("صناعة وتشغيل", "factory"),
    ASSET_MANAGEMENT("إدارة أصول واستثمارات", "account_balance"),
    STARTUP("مشروع ناشئ / Startup", "rocket_launch"),
    OTHER("أخرى", "more_horiz")
}

enum class ValueType {
    ACTUAL,
    ESTIMATED
}

enum class TransactionType(val titleAr: String) {
    INCOME("إيراد / دخل"),
    EXPENSE("مصروف"),
    TRANSFER("تحويل")
}

enum class ExpensePriority(val titleAr: String, val badgeColorHex: Long) {
    ESSENTIAL("أساسي لا غنى عنه", 0xFFEF4444),
    IMPORTANT("مهم وقابل للتحسين", 0xFFF59E0B),
    OPTIONAL("اختياري يمكن الاستغناء عنه", 0xFF10B981)
}

enum class ExpenseNature(val titleAr: String) {
    FIXED("ثابت"),
    VARIABLE("متغير")
}

enum class RecurrenceType(val titleAr: String, val multiplierPerMonth: Double) {
    NONE("غير متكرر", 0.0),
    DAILY("يومي", 30.0),
    WEEKLY("أسبوعي", 4.33),
    MONTHLY("شهري", 1.0),
    QUARTERLY("ربع سنوي", 0.333),
    YEARLY("سنوي", 0.0833)
}

enum class AssetCategory(val titleAr: String) {
    DIGITAL("منتج رقمي / كود", ),
    SAAS("مشروع SaaS", ),
    ECOMMERCE("متجر إلكتروني", ),
    PHYSICAL("أصل مادي / معدات", ),
    SERVICE("خدمة تجارية", ),
    INTELLECTUAL_PROPERTY("ملكية فكرية / علامة", ),
    REAL_ESTATE("عقار / مكتب", ),
    OTHER("أصل آخر", )
}

enum class AssetStatus(val titleAr: String) {
    ACTIVE("نشط ومستمر"),
    FOR_SALE("معروض للبيع"),
    FOR_RENT("معروض للتأجير"),
    LIQUIDATED("تم تسييله"),
    ARCHIVED("مؤرشف")
}

enum class RiskLevel(val titleAr: String, val colorHex: Long) {
    LOW("منخفض", 0xFF10B981),
    MODERATE("متوسط", 0xFFF59E0B),
    HIGH("مرتفع", 0xFFF97316),
    CRITICAL("حرج / خطير", 0xFFEF4444)
}

enum class ScenarioType(val titleAr: String) {
    BUY_ASSET("شراء أصل / معدات"),
    RENT_ASSET("استئجار أصل"),
    SELL_ASSET("بيع أصل"),
    HIRE_EMPLOYEE("توظيف موظف جديد"),
    EXPAND_BUSINESS("توسيع النشاط"),
    LAUNCH_PRODUCT("إطلاق منتج جديد"),
    RAISE_PRICES("رفع الأسعار"),
    LOWER_PRICES("خفض الأسعار"),
    INCREASE_EXPENSE("زيادة مصروفات تشغيلية"),
    REDUCE_EXPENSE("ترشيد وتخفيض المصروفات"),
    REVENUE_DROP("انخفاض الإيرادات"),
    REVENUE_INCREASE("زيادة الإيرادات"),
    LOSE_CLIENT("فقدان عميل رئيسي"),
    ACQUIRE_CLIENT("كسب عميل كبير"),
    DEBT_PAYOFF("سداد ديون والتزامات"),
    GET_FUNDING("الحصول على تمويل / استثمار"),
    EMERGENCY_EXPENSE("مصروف طارئ غير متوقع"),
    SWITCH_TO_SAAS("التحول لنموذج الاشتراكات"),
    CUSTOM("سيناريو مخصص")
}

data class UserProfile(
    val id: String = "default_user",
    val businessName: String = "مشروعي",
    val businessType: BusinessType = BusinessType.FREELANCER,
    val baseCurrency: String = "USD",
    val currentCapital: Double = 8000.0,
    val monthlyRevenue: Double = 3000.0,
    val monthlyExpenses: Double = 2200.0,
    val selectedGoals: List<String> = listOf("المحافظة على السيولة", "بناء احتياطي مالي"),
    val onboardingCompleted: Boolean = false,
    val isPro: Boolean = true
)

data class TransactionItem(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: String,
    val currency: String = "USD",
    val valueType: ValueType = ValueType.ACTUAL,
    val priority: ExpensePriority = ExpensePriority.IMPORTANT,
    val nature: ExpenseNature = ExpenseNature.FIXED,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val note: String = ""
)

data class LiabilityItem(
    val id: Long = 0,
    val title: String,
    val originalAmount: Double,
    val remainingBalance: Double,
    val monthlyPayment: Double,
    val interestRate: Double = 0.0,
    val dueDate: String = "",
    val totalInstallments: Int = 12,
    val remainingInstallments: Int = 12,
    val currency: String = "USD"
)

data class AssetItem(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: AssetCategory,
    val purchasePrice: Double,
    val estimatedValue: Double,
    val monthlyIncome: Double = 0.0,
    val rentalValue: Double = 0.0,
    val status: AssetStatus = AssetStatus.ACTIVE,
    val liquiditySpeedDays: Int = 30,
    val conditionScore: Int = 85, // 0 - 100
    val currency: String = "USD"
)

data class BudgetItem(
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val currentSpent: Double = 0.0,
    val currency: String = "USD"
) {
    val usagePercentage: Double
        get() = if (monthlyLimit > 0) (currentSpent / monthlyLimit) * 100.0 else 0.0

    val statusAlert: String
        get() = when {
            usagePercentage >= 100.0 -> "تجاوز الميزانية! (تحذير حرج)"
            usagePercentage >= 90.0 -> "اقتراب من الحد الأقصى 90%"
            usagePercentage >= 70.0 -> "تنبيه استهلاك 70%"
            else -> "في النطاق الآمن"
        }
}

data class GoalItem(
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadlineMonths: Int,
    val monthlyContribution: Double,
    val currency: String = "USD"
) {
    val progressPercentage: Double
        get() = if (targetAmount > 0) (currentAmount / targetAmount).coerceIn(0.0, 1.0) * 100.0 else 0.0
}

data class ScenarioParameters(
    val id: Long = 0,
    val name: String,
    val type: ScenarioType = ScenarioType.CUSTOM,
    val durationMonths: Int = 12,
    val initialCost: Double = 0.0,
    val recurringMonthlyExpense: Double = 0.0,
    val recurringMonthlyRevenue: Double = 0.0,
    val revenueChangePercent: Double = 0.0, // e.g. +20% or -15%
    val expenseChangePercent: Double = 0.0,
    val growthRateMonthly: Double = 0.0,
    val probabilityOfSuccess: Double = 80.0, // 0 - 100
    val assumptions: List<String> = emptyList(),
    val notes: String = ""
)

data class FinancialMetrics(
    val currentBalance: Double,
    val totalMonthlyRevenue: Double,
    val totalMonthlyExpenses: Double,
    val netMonthlyCashFlow: Double,
    val burnRate: Double,
    val runwayMonths: Double,
    val breakEvenRevenue: Double,
    val fixedExpensesRatio: Double,
    val revenueConcentrationRisk: Double,
    val overallRiskLevel: RiskLevel,
    val confidenceScore: Int,
    val dataQualityNotice: String = ""
)

data class ForecastPoint(
    val monthIndex: Int,
    val label: String,
    val baseCash: Double,
    val bestCash: Double,
    val worstCash: Double,
    val netCashFlow: Double
)

data class ScenarioResult(
    val scenario: ScenarioParameters,
    val baseMetrics: FinancialMetrics,
    val projectedCashAfterDuration: Double,
    val projectedRunwayMonths: Double,
    val lowestCashPoint: Double,
    val recoveryPointMonth: Int, // month index when initial cost is recovered
    val roiPercentage: Double,
    val paybackPeriodMonths: Double,
    val forecastTimeline: List<ForecastPoint>,
    val riskLevel: RiskLevel,
    val cashDifference: Double,
    val runwayDifference: Double
)

data class DecisionMatrixOption(
    val title: String,
    val description: String,
    val initialCost: Double,
    val monthlyImpact: Double,
    val runwayMonths: Double,
    val riskLevel: RiskLevel,
    val timeToBenefitMonths: Int,
    val flexibilityScore: Int, // 1 - 10
    val overallDecisionScore: Int, // 0 - 100
    val recommendationReason: String
)

data class DecisionAnalysisResult(
    val decisionTitle: String,
    val financialOutcomeSummary: String,
    val pros: List<String>,
    val cons: List<String>,
    val risks: List<String>,
    val opportunities: List<String>,
    val recommendedAction: String,
    val alternatives: List<DecisionMatrixOption>,
    val sensitivityInsights: List<String>,
    val assumptionsExplanation: String,
    val confidenceScore: Int
)

data class AssetValuationResult(
    val asset: AssetItem,
    val valuationMethod: String,
    val lowEstimate: Double,
    val likelyEstimate: Double,
    val highEstimate: Double,
    val confidenceScore: Int,
    val valueDrivers: List<String>,
    val valueRisks: List<String>,
    val liquidityOptions: List<LiquidityOption>
)

data class LiquidityOption(
    val method: String,
    val titleAr: String,
    val expectedCash: Double,
    val timeToCashDays: Int,
    val effortLevel: String, // منخفض, متوسط, مرتفع
    val riskLevel: RiskLevel,
    val potentialUpside: String,
    val description: String
)

data class BootstrappingOptimization(
    val currentEssentialExpenses: Double,
    val currentImportantExpenses: Double,
    val currentOptionalExpenses: Double,
    val totalMonthlySavingPotential: Double,
    val extendedRunwayMonths: Double,
    val recommendations: List<CostSavingRecommendation>
)

data class CostSavingRecommendation(
    val title: String,
    val category: String,
    val currentCost: Double,
    val estimatedSaving: Double,
    val alternativeSolution: String,
    val actionType: String // خفض، استبدال، إلغاء، دمج
)

data class AuditLog(
    val id: Long = 0,
    val actionName: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
