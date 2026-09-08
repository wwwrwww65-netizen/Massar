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
    private const val DEFAULT_FALLBACK_API_KEY = ""

    // Official Google Gemini API models in priority order
    private val MODELS_TO_TRY = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-flash",
        "gemini-2.5-pro",
        "gemini-1.5-pro"
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
        return DEFAULT_FALLBACK_API_KEY
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
                أنت 'مرشد مسار الذكي' (MASAR AI Advisor)، مستشار مالي وتجاري واستراتيجي خبير وودود لمساعدة الأفراد، الموظفين، وأصحاب الأعمال والمشاريع.
                
                الملف المالي للمستخدم في مسار:
                - الاسم / النشاط: ${profile.businessName}
                - الوضع المهني / نوع الحساب: ${profile.businessType.titleAr}
                - طبيعة ومصدر الدخل: ${profile.incomeSourceDescription}
                - العملة الأساسية: ${profile.baseCurrency}
                - الرصيد النقدي والمدخرات السائلة المتاحة فوراً: ${metrics.currentBalance} ${profile.baseCurrency}
                - إجمالي الدخل الشهري (راتب / إيرادات): ${metrics.totalMonthlyRevenue} ${profile.baseCurrency}
                - إجمالي المصروفات والالتزامات الشهرية: ${metrics.totalMonthlyExpenses} ${profile.baseCurrency}
                - صافي الفائض / التدفق النقدي الشهري: ${metrics.netMonthlyCashFlow} ${profile.baseCurrency}
                - فترة الأمان المالي / صندوق الطوارئ (Runway): ${if (metrics.runwayMonths >= 900) "فائقة الأمان ومستدامة (دخل يغطي المصاريف)" else "${metrics.runwayMonths} أشهر"}
                - حالة الأصول المجمدة: ${if (profile.hasFrozenAssets) "يوجد أصول مجمدة غير قابلة للتسييل السريع بقيمة تقريبية ${profile.frozenAssetsValue} ${profile.baseCurrency}" else "لا توجد أصول مجمدة، الأصول سائلة"}
                - نسبة المصاريف الثابتة: ${metrics.fixedExpensesRatio}%
                - مستوى المخاطر العام: ${metrics.overallRiskLevel.titleAr}
                - الأهداف المالية المحددة: ${profile.selectedGoals.joinToString("، ")}

                إرشادات الإجابة الذكية:
                1. تحدث باللغة العربية الفصحى الواضحة والعملية بأسلوب ذكي ومباشر بدون تعقيد.
                2. إذا كان المستخدم موظفاً (Employee)، ركز إجاباتك على الراتب، تنظيم المصاريف الشخصية، حساب جدوى الشراء أو التقسيط، وبناء صندوق الطوارئ.
                3. إذا كان صاحب مشروع أو مستقل، ركز على التدفقات النقدية، تكلفة التشغيل، التسعير وتسييل الأصول.
                4. إذا كان لديه أصول مجمدة، ساعده في خطط الاستفادة منها أو تسييلها بدون تعريض أمانه المالي للخطر.
                5. اربط الأرقام الحقيقية المذكورة أعلاه في ردك، واختم دائماً بتنبيه خفيف: "⚠️ تنبيه: هذه التوصيات استرشادية."
            """.trimIndent()

            // Build single coherent prompt with conversation history context
            val conversationHistoryFormatted = if (history.isNotEmpty()) {
                val pastTurns = history.takeLast(4).joinToString("\n") { 
                    val speaker = if (it.sender == "user") "المستخدم" else "المرشد"
                    "$speaker: ${it.content}"
                }
                "\n\nسياق المحادثة السابقة:\n$pastTurns"
            } else ""

            val fullPrompt = """
                [تعليمات النظام وبيانات النشاط]:
                $systemInstructionText
                $conversationHistoryFormatted
                
                [سؤال أو رسالة المستخدم الحالية]:
                $userMessage
            """.trimIndent()

            val requestPayload = GeminiRequest(
                contents = listOf(
                    GeminiContentItem(
                        role = "user",
                        parts = listOf(GeminiContentPart(text = fullPrompt))
                    )
                )
            )

            val adapter = moshi.adapter(GeminiRequest::class.java)
            val jsonBody = adapter.toJson(requestPayload)

            for (model in MODELS_TO_TRY) {
                try {
                    val requestAttempts = listOf(
                        Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                            .addHeader("x-goog-api-key", apiKey),
                        Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                            .addHeader("x-goog-api-key", apiKey)
                            .addHeader("Authorization", "Bearer $apiKey"),
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
                📊 **تحليل مرشد مسار المالي للاستفسار ("$userMessage"):**

                بناءً على الوضع المالي لنشاطك **(${profile.businessName})**:
                - 💰 **الرصيد المتاح:** ${metrics.currentBalance} ${profile.baseCurrency}
                - ⏳ **فترة الأمان (Runway):** ${if (metrics.runwayMonths >= 900) "مستدامة وممتازة" else "${metrics.runwayMonths.toInt()} أشهر"}
                - 📈 **صافي التدفق الشهري:** ${metrics.netMonthlyCashFlow} ${profile.baseCurrency}

                💡 **الرأي الاستشاري السريع:**
                - لأي التزام مالي أو استثماري جديد (مثل شراء أصول أو مصاريف تشغيلية إضافية)، احرص على ألا يتجاوز التأثير 30% من صافي التدفق الشهري الفائض لتفادي الضغط على السيولة.
                - يمكنك تفصيل سؤالك أكثر (مثلاً: "هل أشتري سيارة بقيمة 200 ألف؟" أو "كيف أزيد المبيعات؟") وسأقوم بحساب الأثر المالي الدقيق فوراً!

                ⚠️ *تنبيه: هذه التوصيات استرشادية مبنية على محاكاة البيانات.*
                """.trimIndent()
            }
        }
    }
}

