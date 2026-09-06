package com.example.domain.engine

import com.example.domain.model.BootstrappingOptimization
import com.example.domain.model.CostSavingRecommendation
import com.example.domain.model.ExpensePriority
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.RecurrenceType
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType

object BootstrappingEngine {

    fun analyzeBootstrapping(
        metrics: FinancialMetrics,
        transactions: List<TransactionItem>
    ): BootstrappingOptimization {
        var essential = 0.0
        var important = 0.0
        var optional = 0.0

        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

        for (item in expenses) {
            val monthlyAmount = when (item.recurrence) {
                RecurrenceType.DAILY -> item.amount * 30.0
                RecurrenceType.WEEKLY -> item.amount * 4.33
                RecurrenceType.MONTHLY -> item.amount
                RecurrenceType.QUARTERLY -> item.amount / 3.0
                RecurrenceType.YEARLY -> item.amount / 12.0
                RecurrenceType.NONE -> item.amount
            }

            when (item.priority) {
                ExpensePriority.ESSENTIAL -> essential += monthlyAmount
                ExpensePriority.IMPORTANT -> important += monthlyAmount
                ExpensePriority.OPTIONAL -> optional += monthlyAmount
            }
        }

        // If no categorized expenses, calculate baseline heuristic breakdown
        if (expenses.isEmpty() && metrics.totalMonthlyExpenses > 0) {
            essential = metrics.totalMonthlyExpenses * 0.55
            important = metrics.totalMonthlyExpenses * 0.30
            optional = metrics.totalMonthlyExpenses * 0.15
        }

        val savingPotential = (optional * 0.90) + (important * 0.25)
        val newMonthlyExpenses = maxOf(100.0, metrics.totalMonthlyExpenses - savingPotential)
        val newNet = metrics.totalMonthlyRevenue - newMonthlyExpenses
        val newBurn = if (newNet < 0) -newNet else 0.0
        val extendedRunway = if (newBurn > 0) {
            FinancialEngine.round2(metrics.currentBalance / newBurn)
        } else {
            999.0
        }

        val recommendations = mutableListOf<CostSavingRecommendation>()

        recommendations.add(
            CostSavingRecommendation(
                title = "إلغاء الاشتراكات والبرمجيات غير المستغلة",
                category = "برمجيات واشتراكات SaaS",
                currentCost = FinancialEngine.round2(optional),
                estimatedSaving = FinancialEngine.round2(optional * 0.85),
                alternativeSolution = "استخدام أدوات مفتوحة المصدر أو خطط مجانية أو إلغاء تراخيص المقاعد الزائدة.",
                actionType = "إلغاء فوري"
            )
        )

        recommendations.add(
            CostSavingRecommendation(
                title = "تحويل التكاليف الثابتة إلى متغيرة بنظام العمولة/الدفع بالاستخدام",
                category = "تسويق وخدمات",
                currentCost = FinancialEngine.round2(important * 0.5),
                estimatedSaving = FinancialEngine.round2(important * 0.20),
                alternativeSolution = "التعاقد مع مستقلين للمهام المحددة بدلاً من التكاليف الثابتة الشهرية.",
                actionType = "إعادة هيكلة"
            )
        )

        recommendations.add(
            CostSavingRecommendation(
                title = "التفاوض على الفواتير السنوية أو البنية التحتية",
                category = "سيرفرات واستضافة",
                currentCost = FinancialEngine.round2(essential * 0.25),
                estimatedSaving = FinancialEngine.round2(essential * 0.10),
                alternativeSolution = "الاستفادة من رصيد الشركات الناشئة (Cloud Credits) أو الانتقال للدفع السنوي بخصم 20%.",
                actionType = "تفاوض وتحسين"
            )
        )

        return BootstrappingOptimization(
            currentEssentialExpenses = FinancialEngine.round2(essential),
            currentImportantExpenses = FinancialEngine.round2(important),
            currentOptionalExpenses = FinancialEngine.round2(optional),
            totalMonthlySavingPotential = FinancialEngine.round2(savingPotential),
            extendedRunwayMonths = extendedRunway,
            recommendations = recommendations
        )
    }
}
