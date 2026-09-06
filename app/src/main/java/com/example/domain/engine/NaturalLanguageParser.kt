package com.example.domain.engine

import com.example.domain.model.ScenarioParameters
import com.example.domain.model.ScenarioType
import java.util.regex.Pattern

object NaturalLanguageParser {

    fun parseArabicPrompt(prompt: String): ScenarioParameters {
        val clean = prompt.trim()
        val numbers = extractNumbers(clean)

        var scenarioType = ScenarioType.CUSTOM
        var name = "سيناريو محاكى: ${clean.take(28)}"

        if (clean.contains("توظيف") || clean.contains("موظف") || clean.contains("راتب") || clean.contains("مساعد")) {
            scenarioType = ScenarioType.HIRE_EMPLOYEE
            name = "توظيف موظف أو مساعد"
        } else if (clean.contains("شراء") || clean.contains("جهاز") || clean.contains("معدة") || clean.contains("أصل")) {
            scenarioType = ScenarioType.BUY_ASSET
            name = "شراء أصل / معدات"
        } else if (clean.contains("استئجار") || clean.contains("إيجار") || clean.contains("أجر")) {
            scenarioType = ScenarioType.RENT_ASSET
            name = "استئجار أصل"
        } else if (clean.contains("رفع السعر") || clean.contains("زيادة السعر") || clean.contains("أسعار")) {
            scenarioType = ScenarioType.RAISE_PRICES
            name = "رفع الأسعار"
        } else if (clean.contains("انخفاض") || clean.contains("خسارة") || clean.contains("فقدان")) {
            scenarioType = ScenarioType.REVENUE_DROP
            name = "انخفاض مفاجئ في الدخل"
        } else if (clean.contains("بيع") || clean.contains("تسييل")) {
            scenarioType = ScenarioType.SELL_ASSET
            name = "بيع أصل وتسييل"
        } else if (clean.contains("منتج جديد") || clean.contains("إطلاق") || clean.contains("تطبيق")) {
            scenarioType = ScenarioType.LAUNCH_PRODUCT
            name = "إطلاق منتج أو خدمة جديدة"
        }

        var initialCost = 0.0
        var recurringMonthlyExpense = 0.0
        var recurringMonthlyRevenue = 0.0
        var durationMonths = 12

        // Extract duration in months
        val monthPattern = Pattern.compile("(\\d+)\\s*(أشهر|شهر|شهور|months?)")
        val monthMatcher = monthPattern.matcher(clean)
        if (monthMatcher.find()) {
            durationMonths = monthMatcher.group(1)?.toIntOrNull()?.coerceIn(1, 36) ?: 12
        } else if (clean.contains("سنة") || clean.contains("عام")) {
            durationMonths = 12
        } else if (clean.contains("سنتين") || clean.contains("عامين")) {
            durationMonths = 24
        }

        // Intelligently assign numbers based on semantics or order
        if (numbers.isNotEmpty()) {
            if (scenarioType == ScenarioType.HIRE_EMPLOYEE) {
                // First number usually monthly salary, second might be initial equipment or expected revenue
                recurringMonthlyExpense = numbers.getOrNull(0) ?: 800.0
                if (numbers.size >= 2) {
                    if (numbers[1] > recurringMonthlyExpense) {
                        recurringMonthlyRevenue = numbers[1]
                    } else {
                        initialCost = numbers[1]
                    }
                }
                if (numbers.size >= 3) {
                    recurringMonthlyRevenue = numbers[2]
                }
            } else if (scenarioType == ScenarioType.BUY_ASSET) {
                initialCost = numbers.getOrNull(0) ?: 2000.0
                if (numbers.size >= 2) recurringMonthlyExpense = numbers[1]
                if (numbers.size >= 3) recurringMonthlyRevenue = numbers[2]
            } else if (scenarioType == ScenarioType.RENT_ASSET) {
                recurringMonthlyExpense = numbers.getOrNull(0) ?: 300.0
                if (numbers.size >= 2) initialCost = numbers[1]
                if (numbers.size >= 3) recurringMonthlyRevenue = numbers[2]
            } else if (scenarioType == ScenarioType.RAISE_PRICES) {
                val percent = numbers.getOrNull(0) ?: 15.0
                return ScenarioParameters(
                    name = name,
                    type = scenarioType,
                    durationMonths = durationMonths,
                    initialCost = 0.0,
                    recurringMonthlyExpense = 0.0,
                    recurringMonthlyRevenue = 0.0,
                    revenueChangePercent = percent,
                    assumptions = listOf("تحليل استجابة التسعير بناء على الإدخال: $clean")
                )
            } else {
                if (numbers.size == 1) {
                    if (clean.contains("دخل") || clean.contains("إيراد") || clean.contains("أرباح")) {
                        recurringMonthlyRevenue = numbers[0]
                    } else {
                        initialCost = numbers[0]
                    }
                } else if (numbers.size >= 2) {
                    initialCost = numbers[0]
                    recurringMonthlyRevenue = numbers[1]
                }
            }
        } else {
            // Default heuristics if no explicit numbers found
            when (scenarioType) {
                ScenarioType.HIRE_EMPLOYEE -> {
                    recurringMonthlyExpense = 750.0
                    recurringMonthlyRevenue = 1200.0
                    initialCost = 300.0
                }
                ScenarioType.BUY_ASSET -> {
                    initialCost = 2500.0
                    recurringMonthlyRevenue = 600.0
                }
                ScenarioType.RENT_ASSET -> {
                    recurringMonthlyExpense = 250.0
                    recurringMonthlyRevenue = 600.0
                }
                else -> {
                    initialCost = 1000.0
                    recurringMonthlyRevenue = 400.0
                }
            }
        }

        return ScenarioParameters(
            name = name,
            type = scenarioType,
            durationMonths = durationMonths,
            initialCost = initialCost,
            recurringMonthlyExpense = recurringMonthlyExpense,
            recurringMonthlyRevenue = recurringMonthlyRevenue,
            assumptions = listOf(
                "تم استخراج المعطيات آلياً من النص: \"$clean\"",
                "تكلفة أولية: $initialCost$ | مصروف شهري: $recurringMonthlyExpense$ | إيراد شهري: $recurringMonthlyRevenue$"
            ),
            notes = clean
        )
    }

    private fun extractNumbers(text: String): List<Double> {
        // Match numbers including decimals and Arabic digits
        val normalized = text
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
            .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')

        val list = mutableListOf<Double>()
        val pattern = Pattern.compile("(\\d+(\\.\\d+)?)")
        val matcher = pattern.matcher(normalized)
        while (matcher.find()) {
            val num = matcher.group(1)?.toDoubleOrNull()
            if (num != null && num > 0) {
                list.add(num)
            }
        }
        return list
    }
}
