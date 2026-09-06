package com.example.domain.engine

import com.example.domain.model.FinancialMetrics
import com.example.domain.model.ForecastPoint
import com.example.domain.model.RiskLevel
import com.example.domain.model.ScenarioParameters
import com.example.domain.model.ScenarioResult
import com.example.domain.model.ScenarioType
import kotlin.math.max
import kotlin.math.min

object ScenarioEngine {

    fun simulate(
        scenario: ScenarioParameters,
        baseMetrics: FinancialMetrics
    ): ScenarioResult {
        val duration = max(1, scenario.durationMonths)
        var currentCash = baseMetrics.currentBalance - scenario.initialCost
        var lowestCash = currentCash
        var recoveryMonth = -1
        var accumulatedAdditionalNet = 0.0

        val timeline = mutableListOf<ForecastPoint>()
        timeline.add(
            ForecastPoint(
                monthIndex = 0,
                label = "البداية",
                baseCash = FinancialEngine.round2(currentCash),
                bestCash = FinancialEngine.round2(currentCash),
                worstCash = FinancialEngine.round2(currentCash),
                netCashFlow = -scenario.initialCost
            )
        )

        val baseMonthlyRev = baseMetrics.totalMonthlyRevenue * (1.0 + (scenario.revenueChangePercent / 100.0)) + scenario.recurringMonthlyRevenue
        val baseMonthlyExp = baseMetrics.totalMonthlyExpenses * (1.0 + (scenario.expenseChangePercent / 100.0)) + scenario.recurringMonthlyExpense

        var bestCash = currentCash
        var worstCash = currentCash

        for (m in 1..duration) {
            // Base growth
            val growthMultiplier = Math.pow(1.0 + (scenario.growthRateMonthly / 100.0), m.toDouble())
            val monthRev = baseMonthlyRev * growthMultiplier
            val monthExp = baseMonthlyExp
            val monthNet = monthRev - monthExp

            currentCash += monthNet
            lowestCash = min(lowestCash, currentCash)

            val monthAdditional = (monthRev - baseMetrics.totalMonthlyRevenue) - (monthExp - baseMetrics.totalMonthlyExpenses)
            accumulatedAdditionalNet += monthAdditional
            if (recoveryMonth == -1 && accumulatedAdditionalNet >= scenario.initialCost) {
                recoveryMonth = m
            }

            // Best case (+20% revenue performance)
            val bestRev = monthRev * 1.20
            val bestExp = monthExp * 0.95
            bestCash += (bestRev - bestExp)

            // Worst case (-25% revenue performance, +10% expense)
            val worstRev = monthRev * 0.75
            val worstExp = monthExp * 1.10
            worstCash += (worstRev - worstExp)

            timeline.add(
                ForecastPoint(
                    monthIndex = m,
                    label = "شهر $m",
                    baseCash = FinancialEngine.round2(currentCash),
                    bestCash = FinancialEngine.round2(bestCash),
                    worstCash = FinancialEngine.round2(worstCash),
                    netCashFlow = FinancialEngine.round2(monthNet)
                )
            )
        }

        val finalNetFlow = baseMonthlyRev - baseMonthlyExp
        val projectedBurn = if (finalNetFlow < 0) -finalNetFlow else 0.0
        val projectedRunway = if (projectedBurn > 0) {
            FinancialEngine.round2(max(0.0, currentCash) / projectedBurn)
        } else {
            999.0
        }

        val totalAdditionalGains = accumulatedAdditionalNet
        val roi = FinancialEngine.calculateROI(scenario.initialCost, totalAdditionalGains)
        val paybackMonths = if (scenario.initialCost > 0 && (baseMonthlyRev - baseMonthlyExp - baseMetrics.netMonthlyCashFlow) > 0) {
            FinancialEngine.calculatePaybackPeriod(
                scenario.initialCost,
                (baseMonthlyRev - baseMonthlyExp - baseMetrics.netMonthlyCashFlow)
            )
        } else if (scenario.initialCost == 0.0) {
            0.0
        } else {
            99.0
        }

        val riskLevel = when {
            lowestCash < 0 -> RiskLevel.CRITICAL
            projectedRunway < 3.0 -> RiskLevel.CRITICAL
            projectedRunway < 6.0 -> RiskLevel.HIGH
            projectedRunway < 12.0 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        val cashDiff = FinancialEngine.round2(currentCash - (baseMetrics.currentBalance + (baseMetrics.netMonthlyCashFlow * duration)))
        val runwayDiff = FinancialEngine.round2(projectedRunway - baseMetrics.runwayMonths)

        return ScenarioResult(
            scenario = scenario,
            baseMetrics = baseMetrics,
            projectedCashAfterDuration = FinancialEngine.round2(currentCash),
            projectedRunwayMonths = projectedRunway,
            lowestCashPoint = FinancialEngine.round2(lowestCash),
            recoveryPointMonth = if (recoveryMonth != -1) recoveryMonth else 0,
            roiPercentage = roi,
            paybackPeriodMonths = paybackMonths,
            forecastTimeline = timeline,
            riskLevel = riskLevel,
            cashDifference = cashDiff,
            runwayDifference = runwayDiff
        )
    }

    fun getPredefinedTemplates(): List<ScenarioParameters> {
        return listOf(
            ScenarioParameters(
                name = "شراء معدات / أجهزة جديدة",
                type = ScenarioType.BUY_ASSET,
                durationMonths = 12,
                initialCost = 3000.0,
                recurringMonthlyExpense = 50.0,
                recurringMonthlyRevenue = 800.0,
                revenueChangePercent = 0.0,
                expenseChangePercent = 0.0,
                growthRateMonthly = 2.0,
                assumptions = listOf("زيادة القدرة الإنتاجية", "تكلفة صيانة 50$ شهرياً", "إيراد إضافي متوقع 800$")
            ),
            ScenarioParameters(
                name = "استئجار معدات بدلاً من الشراء",
                type = ScenarioType.RENT_ASSET,
                durationMonths = 12,
                initialCost = 300.0,
                recurringMonthlyExpense = 250.0,
                recurringMonthlyRevenue = 800.0,
                revenueChangePercent = 0.0,
                expenseChangePercent = 0.0,
                growthRateMonthly = 2.0,
                assumptions = listOf("حماية السيولة النقدية الأولية", "مرونة عالية في الإلغاء", "تكلفة إيجار شهرية مستمرة")
            ),
            ScenarioParameters(
                name = "توظيف مساعد / مسوق",
                type = ScenarioType.HIRE_EMPLOYEE,
                durationMonths = 12,
                initialCost = 500.0,
                recurringMonthlyExpense = 800.0,
                recurringMonthlyRevenue = 1400.0,
                revenueChangePercent = 0.0,
                expenseChangePercent = 0.0,
                growthRateMonthly = 3.0,
                assumptions = listOf("راتب شهري 800$", "فترة تهيئة شهرين", "عائد مبيعات متوقع 1400$ بعد الشهر الثاني")
            ),
            ScenarioParameters(
                name = "رفع الأسعار بنسبة 20%",
                type = ScenarioType.RAISE_PRICES,
                durationMonths = 12,
                initialCost = 0.0,
                recurringMonthlyExpense = 0.0,
                recurringMonthlyRevenue = 0.0,
                revenueChangePercent = 20.0,
                expenseChangePercent = 0.0,
                growthRateMonthly = 0.0,
                assumptions = listOf("زيادة متوسط الفاتورة 20%", "افتراض ثبات 95% من العملاء الحاليين")
            ),
            ScenarioParameters(
                name = "فقدان أكبر عميل (-30% إيرادات)",
                type = ScenarioType.LOSE_CLIENT,
                durationMonths = 12,
                initialCost = 0.0,
                recurringMonthlyExpense = 0.0,
                recurringMonthlyRevenue = 0.0,
                revenueChangePercent = -30.0,
                expenseChangePercent = 0.0,
                growthRateMonthly = 0.0,
                assumptions = listOf("انخفاض مفاجئ في الدخل بنسبة 30%", "اختبار متانة الـ Runway والسيولة")
            ),
            ScenarioParameters(
                name = "ترشيد المصروفات بنسبة 15%",
                type = ScenarioType.REDUCE_EXPENSE,
                durationMonths = 12,
                initialCost = 0.0,
                recurringMonthlyExpense = 0.0,
                recurringMonthlyRevenue = 0.0,
                revenueChangePercent = 0.0,
                expenseChangePercent = -15.0,
                growthRateMonthly = 0.0,
                assumptions = listOf("إلغاء الاشتراكات غير الضرورية", "التفاوض على تكاليف الاستضافة والخدمات")
            )
        )
    }
}
