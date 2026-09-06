package com.example.domain.engine

import com.example.domain.model.AssetCategory
import com.example.domain.model.AssetItem
import com.example.domain.model.AssetStatus
import com.example.domain.model.AssetValuationResult
import com.example.domain.model.LiquidityOption
import com.example.domain.model.RiskLevel

object ValuationEngine {

    fun valuateAsset(asset: AssetItem): AssetValuationResult {
        val annualIncome = asset.monthlyIncome * 12.0
        val baseMultiplier = when (asset.category) {
            AssetCategory.SAAS -> 3.5 // 3.5x - 5.0x ARR
            AssetCategory.DIGITAL -> 2.4 // 2.0x - 3.5x annual revenue
            AssetCategory.ECOMMERCE -> 2.2
            AssetCategory.SERVICE -> 1.5
            AssetCategory.INTELLECTUAL_PROPERTY -> 2.8
            AssetCategory.PHYSICAL -> 0.7 // depreciated replacement
            AssetCategory.REAL_ESTATE -> 12.0 // rental yield proxy
            AssetCategory.OTHER -> 1.8
        }

        val conditionFactor = (asset.conditionScore.toDouble() / 100.0).coerceIn(0.5, 1.2)

        val likelyValue = if (annualIncome > 0) {
            FinancialEngine.round2((annualIncome * baseMultiplier * conditionFactor) + (asset.purchasePrice * 0.2))
        } else {
            FinancialEngine.round2(asset.purchasePrice * conditionFactor * 0.85)
        }

        val lowValue = FinancialEngine.round2(likelyValue * 0.75)
        val highValue = FinancialEngine.round2(likelyValue * 1.35)

        val drivers = mutableListOf<String>()
        val risks = mutableListOf<String>()

        when (asset.category) {
            AssetCategory.DIGITAL, AssetCategory.SAAS -> {
                drivers.add("إيراد شهري متكرر وقابل للتوسع بدون تكاليف تشغيل ضخمة.")
                drivers.add("ملكية الكود والبرمجية والبنية التحتية بنسبة 100%.")
                risks.add("التقادم التقني وحاجة التطوير المستمر ومواكبة الأمان.")
            }
            AssetCategory.PHYSICAL, AssetCategory.REAL_ESTATE -> {
                drivers.add("قيمة مادية ملموسة وسهولة استخدام كضمان أو إعادة بيع.")
                risks.add("الإهلاك الطبيعي وتكاليف الصيانة الدورية.")
            }
            else -> {
                drivers.add("قدرة الأصل على توليد دخل وتلبية احتياج في السوق.")
                risks.add("اعتماد الأصل على الوقت والمجهود الشخصي للمشغل.")
            }
        }

        // Liquidity Options
        val liquidityOptions = generateLiquidityOptions(asset, likelyValue)

        return AssetValuationResult(
            asset = asset,
            valuationMethod = when (asset.category) {
                AssetCategory.SAAS, AssetCategory.DIGITAL -> "مضاعف الإيراد السنوي (SaaS Multiples)"
                AssetCategory.PHYSICAL -> "تكلفة الاستبدال مع الإهلاك (Replacement Cost)"
                else -> "طريقة التدفق النقدي والتقييم المقارن"
            },
            lowEstimate = lowValue,
            likelyEstimate = likelyValue,
            highEstimate = highValue,
            confidenceScore = if (annualIncome > 0) 85 else 68,
            valueDrivers = drivers,
            valueRisks = risks,
            liquidityOptions = liquidityOptions
        )
    }

    private fun generateLiquidityOptions(asset: AssetItem, likelyValue: Double): List<LiquidityOption> {
        val options = mutableListOf<LiquidityOption>()

        // 1. Direct Sale
        options.add(
            LiquidityOption(
                method = "DIRECT_SALE",
                titleAr = "البيع المباشر (تسييل كلي)",
                expectedCash = FinancialEngine.round2(likelyValue * 0.90),
                timeToCashDays = asset.liquiditySpeedDays,
                effortLevel = "متوسط",
                riskLevel = RiskLevel.LOW,
                potentialUpside = "سيولة نقدية فورية كاملة تدعم الـ Runway بشكل فوري.",
                description = "عرض الأصل للبيع المباشر في منصات الاستحواذ أو لمستثمر مهتم."
            )
        )

        // 2. Rental / Leasing
        val monthlyRent = if (asset.rentalValue > 0) asset.rentalValue else (likelyValue * 0.05)
        options.add(
            LiquidityOption(
                method = "RENTAL",
                titleAr = "التأجير التشغيلي / الإعارة",
                expectedCash = FinancialEngine.round2(monthlyRent * 12.0),
                timeToCashDays = 14,
                effortLevel = "منخفض",
                riskLevel = RiskLevel.LOW,
                potentialUpside = "الحفاظ على ملكية الأصل مع الحصول على عائد شهري مستمر قدره ${monthlyRent.toInt()}$.",
                description = "تأجير المعدات أو الترخيص الاستخدام لأطراف أخرى دون نقل الملكية."
            )
        )

        // 3. Licensing / Whitelabel
        if (asset.category == AssetCategory.DIGITAL || asset.category == AssetCategory.SAAS || asset.category == AssetCategory.INTELLECTUAL_PROPERTY) {
            options.add(
                LiquidityOption(
                    method = "LICENSING",
                    titleAr = "ترخيص العلامة / Whitelabeling",
                    expectedCash = FinancialEngine.round2(likelyValue * 0.40),
                    timeToCashDays = 21,
                    effortLevel = "منخفض",
                    riskLevel = RiskLevel.MODERATE,
                    potentialUpside = "تكرار بيع رخص الاستخدام لشركات أخرى دون المساس بالنسخة الأصلية.",
                    description = "بيع حقوق استخدام أو استضافة مخصصة لشركات خارجية بمقابل شهري أو سنوي."
                )
            )
        }

        // 4. Revenue Share / Partnership
        options.add(
            LiquidityOption(
                method = "REV_SHARE",
                titleAr = "الشراكة بنسبة من الأرباح",
                expectedCash = FinancialEngine.round2(likelyValue * 0.60),
                timeToCashDays = 30,
                effortLevel = "متوسط",
                riskLevel = RiskLevel.HIGH,
                potentialUpside = "إدخال شريك تشغيلي يتولى التسويق والإدارة مقابل نسبة من الإيراد.",
                description = "تسليم الأصل لشريك نشط لزيادة الدخل وتحويله إلى دخل سلبي شبه كامل."
            )
        )

        return options
    }
}
