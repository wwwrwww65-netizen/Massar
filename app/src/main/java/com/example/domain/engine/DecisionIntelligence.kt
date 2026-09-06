package com.example.domain.engine

import com.example.domain.model.DecisionAnalysisResult
import com.example.domain.model.DecisionMatrixOption
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.RiskLevel
import com.example.domain.model.ScenarioParameters
import com.example.domain.model.ScenarioResult
import com.example.domain.model.ScenarioType
import kotlin.math.roundToInt

object DecisionIntelligence {

    fun analyzeDecision(
        scenario: ScenarioParameters,
        result: ScenarioResult,
        baseMetrics: FinancialMetrics
    ): DecisionAnalysisResult {
        val pros = mutableListOf<String>()
        val cons = mutableListOf<String>()
        val risks = mutableListOf<String>()
        val opportunities = mutableListOf<String>()
        val alternatives = mutableListOf<DecisionMatrixOption>()
        val sensitivities = mutableListOf<String>()

        val cashImpact = result.cashDifference
        val runwayDiff = result.runwayDifference

        // Outcome summary
        val outcomeSummary = if (scenario.initialCost > 0) {
            "الاستثمار يتطلب ${scenario.initialCost}$ مقدماً، مع عائد صافي متوقع $cashImpact$ خلال ${scenario.durationMonths} شهراً وفترة استرداد ${result.paybackPeriodMonths} شهراً."
        } else {
            "القرار يحدث أثراً شهرياً يؤدي إلى تغير صافي السيولة بمقدار $cashImpact$ مع مستوى مخاطر ${result.riskLevel.titleAr}."
        }

        // Pros
        if (scenario.recurringMonthlyRevenue > 0 || scenario.revenueChangePercent > 0) {
            pros.add("توليد تدفق نقدي إيجابي جديد يرفع إجمالي الإيرادات الشهرية.")
        }
        if (result.roiPercentage > 25.0) {
            pros.add("عائد استثماري واعد بنسبة ${result.roiPercentage.roundToInt()}% على المدى المتوسط.")
        }
        if (scenario.expenseChangePercent < 0) {
            pros.add("تقليل المصاريف التشغيلية مما يرفع هامش الأمان والسيولة الاحتياطية.")
        }
        if (pros.isEmpty()) {
            pros.add("يساعد في تحسين الكفاءة التشغيلية أو التواجد بالسوق.")
        }

        // Cons
        if (scenario.initialCost > baseMetrics.currentBalance * 0.3) {
            cons.add("تكلفة أولية تشكل ${(scenario.initialCost / baseMetrics.currentBalance * 100).roundToInt()}% من الرصيد النقدي المتاح حالياً.")
        }
        if (runwayDiff < -1.0) {
            cons.add("انخفاض في فترة الاستدامة (Runway) بمقدار ${-runwayDiff} شهراً.")
        }
        if (result.lowestCashPoint < baseMetrics.currentBalance * 0.25) {
            cons.add("الوصول لنقطة سيولة حرجة (${result.lowestCashPoint}$) قد تعرض المشروع لضغوط طارئة.")
        }
        if (cons.isEmpty()) {
            cons.add("التزام تشغيلي يتطلب متابعة مستمرة لتحقيق المستهدفات.")
        }

        // Risks
        if (scenario.recurringMonthlyRevenue > 0) {
            risks.add("مخاطر عدم تحقيق الإيراد الإضافي المتوقع (${scenario.recurringMonthlyRevenue}$) مما يثقل كاهل السيولة.")
        }
        if (scenario.initialCost > 0) {
            risks.add("تأخر فترة استرداد رأس المال إذا طرأت ظروف سوقية غير مواتية.")
        }
        risks.add("تغيرات في سلوك العملاء أو التكاليف التشغيلية الثابتة.")

        // Opportunities
        opportunities.add("إمكانية التوسع واستقطاب شريحة عملاء أوسع وتطوير نموذج التسعير.")
        opportunities.add("تحسين القدرة التنافسية وبناء أصول رقمية / تشغيلية طويلة الأجل.")

        // Recommendation formulation
        val recommendation = when {
            result.riskLevel == RiskLevel.CRITICAL ->
                "لا نوصي بالتنفيذ الفوري بشكله الحالي نظراً لتعريض السيولة لضغوط حرجة. البديل الأنسب هو خيار التأجير أو التقسيط أو تقليل النطاق المبدئي بنسبة 40%."
            result.riskLevel == RiskLevel.HIGH ->
                "القرار يحمل فرصاً جيدة ولكن يُفضل تأجيله شهرين لبناء احتياطي نقدي إضافي، أو البدء بنسخة تجريبية مخفضة التكاليف."
            result.roiPercentage > 30.0 && result.lowestCashPoint > baseMetrics.currentBalance * 0.4 ->
                "نوصي بالمضي قدماً في تنفيذ القرار؛ فهو يعزز التدفق الإيجابي ويحافظ على هامش أمان متين للـ Runway."
            else ->
                "القرار متوازن ومقبول المخاطر. نوصي بتتبع مؤشرات الإيراد أسبوعياً خلال أول 90 يوماً للتأكد من مواكبة التوقعات."
        }

        // Alternatives Decision Matrix
        // Option 1: Current plan
        val scoreA = calculateDecisionScore(
            costImpact = scenario.initialCost / maxOf(1.0, baseMetrics.currentBalance),
            roi = result.roiPercentage,
            runwayMonths = result.projectedRunwayMonths,
            risk = result.riskLevel,
            flexibility = 6
        )
        alternatives.add(
            DecisionMatrixOption(
                title = "الخيار أ: تنفيذ القرار الحالي (${scenario.name})",
                description = "تنفيذ كامل الخطة كما تم إدخالها في المحاكي.",
                initialCost = scenario.initialCost,
                monthlyImpact = scenario.recurringMonthlyRevenue - scenario.recurringMonthlyExpense,
                runwayMonths = result.projectedRunwayMonths,
                riskLevel = result.riskLevel,
                timeToBenefitMonths = if (result.recoveryPointMonth > 0) result.recoveryPointMonth else 3,
                flexibilityScore = 6,
                overallDecisionScore = scoreA,
                recommendationReason = "العائد الكامل مع الالتزام برأس المال المطلوب."
            )
        )

        // Option 2: Lean / Rental / Phased Alternative
        val leanCost = scenario.initialCost * 0.25
        val leanMonthlyNet = (scenario.recurringMonthlyRevenue * 0.85) - (scenario.recurringMonthlyExpense + (scenario.initialCost * 0.08))
        val leanRunway = if (leanMonthlyNet < 0) baseMetrics.currentBalance / -leanMonthlyNet else 999.0
        val scoreB = calculateDecisionScore(
            costImpact = leanCost / maxOf(1.0, baseMetrics.currentBalance),
            roi = result.roiPercentage * 0.9,
            runwayMonths = minOf(999.0, leanRunway),
            risk = RiskLevel.LOW,
            flexibility = 9
        )
        alternatives.add(
            DecisionMatrixOption(
                title = "الخيار ب: الاستئجار أو التنفيذ المرن (Lean Alternative)",
                description = "استئجار الخدمة/المعدات أو العمل بنظام الدفع عند الاستخدام لحماية السيولة الأولية.",
                initialCost = FinancialEngine.round2(leanCost),
                monthlyImpact = FinancialEngine.round2(leanMonthlyNet),
                runwayMonths = FinancialEngine.round2(minOf(24.0, leanRunway)),
                riskLevel = RiskLevel.LOW,
                timeToBenefitMonths = 1,
                flexibilityScore = 9,
                overallDecisionScore = scoreB,
                recommendationReason = "يحافظ على سيولة نقدية أعلى بنسبة 75% مع مرونة فائقة للإلغاء في أي وقت."
            )
        )

        // Option 3: Postpone / Build Cash Reserve
        val scoreC = calculateDecisionScore(
            costImpact = 0.0,
            roi = 0.0,
            runwayMonths = baseMetrics.runwayMonths,
            risk = RiskLevel.LOW,
            flexibility = 10
        )
        alternatives.add(
            DecisionMatrixOption(
                title = "الخيار ج: التأجيل لبناء احتياطي نقدي 3 أشهر",
                description = "تأجيل الخطوة 90 يوماً حتى يصل الرصيد الاحتياطي إلى مستوى أمان مضاعف.",
                initialCost = 0.0,
                monthlyImpact = 0.0,
                runwayMonths = baseMetrics.runwayMonths,
                riskLevel = RiskLevel.LOW,
                timeToBenefitMonths = 4,
                flexibilityScore = 10,
                overallDecisionScore = scoreC,
                recommendationReason = "الخيار الأكثر أماناً لحالات تذبذب الدخل أو قلة السيولة الاحتياطية."
            )
        )

        // Sensitivity Insights
        sensitivities.add("إذا كان الإيراد الإضافي أقل بـ 20%: سينخفض الـ Runway التقديري بمقدار ${(result.projectedRunwayMonths * 0.15).roundToInt()} شهراً.")
        sensitivities.add("إذا ارتفعت التكلفة التشغيلية 15%: ستزيد فترة الاسترداد بمقدار ${(result.paybackPeriodMonths * 0.25).roundToInt() + 1} شهور إضافية.")
        sensitivities.add("إذا تأخرت العوائد 60 يوماً: ستحتاج لتغطية عجز مؤقت قدره ${FinancialEngine.round2(scenario.recurringMonthlyExpense * 2)}$ من الرصيد الاحتياطي.")

        return DecisionAnalysisResult(
            decisionTitle = scenario.name,
            financialOutcomeSummary = outcomeSummary,
            pros = pros,
            cons = cons,
            risks = risks,
            opportunities = opportunities,
            recommendedAction = recommendation,
            alternatives = alternatives.sortedByDescending { it.overallDecisionScore },
            sensitivityInsights = sensitivities,
            assumptionsExplanation = "تعتمد النتيجة على معدل نمو ${scenario.growthRateMonthly}%، وتكلفة أولية ${scenario.initialCost}$ مع افتراض استقرار السوق المحيط.",
            confidenceScore = if (result.riskLevel == RiskLevel.LOW) 88 else 72
        )
    }

    private fun calculateDecisionScore(
        costImpact: Double,
        roi: Double,
        runwayMonths: Double,
        risk: RiskLevel,
        flexibility: Int
    ): Int {
        var score = 50.0

        // ROI contribution
        score += (roi.coerceIn(-50.0, 100.0) * 0.25)

        // Cost penalty
        score -= (costImpact * 30.0).coerceAtMost(30.0)

        // Runway bonus
        if (runwayMonths >= 12.0) score += 15.0
        else if (runwayMonths >= 6.0) score += 8.0
        else score -= 15.0

        // Risk penalty
        when (risk) {
            RiskLevel.LOW -> score += 10.0
            RiskLevel.MODERATE -> score += 0.0
            RiskLevel.HIGH -> score -= 12.0
            RiskLevel.CRITICAL -> score -= 25.0
        }

        // Flexibility
        score += (flexibility * 1.5)

        return score.roundToInt().coerceIn(15, 98)
    }
}
