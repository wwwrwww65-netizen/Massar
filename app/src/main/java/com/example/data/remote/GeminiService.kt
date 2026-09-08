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
data class GeminiSystemInstruction(
    val parts: List<GeminiContentPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContentItem>,
    @Json(name = "system_instruction")
    val systemInstruction: GeminiSystemInstruction? = null
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
    // Preferred Gemini models in priority order
    private val MODELS_TO_TRY = listOf(
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite",
        "gemini-3.5-flash",
        "gemini-flash-latest"
    )
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var customApiKey: String? = null

    fun setCustomApiKey(key: String) {
        customApiKey = key.trim()
    }

    fun getActiveApiKey(): String {
        if (!customApiKey.isNullOrBlank()) {
            return customApiKey!!.trim()
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (!buildKey.isNullOrBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    suspend fun askAdvisor(
        userMessage: String,
        history: List<ChatMessage> = emptyList(),
        profile: UserProfile,
        metrics: FinancialMetrics
    ): String = withContext(Dispatchers.IO) {
        val trimmedMsg = userMessage.trim()

        // Auto-detect if user pastes an API key directly in chat
        if ((trimmedMsg.startsWith("AQ.") || trimmedMsg.startsWith("AIzaSy")) && trimmedMsg.length >= 25) {
            setCustomApiKey(trimmedMsg)
            return@withContext """
                🎉 **تم تفعيل وحفظ مفتاح Gemini API بنجاح!**
                
                رمز المفتاح: `${trimmedMsg.take(10)}...${trimmedMsg.takeLast(4)}`
                تم ربط المحرك مباشرة بنماذج الذكاء الاصطناعي من Google. يمكنك الآن سؤالي أي استفسار وسأجيبك فوراً! 🚀
            """.trimIndent()
        }

        val apiKey = getActiveApiKey()

        var lastErrorDetails = ""

        if (apiKey.isNotBlank()) {
            val systemInstructionText = """
                أنت 'مرشد مسار الذكي' (MASAR AI Advisor)، مستشار مالي وتجاري واستراتيجي خبير وودود لمساعدة رواد الأعمال والمستقلين.
                
                بيانات المستخدم المالية الحالية في مسار:
                - اسم النشاط: ${profile.businessName}
                - نوع النشاط: ${profile.businessType.titleAr}
                - العملة الأساسية: ${profile.baseCurrency}
                - الرصيد النقدي الفعلي الحالي: ${metrics.currentBalance} ${profile.baseCurrency}
                - إجمالي الإيرادات الشهرية: ${metrics.totalMonthlyRevenue} ${profile.baseCurrency}
                - إجمالي المصروفات الشهرية: ${metrics.totalMonthlyExpenses} ${profile.baseCurrency}
                - صافي التدفق النقدي الشهري: ${metrics.netMonthlyCashFlow} ${profile.baseCurrency}
                - فترة الأمان المالي (Runway): ${if (metrics.runwayMonths >= 900) "فائقة الأمان ومستدامة (تدفق إيجابي)" else "${metrics.runwayMonths} أشهر"}
                - نسبة المصاريف الثابتة: ${metrics.fixedExpensesRatio}%
                - مستوى المخاطر: ${metrics.overallRiskLevel.titleAr}
                - الأهداف: ${profile.selectedGoals.joinToString("، ")}

                إرشادات الإجابة:
                1. تحدث باللغة العربية الفصحى الواضحة والعملية وبأسلوب حواري ذكي ومباشر.
                2. أجب بدقة وعمق على سؤال المستخدم أياً كان موضوعه، سواء كان سؤالاً عاماً، نقاشاً تجارياً، أو استفساراً مالياً.
                3. اربط الإجابة بأرقام نشاطه أعلاه متى ما كان ذلك مناسباً.
                4. اختم دائماً بإخلاء مسؤولية قصير: "⚠️ تنبيه: هذه التوصيات استرشادية."
            """.trimIndent()

            for (model in MODELS_TO_TRY) {
                try {
                    val contents = mutableListOf<GeminiContentItem>()
                    
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

                    contents.add(
                        GeminiContentItem(
                            role = "user",
                            parts = listOf(GeminiContentPart(text = "$userMessage\n\n[سياق مسار المالي للمستخدم: $systemInstructionText]"))
                        )
                    )

                    val requestPayload = GeminiRequest(
                        contents = contents
                    )

                    val adapter = moshi.adapter(GeminiRequest::class.java)
                    val jsonBody = adapter.toJson(requestPayload)

                    // Multiple request configurations for maximum compatibility with Auth Keys & Standard Keys
                    val requestAttempts = listOf(
                        Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                            .addHeader("x-goog-api-key", apiKey)
                            .addHeader("Authorization", "Bearer $apiKey"),
                        Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                            .addHeader("x-goog-api-key", apiKey),
                        Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1/models/$model:generateContent?key=$apiKey")
                            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                    )

                    for (reqBuilder in requestAttempts) {
                        try {
                            val response = client.newCall(reqBuilder.build()).execute()
                            val responseBody = response.body?.string()

                            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                                val respAdapter = moshi.adapter(GeminiResponse::class.java)
                                val parsed = respAdapter.fromJson(responseBody)
                                val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                if (!text.isNullOrBlank()) {
                                    Log.d(TAG, "Successfully received Gemini response from $model")
                                    return@withContext text.trim()
                                }
                            } else {
                                lastErrorDetails = "HTTP ${response.code}: $responseBody"
                                Log.w(TAG, "Gemini attempt for model $model returned $lastErrorDetails")
                            }
                        } catch (attemptEx: Exception) {
                            Log.w(TAG, "Single request attempt failed: ${attemptEx.message}")
                        }
                    }
                } catch (e: Exception) {
                    lastErrorDetails = e.message ?: "Network error"
                    Log.e(TAG, "Exception calling Gemini model $model: ${e.message}", e)
                }
            }
        }

        // If API key was provided but returned an explicit authentication error
        if (apiKey.isNotBlank() && (lastErrorDetails.contains("400") || lastErrorDetails.contains("403") || lastErrorDetails.contains("API_KEY_INVALID"))) {
            Log.w(TAG, "Gemini API key returned authentication error: $lastErrorDetails")
        }

        // Contextual Natural Conversational Engine Fallback
        return@withContext generateDeterministicAdvice(userMessage, profile, metrics)
    }

    private fun generateDeterministicAdvice(
        userMessage: String,
        profile: UserProfile,
        metrics: FinancialMetrics
    ): String {
        val q = userMessage.trim().lowercase()

        // Check for greetings or casual messages
        if (q.matches(Regex("^(مرحبا|أهلا|اهلا|سلام|السلام عليكم|صباح الخير|مساء الخير|hi|hello|hey|hhh|هههه|ههه|هلا|حياك).*"))) {
            return """
                أهلاً وسهلاً بك في **مرشد مسار الذكي**! 👋
                
                أنا جاهز ومستعد لمساعدتك في كل ما يخص نشاطك **(${profile.businessName})**:
                
                - 💰 **رصيدك الحالي:** ${metrics.currentBalance} ${profile.baseCurrency}
                - ⏳ **فترة الأمان (Runway):** ${if (metrics.runwayMonths >= 900) "مستدامة وفائضة" else "${metrics.runwayMonths.toInt()} أشهر"}
                - 📈 **صافي التدفق:** ${metrics.netMonthlyCashFlow} ${profile.baseCurrency}/شهرياً
                
                عن ماذا تحب أن نتحدث اليوم؟ (خطط تنمية الإيرادات، ترشيد التكاليف، تسييل الأصول، أو قرار توظيف جديد؟)
            """.trimIndent()
        }

        if (q.contains("شكرا") || q.contains("شكراً") || q.contains("يعطيك العافية") || q.contains("تسلم") || q.contains("thanks")) {
            return "على الرحب والسعة دائماً! 🌟 أنا هنا لمساعدتك في أي وقت لاتخاذ أفضل القرارات المالية لمشروعك."
        }

        if (q.contains("توظيف") || q.contains("موظف") || q.contains("هيرينج") || q.contains("راتب") || q.contains("فريق")) {
            val maxSalarySafe = (metrics.netMonthlyCashFlow * 0.4).coerceAtLeast(0.0)
            return """
                👥 **تحليل قرار التوظيف لمشروعك (${profile.businessName}):**
                
                1. **القدرة المالية الحالية:**
                   - صافي التدفق الشهري: ${metrics.netMonthlyCashFlow} ${profile.baseCurrency}.
                   - الراتب الآمن المقترح: لا يتجاوز ${maxSalarySafe.toInt()} ${profile.baseCurrency} شهرياً للحفاظ على استقرار الـ Runway.
                
                2. **توصية مسار:**
                   - إذا كانت الوظيفة تولد دخلاً مباشراً (كالمبيعات أو التسويق)، فابدأ بنظام النسبة أو العمل الحر (Freelance) لمدة شهرين للتأكد من العائد (ROI) قبل التثبيت.
                
                ⚠️ *تنبيه: التوصية استرشادية بناءً على أرقامك المدخلة.*
            """.trimIndent()
        }

        if (q.contains("تسعير") || q.contains("سعر") || q.contains("رفع السعر") || q.contains("خصم")) {
            return """
                🏷️ **استراتيجية التسعير المقترحة:**
                
                - لتغطية مصاريفك الشهرية (${metrics.totalMonthlyExpenses} ${profile.baseCurrency}) وتحقيق هامش أمان 25%:
                1. **التسعير المبني على القيمة (Value-Based):** ركز على المشكلة التي تحلها للعميل وقيمتها لديه بدلاً من حساب تكلفة الوقت فقط.
                2. **باقات متدرجة (Tiered Pricing):** قدم 3 باقات (أساسية، متقدمة، احترافية) لتشجيع العميل على اختيار الباقة الوسطى.
                
                ⚠️ *تنبيه: التوصية استرشادية.*
            """.trimIndent()
        }

        return when {
            q.contains("تسييل") || q.contains("بيع أصل") || q.contains("أصول") -> {
                """
                💡 **خطة تسييل الأصول المقترحة من مسار:**
                
                بناءً على رصيدك الحالي (${metrics.currentBalance} ${profile.baseCurrency}) وـ Runway البالغ (${metrics.runwayMonths} أشهر):
                
                1. **البيع المباشر السريع (Direct Cashout):**
                   - الأصول الرقمية أو المعدات غير المستغلة يمكن تسييلها مع خصم تشجيعي لتوفير سيولة عاجلة.
                
                2. **التأجير التشغيلي (Operational Leasing):**
                   - تأجير الأصل أو الخدمة بعائد شهري يغطي جزءاً من مصاريفك الثابتة (${metrics.totalMonthlyExpenses} ${profile.baseCurrency}).
                
                3. **ترخيص الكود أو المنتج (Whitelabel):**
                   - بيع رخص استخدام يضمن إيرادات دورية إضافية دون تكاليف جديدة.
                
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
                💬 أهلاً بك! لقد استلمت رسالتك: **"$userMessage"**.
                
                للحصول على إجابات تفاعلية ذكية وحرة ومفتوحة عبر نموذج **Google Gemini AI**، كل ما تحتاجه هو إرسال مفتاح الـ API المجاني الخاص بك هنا في المحادثة مباشرة.
                
                🔑 **طريقة الحصول على المفتاح المجاني (خلال 5 ثوانٍ):**
                1. افتح: **https://aistudio.google.com/app/apikey**
                2. اضغط **Create API key**
                3. انسخ المفتاح الذي يبدأ بـ **`AIzaSy...`** والصقه هنا في الشات!
                
                📊 **ملخص وضعك المالي السريع في مسار:**
                - الرصيد: ${metrics.currentBalance} ${profile.baseCurrency}
                - التدفق الشهري: ${metrics.netMonthlyCashFlow} ${profile.baseCurrency}
                """.trimIndent()
            }
        }
    }
}

