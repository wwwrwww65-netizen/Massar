package com.example.domain.engine

import com.example.domain.model.ExpenseNature
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.ForecastPoint
import com.example.domain.model.LiabilityItem
import com.example.domain.model.RecurrenceType
import com.example.domain.model.RiskLevel
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.UserProfile
import com.example.domain.model.ValueType
import java.math.BigDecimal
import java.math.RoundingMode

object FinancialEngine {

    fun calculateMetrics(
        profile: UserProfile,
        transactions: List<TransactionItem>,
        liabilities: List<LiabilityItem>
    ): FinancialMetrics {
        // Base numbers from transactions or profile fallback
        var monthlyIncome = 0.0
        var monthlyExpenses = 0.0
        var fixedExpenses = 0.0
        var estimatedCount = 0
        var totalCount = transactions.size

        if (transactions.isNotEmpty()) {
            for (item in transactions) {
                if (item.valueType == ValueType.ESTIMATED) estimatedCount++
                val monthlyAmount = when (item.recurrence) {
                    RecurrenceType.DAILY -> item.amount * 30.0
                    RecurrenceType.WEEKLY -> item.amount * 4.33
                    RecurrenceType.MONTHLY -> item.amount
                    RecurrenceType.QUARTERLY -> item.amount / 3.0
                    RecurrenceType.YEARLY -> item.amount / 12.0
                    RecurrenceType.NONE -> item.amount // treated as current month
                }

                when (item.type) {
                    TransactionType.INCOME -> monthlyIncome += monthlyAmount
                    TransactionType.EXPENSE -> {
                        monthlyExpenses += monthlyAmount
                        if (item.nature == ExpenseNature.FIXED) {
                            fixedExpenses += monthlyAmount
                        }
                    }
                    TransactionType.TRANSFER -> {}
                }
            }
        } else {
            monthlyIncome = profile.monthlyRevenue
            monthlyExpenses = profile.monthlyExpenses
            fixedExpenses = profile.monthlyExpenses * 0.65 // reasonable baseline estimate
        }

        // Add monthly debt obligations
        val debtMonthly = liabilities.sumOf { it.monthlyPayment }
        monthlyExpenses += debtMonthly
        fixedExpenses += debtMonthly

        val netCashFlow = round2(monthlyIncome - monthlyExpenses)
        val burnRate = if (netCashFlow < 0) round2(-netCashFlow) else 0.0

        // Runway calculation (months)
        val runwayMonths = if (burnRate > 0) {
            round2(profile.currentCapital / burnRate)
        } else {
            999.0 // Infinite / Cash flow positive
        }

        // Break even monthly revenue
        val breakEvenRevenue = round2(monthlyExpenses)

        // Fixed expenses ratio
        val fixedRatio = if (monthlyExpenses > 0) round2((fixedExpenses / monthlyExpenses) * 100.0) else 0.0

        // Revenue concentration estimate
        val concentrationRisk = 45.0

        // Overall risk determination
        val overallRisk = when {
            runwayMonths < 3.0 -> RiskLevel.CRITICAL
            runwayMonths < 6.0 -> RiskLevel.HIGH
            runwayMonths < 12.0 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        // Confidence score based on actual vs estimated data
        val confidence = if (totalCount > 0) {
            val actualRatio = (totalCount - estimatedCount).toDouble() / totalCount.toDouble()
            (50 + (actualRatio * 45)).toInt().coerceIn(50, 95)
        } else {
            70 // Baseline setup confidence
        }

        val dataNotice = if (estimatedCount > 0 || totalCount == 0) {
            "تعتمد بعض الحسابات على قيم تقديرية، مما قد يؤثر على هامش التوقع."
        } else {
            "بيانات فعلية وموثقة تعطي دقة توقع عالية."
        }

        return FinancialMetrics(
            currentBalance = round2(profile.currentCapital),
            totalMonthlyRevenue = round2(monthlyIncome),
            totalMonthlyExpenses = round2(monthlyExpenses),
            netMonthlyCashFlow = netCashFlow,
            burnRate = burnRate,
            runwayMonths = runwayMonths,
            breakEvenRevenue = breakEvenRevenue,
            fixedExpensesRatio = fixedRatio,
            revenueConcentrationRisk = concentrationRisk,
            overallRiskLevel = overallRisk,
            confidenceScore = confidence,
            dataQualityNotice = dataNotice
        )
    }

    fun generateForecast(
        currentCapital: Double,
        monthlyRevenue: Double,
        monthlyExpenses: Double,
        months: Int = 12,
        revenueGrowthRate: Double = 0.02, // 2% monthly base growth
        expenseInflationRate: Double = 0.01 // 1% monthly expense increase
    ): List<ForecastPoint> {
        val points = mutableListOf<ForecastPoint>()
        var baseCash = currentCapital
        var bestCash = currentCapital
        var worstCash = currentCapital

        // Month 0
        points.add(
            ForecastPoint(
                monthIndex = 0,
                label = "الآن",
                baseCash = round2(baseCash),
                bestCash = round2(bestCash),
                worstCash = round2(worstCash),
                netCashFlow = round2(monthlyRevenue - monthlyExpenses)
            )
        )

        for (m in 1..months) {
            // Base case
            val baseRev = monthlyRevenue * Math.pow(1.0 + revenueGrowthRate, m.toDouble())
            val baseExp = monthlyExpenses * Math.pow(1.0 + expenseInflationRate, m.toDouble())
            val baseNet = baseRev - baseExp
            baseCash += baseNet

            // Best case (optimistic: +15% revenue, flat expenses)
            val bestRev = monthlyRevenue * 1.15 * Math.pow(1.0 + (revenueGrowthRate * 1.5), m.toDouble())
            val bestExp = monthlyExpenses * Math.pow(1.0 + (expenseInflationRate * 0.5), m.toDouble())
            bestCash += (bestRev - bestExp)

            // Worst case (pessimistic: -20% revenue, +10% expenses)
            val worstRev = monthlyRevenue * 0.80 * Math.pow(1.0 - 0.01, m.toDouble())
            val worstExp = monthlyExpenses * 1.10 * Math.pow(1.0 + (expenseInflationRate * 1.5), m.toDouble())
            worstCash += (worstRev - worstExp)

            points.add(
                ForecastPoint(
                    monthIndex = m,
                    label = "شهر $m",
                    baseCash = round2(baseCash),
                    bestCash = round2(bestCash),
                    worstCash = round2(worstCash),
                    netCashFlow = round2(baseNet)
                )
            )
        }

        return points
    }

    fun calculateROI(initialCost: Double, totalNetGain: Double): Double {
        if (initialCost <= 0) return 100.0
        val roi = ((totalNetGain - initialCost) / initialCost) * 100.0
        return round2(roi)
    }

    fun calculatePaybackPeriod(initialCost: Double, additionalMonthlyCash: Double): Double {
        if (additionalMonthlyCash <= 0) return 99.0
        return round2(initialCost / additionalMonthlyCash)
    }

    fun round2(value: Double): Double {
        return try {
            BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        } catch (e: Exception) {
            value
        }
    }
}
