package com.example.domain.engine

import com.example.domain.model.FinancialMetrics
import com.example.domain.model.LiabilityItem
import com.example.domain.model.RiskLevel
import com.example.domain.model.TransactionItem

data class DetailedRiskBreakdown(
    val liquidityRisk: RiskLevel,
    val liquidityRiskScore: Int,
    val runwayRisk: RiskLevel,
    val runwayRiskScore: Int,
    val fixedCostRisk: RiskLevel,
    val fixedCostRiskScore: Int,
    val debtRisk: RiskLevel,
    val debtRiskScore: Int,
    val concentrationRisk: RiskLevel,
    val concentrationRiskScore: Int,
    val overallScore: Int,
    val topRiskWarnings: List<String>
)

object RiskEngine {

    fun evaluateRisks(
        metrics: FinancialMetrics,
        transactions: List<TransactionItem>,
        liabilities: List<LiabilityItem>
    ): DetailedRiskBreakdown {
        val warnings = mutableListOf<String>()

        // 1. Runway Risk
        val runwayScore = when {
            metrics.runwayMonths < 3.0 -> 90
            metrics.runwayMonths < 6.0 -> 65
            metrics.runwayMonths < 12.0 -> 35
            else -> 10
        }
        val runwayRisk = getLevel(runwayScore)
        if (runwayScore >= 65) {
            warnings.add("فترة البقاء المالي (Runway) قصيرة (${metrics.runwayMonths} أشهر). يُنصح ببناء احتياطي إضافي عاجل.")
        }

        // 2. Fixed Cost Risk
        val fixedScore = when {
            metrics.fixedExpensesRatio > 75.0 -> 85
            metrics.fixedExpensesRatio > 55.0 -> 55
            metrics.fixedExpensesRatio > 35.0 -> 30
            else -> 15
        }
        val fixedRisk = getLevel(fixedScore)
        if (fixedScore >= 55) {
            warnings.add("نسبة التكاليف الثابتة مرتفعة (${metrics.fixedExpensesRatio}%)، مما يقلل من مرونة التكيف مع انخفاض الدخل.")
        }

        // 3. Debt Obligations Risk
        val totalDebt = liabilities.sumOf { it.remainingBalance }
        val monthlyDebt = liabilities.sumOf { it.monthlyPayment }
        val debtRatio = if (metrics.totalMonthlyRevenue > 0) (monthlyDebt / metrics.totalMonthlyRevenue) * 100.0 else 0.0
        val debtScore = when {
            debtRatio > 40.0 -> 90
            debtRatio > 25.0 -> 60
            debtRatio > 10.0 -> 35
            totalDebt > 0 -> 20
            else -> 5
        }
        val debtRisk = getLevel(debtScore)
        if (debtScore >= 60) {
            warnings.add("التزامات الديون الشهرية تستقطع ${debtRatio.toInt()}% من إجمالي الإيرادات، وهو معدل يستوجب الحذر.")
        }

        // 4. Liquidity Risk
        val liquidityScore = when {
            metrics.currentBalance < metrics.totalMonthlyExpenses * 2 -> 80
            metrics.currentBalance < metrics.totalMonthlyExpenses * 4 -> 50
            metrics.currentBalance < metrics.totalMonthlyExpenses * 6 -> 25
            else -> 10
        }
        val liquidityRisk = getLevel(liquidityScore)
        if (liquidityScore >= 50) {
            warnings.add("السيولة النقدية تغطي أقل من 4 أشهر من المصاريف التشغيلية.")
        }

        // 5. Concentration Risk
        val concentrationScore = 40
        val concentrationRisk = getLevel(concentrationScore)

        val overallScore = ((runwayScore * 0.35) + (liquidityScore * 0.25) + (fixedScore * 0.20) + (debtScore * 0.20)).toInt()

        if (warnings.isEmpty()) {
            warnings.add("الوضع المالي مستقر ومتوازن مع مؤشرات أمان جيدة.")
        }

        return DetailedRiskBreakdown(
            liquidityRisk = liquidityRisk,
            liquidityRiskScore = liquidityScore,
            runwayRisk = runwayRisk,
            runwayRiskScore = runwayScore,
            fixedCostRisk = fixedRisk,
            fixedCostRiskScore = fixedScore,
            debtRisk = debtRisk,
            debtRiskScore = debtScore,
            concentrationRisk = concentrationRisk,
            concentrationRiskScore = concentrationScore,
            overallScore = overallScore,
            topRiskWarnings = warnings
        )
    }

    private fun getLevel(score: Int): RiskLevel = when {
        score >= 75 -> RiskLevel.CRITICAL
        score >= 50 -> RiskLevel.HIGH
        score >= 30 -> RiskLevel.MODERATE
        else -> RiskLevel.LOW
    }
}
