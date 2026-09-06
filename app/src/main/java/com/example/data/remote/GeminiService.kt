package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.repository.ChatMessage
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.UserProfile
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiContentPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContentItem(
    val role: String = "user",
    val parts: List<GeminiContentPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContentItem>,
    val systemInstruction: GeminiContentItem? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContentResponse?
)

@JsonClass(generateAdapter = true)
data class GeminiContentResponse(
    val parts: List<GeminiContentPart>?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

object GeminiService {
    private const val TAG = "GeminiService"
    // Use supported modern Gemini Flash model
    private const val GEMINI_MODEL = "gemini-3.5-flash"
    private const val GEMINI_FALLBACK_MODEL = "gemini-2.5-flash"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getActiveApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (!key.isNullOrBlank() && key != "MY_GEMINI_API_KEY") key.trim() else ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun askAdvisor(
        userMessage: String,
        history: List<ChatMessage> = emptyList(),
        profile: UserProfile,
        metrics: FinancialMetrics
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey()

        if (apiKey.isNotBlank()) {
            val systemInstruction = """
                أنت 'مرشد مسار الذكي' (MASAR AI Advisor)، مستشار مالي وتجاري واستراتيجي خبير متخصص في مساعدة رواد الأعمال، وأصحاب المشروعات، والمستقلين.
                
                البيانات المالية الحالية للمستخدم (محسوبة لحظياً بدقة عبر المحرك المالي لتطبيق مسار):
                - اسم النشاط: ${profile.businessName}
                - نوع النشاط: ${profile.businessType.titleAr}
                - العملة: ${profile.baseCurrency}
                - الرصيد النقدي الفعلي الحالي: ${metrics.currentBalance} ${profile.baseCurrency}
                - إجمالي الإيرادات الشهرية: ${metrics.totalMonthlyRevenue} ${profile.baseCurrency}
                - إجمالي المصروفات الشهرية: ${metrics.totalMonthlyExpenses} ${profile.baseCurrency}
                - صافي التدفق النقدي الشهري: ${metrics.netMonthlyCashFlow} ${profile.baseCurrency}
                - فترة الأمان المالي والاستدامة (Runway): ${if (metrics.runwayMonths >= 900) "فائقة الأمان ومستدامة (تدفق إيجابي)" else "${metrics.runwayMonths} أشهر"}
                - نسبة المصاريف الثابتة: ${metrics.fixedExpensesRatio}%
                - مستوى المخاطر العام: ${metrics.overallRiskLevel.titleAr}
                - أهداف المستخدم: ${profile.selectedGoals.joinToString("، ")}

                إرشادات الإجابة:
                1. تحدث باللغة العربية الفصحى الواضحة والراقية بأسلوب عملي، مباشر، وداعم.
                2. اربط استشارتك دائماً بالأرقام والوضع الفعلي أعلاه، وقدم خطوات واضحة (1, 2, 3) قابلة للتطبيق.
                3. في حال السؤال عن قرارات شراء أو توظيف أو تسعير أو ترشيد، اذكر الأثر النقدي المباشر وبدائل ذكية.
                4. اختم دائماً بإخلاء مسؤولية قصير ولطيف: "⚠️ تنبيه: هذه التحليلات استرشادية مبنية على البيانات المالية ولا تغني عن الاستشارة المهنية المتخصصة."
            """.trimIndent()

            // Try primary model first, fallback to secondary model if needed
            val modelsToTry = listOf(GEMINI_MODEL, GEMINI_FALLBACK_MODEL)
            for (model in modelsToTry) {
                try {
                    // Build multi-turn content items from recent history (last 6 messages)
                    val contents = mutableListOf<GeminiContentItem>()
                    
                    // Add previous messages
                    val recentHistory = history.takeLast(6)
                    for (msg in recentHistory) {
                        val role = if (msg.sender == "user") "user" else "model"
                        contents.add(
                            GeminiContentItem(
                                role = role,
                                parts = listOf(GeminiContentPart(text = msg.content))
                            )
                        )
                    }

                    // Add current user message
                    contents.add(
                        GeminiContentItem(
                            role = "user",
                            parts = listOf(GeminiContentPart(text = userMessage))
                        )
                    )

                    val requestPayload = GeminiRequest(
                        contents = contents,
                        systemInstruction = GeminiContentItem(
                            role = "user",
                            parts = listOf(GeminiContentPart(text = systemInstruction))
                        )
                    )

                    val adapter = moshi.adapter(GeminiRequest::class.java)
                    val jsonBody = adapter.toJson(requestPayload)

                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                    val request = Request.Builder()
                        .url(url)
                        .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (!responseBody.isNullOrBlank()) {
                            val respAdapter = moshi.adapter(GeminiResponse::class.java)
                            val parsed = respAdapter.fromJson(responseBody)
                            val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (!text.isNullOrBlank()) {
                                return@withContext text.trim()
                            }
                        }
                    } else {
                        Log.w(TAG, "Gemini API ($model) failed with code ${response.code}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error with model $model: ${e.message}")
                }
            }
        }

        // Offline Deterministic Engine Fallback
        return@withContext generateDeterministicAdvice(userMessage, profile, metrics)
    }

    private fun generateDeterministicAdvice(
        userMessage: String,
        profile: UserProfile,
        metrics: FinancialMetrics
    ): String {
        val q = userMessage.lowercase()

        return when {
            q.contains("تسييل") || q.contains("بيع أصل") || q.contains("أصول") -> {
                """
                💡 **خطة تسييل الأصول المقترحة من مسار:**
                
                بناءً على رصيدك الحالي (${metrics.currentBalance} ${profile.baseCurrency}) وـ Runway البالغ (${metrics.runwayMonths} أشهر):
                
                1. **البيع المباشر السريع (Direct Cashout):**
                   - الأصول الرقمية أو المنتجات غير النشطة يمكن طرحها للبيع مع خصم 10-15% للحصول على كاش خلال 14-30 يوماً.
                
                2. **التأجير التشغيلي (Operational Leasing):**
                   - بدلاً من التنازل الكامل عن الملكية، قم بتأجير المعدات أو استضافة الخدمة بعائد شهري يغطي جزءاً من مصاريفك الثابتة (${metrics.totalMonthlyExpenses} ${profile.baseCurrency}).
                
                3. **ترخيص الكود أو المنتج (Whitelabel):**
                   - بيع رخص استخدام مخصصة لشركات أخرى يضمن إيرادات إضافية دون تكاليف تشغيلية جديدة.
                
                ⚠️ *تنبيه: التحليلات مبنية على محاكاة البيانات المدخلة ولا تعد مشورة استثمارية رسمية.*
                """.trimIndent()
            }

            q.contains("خفض") || q.contains("مصاريف") || q.contains("ترشيد") || q.contains("توفير") -> {
                """
                📉 **استراتيجية Bootstrapping لترشيد المصروفات:**
                
                إجمالي مصروفاتك الشهرية الحالية: ${metrics.totalMonthlyExpenses} ${profile.baseCurrency}
                
                1. **الاشتراكات والبرمجيات SaaS (وفر حتى 20% فوراً):**
                   - راجع أدواتك الرقمية وألغِ الحسابات غير المستغلة أو انتقل للخطط السنوية ذات الخصم.
                
                2. **تحويل المصاريف الثابتة إلى متغيرة:**
                   - استعن بمستقلين بنظام المشروع للمهام التسويقية والتصميمية بدلاً من التعاقدات الثابتة.
                
                3. **الأثر المالي المتوقع:**
                   - ترشيد 15% من المصاريف سيرفع فترة الـ Runway بمقدار ${(metrics.runwayMonths * 0.25).toInt() + 1} أشهر إضافية من الأمان المالي.
                
                ⚠️ *تنبيه: التحليلات مبنية على محاكاة البيانات المدخلة ولا تعد مشورة استثمارية رسمية.*
                """.trimIndent()
            }

            q.contains("runway") || q.contains("استدامة") || q.contains("بقاء") || q.contains("سيولة") -> {
                """
                ⏳ **تحليل فترة الاستدامة المالية (Runway Analysis):**
                
                - **فترة الـ Runway الحالية:** ${if (metrics.runwayMonths >= 900) "إيجابية ومستدامة (تدفق نقدي فائض)" else "${metrics.runwayMonths} أشهر"}
                - **التدفق النقدي الصافي:** ${metrics.netMonthlyCashFlow} ${profile.baseCurrency} شهرياً
                - **مستوى المخاطر الكلي:** ${metrics.overallRiskLevel.titleAr}
                
                **توصيات مسار لتعزيز الأمان:**
                - الاحتفاظ برصيد طوارئ يغطي 6 أشهر على الأقل (${metrics.totalMonthlyExpenses * 6} ${profile.baseCurrency}).
                - تحصيل مستحقات العملاء مقدماً أو بنظام الدفعات المجزأة لتقليل فجوة التحصيل النقدي.
                
                ⚠️ *تنبيه: التحليلات مبنية على محاكاة البيانات المدخلة ولا تعد مشورة استثمارية رسمية.*
                """.trimIndent()
            }

            q.contains("شراء") || q.contains("اشتري") || q.contains("استثمار") -> {
                """
                🎯 **تقييم قرار الشراء والاستثمار:**
                
                قبل الالتزام بأي استثمار جديد، اتبع قاعدة مسار الثلاثية:
                
                1. **اختبار السيولة:** هل يتبقى لديك احتياطي يغطي 3 أشهر على الأقل بعد دفع ثمن الأصل؟
                2. **فترة استرداد رأس المال (Payback):** احرص على ألا تتجاوز فترة الاسترداد 6 إلى 12 شهراً كحد أقصى.
                3. **البديل المرن:** جرب استئجار المعدة أو الخدمة لمدة 90 يوماً للتأكد من حجم الطلب الفعلي قبل الشراء الكامل.
                
                💡 استخدم قسم **"المحاكي المالي"** لتجربة هذا القرار برقم التكلفة المحدد ومقارنة السيناريوهات الثلاثة (المتفائل، الأساسي، المتشائم).
                
                ⚠️ *تنبيه: التحليلات مبنية على محاكاة البيانات المدخلة ولا تعد مشورة استثمارية رسمية.*
                """.trimIndent()
            }

            else -> {
                """
                📊 **التحليل المالي الاستشاري من مسار:**
                
                بناءً على وضع نشاطك الحالي (${profile.businessName} - ${profile.businessType.titleAr}):
                
                - **الرصيد المتاح:** ${metrics.currentBalance} ${profile.baseCurrency}
                - **الإيرادات الشهرية:** ${metrics.totalMonthlyRevenue} ${profile.baseCurrency}
                - **المصروفات الشهرية:** ${metrics.totalMonthlyExpenses} ${profile.baseCurrency}
                - **مستوى المخاطر العام:** ${metrics.overallRiskLevel.titleAr}
                
                💡 **الخطوات ذات الأولوية القصوى:**
                1. بناء خطة شهرية لزيادة الإيرادات بنسبة 10-15% عبر تنويع العروض أو استهداف عملاء إضافيين.
                2. تثبيت المصروفات التشغيلية وضبط الميزانيات التقديرية لكل فئة.
                3. تجربة أي قرار مالي كبير عبر محاكي مسار قبل توقيع أي عقود أو التزامات جديدة.
                
                ⚠️ *تنبيه: التحليلات مبنية على محاكاة البيانات المدخلة ولا تعد مشورة استثمارية رسمية.*
                """.trimIndent()
            }
        }
    }
}

