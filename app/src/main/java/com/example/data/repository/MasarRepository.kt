package com.example.data.repository

import com.example.data.local.dao.MasarDao
import com.example.data.local.entities.AssetEntity
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.BudgetEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.LiabilityEntity
import com.example.data.local.entities.SavedScenarioEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.data.remote.GeminiService
import com.example.domain.model.AssetCategory
import com.example.domain.model.AssetItem
import com.example.domain.model.AssetStatus
import com.example.domain.model.AuditLog
import com.example.domain.model.BudgetItem
import com.example.domain.model.BusinessType
import com.example.domain.model.ExpenseNature
import com.example.domain.model.ExpensePriority
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.GoalItem
import com.example.domain.model.LiabilityItem
import com.example.domain.model.RecurrenceType
import com.example.domain.model.ScenarioParameters
import com.example.domain.model.ScenarioType
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.UserProfile
import com.example.domain.model.ValueType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ChatMessage(
    val id: Long = 0,
    val conversationId: String = "default_session",
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isHelpful: Boolean? = null
)

data class ChatSession(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long,
    val messageCount: Int
)

class MasarRepository(private val dao: MasarDao) {

    // User Profile
    fun getUserProfile(): Flow<UserProfile?> {
        return dao.getUserProfile().map { entity ->
            entity?.let {
                UserProfile(
                    id = it.id,
                    businessName = it.businessName,
                    businessType = try { BusinessType.valueOf(it.businessType) } catch (e: Exception) { BusinessType.EMPLOYEE },
                    baseCurrency = it.baseCurrency,
                    currentCapital = it.currentCapital,
                    monthlyRevenue = it.monthlyRevenue,
                    monthlyExpenses = it.monthlyExpenses,
                    hasFrozenAssets = it.hasFrozenAssets,
                    frozenAssetsValue = it.frozenAssetsValue,
                    incomeSourceDescription = it.incomeSourceDescription,
                    selectedGoals = it.selectedGoals.split(",").filter { g -> g.isNotBlank() },
                    onboardingCompleted = it.onboardingCompleted,
                    isPro = it.isPro
                )
            }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val entity = UserProfileEntity(
            id = profile.id,
            businessName = profile.businessName,
            businessType = profile.businessType.name,
            baseCurrency = profile.baseCurrency,
            currentCapital = profile.currentCapital,
            monthlyRevenue = profile.monthlyRevenue,
            monthlyExpenses = profile.monthlyExpenses,
            hasFrozenAssets = profile.hasFrozenAssets,
            frozenAssetsValue = profile.frozenAssetsValue,
            incomeSourceDescription = profile.incomeSourceDescription,
            selectedGoals = profile.selectedGoals.joinToString(","),
            onboardingCompleted = profile.onboardingCompleted,
            isPro = profile.isPro
        )
        dao.insertOrUpdateProfile(entity)
        logAction("تحديث الملف الشخصي", "تم تحديث البيانات المالية: ${profile.businessType.titleAr} - ${profile.currentCapital} ${profile.baseCurrency}")
    }

    // Transactions
    fun getTransactions(): Flow<List<TransactionItem>> {
        return dao.getAllTransactions().map { list ->
            list.map { entity ->
                TransactionItem(
                    id = entity.id,
                    title = entity.title,
                    amount = entity.amount,
                    type = try { TransactionType.valueOf(entity.type) } catch (e: Exception) { TransactionType.EXPENSE },
                    category = entity.category,
                    date = entity.date,
                    currency = entity.currency,
                    valueType = try { ValueType.valueOf(entity.valueType) } catch (e: Exception) { ValueType.ACTUAL },
                    priority = try { ExpensePriority.valueOf(entity.priority) } catch (e: Exception) { ExpensePriority.IMPORTANT },
                    nature = try { ExpenseNature.valueOf(entity.nature) } catch (e: Exception) { ExpenseNature.FIXED },
                    recurrence = try { RecurrenceType.valueOf(entity.recurrence) } catch (e: Exception) { RecurrenceType.MONTHLY },
                    note = entity.note
                )
            }
        }
    }

    suspend fun addTransaction(item: TransactionItem): Long {
        val entity = TransactionEntity(
            id = item.id,
            title = item.title,
            amount = item.amount,
            type = item.type.name,
            category = item.category,
            date = item.date,
            currency = item.currency,
            valueType = item.valueType.name,
            priority = item.priority.name,
            nature = item.nature.name,
            recurrence = item.recurrence.name,
            note = item.note
        )
        val id = dao.insertTransaction(entity)
        logAction("إضافة معاملة مالية", "${item.type.titleAr}: ${item.title} بمبلغ ${item.amount} ${item.currency}")
        return id
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteTransactionById(id)
        logAction("حذف معاملة", "تم حذف المعاملة رقم $id")
    }

    // Liabilities
    fun getLiabilities(): Flow<List<LiabilityItem>> {
        return dao.getAllLiabilities().map { list ->
            list.map {
                LiabilityItem(
                    id = it.id,
                    title = it.title,
                    originalAmount = it.originalAmount,
                    remainingBalance = it.remainingBalance,
                    monthlyPayment = it.monthlyPayment,
                    interestRate = it.interestRate,
                    dueDate = it.dueDate,
                    totalInstallments = it.totalInstallments,
                    remainingInstallments = it.remainingInstallments,
                    currency = it.currency
                )
            }
        }
    }

    suspend fun addLiability(item: LiabilityItem): Long {
        val entity = LiabilityEntity(
            id = item.id,
            title = item.title,
            originalAmount = item.originalAmount,
            remainingBalance = item.remainingBalance,
            monthlyPayment = item.monthlyPayment,
            interestRate = item.interestRate,
            dueDate = item.dueDate,
            totalInstallments = item.totalInstallments,
            remainingInstallments = item.remainingInstallments,
            currency = item.currency
        )
        return dao.insertLiability(entity)
    }

    suspend fun deleteLiability(id: Long) {
        dao.deleteLiabilityById(id)
    }

    // Assets
    fun getAssets(): Flow<List<AssetItem>> {
        return dao.getAllAssets().map { list ->
            list.map {
                AssetItem(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    category = try { AssetCategory.valueOf(it.category) } catch (e: Exception) { AssetCategory.DIGITAL },
                    purchasePrice = it.purchasePrice,
                    estimatedValue = it.estimatedValue,
                    monthlyIncome = it.monthlyIncome,
                    rentalValue = it.rentalValue,
                    status = try { AssetStatus.valueOf(it.status) } catch (e: Exception) { AssetStatus.ACTIVE },
                    liquiditySpeedDays = it.liquiditySpeedDays,
                    conditionScore = it.conditionScore,
                    currency = it.currency
                )
            }
        }
    }

    suspend fun addAsset(item: AssetItem): Long {
        val entity = AssetEntity(
            id = item.id,
            title = item.title,
            description = item.description,
            category = item.category.name,
            purchasePrice = item.purchasePrice,
            estimatedValue = item.estimatedValue,
            monthlyIncome = item.monthlyIncome,
            rentalValue = item.rentalValue,
            status = item.status.name,
            liquiditySpeedDays = item.liquiditySpeedDays,
            conditionScore = item.conditionScore,
            currency = item.currency
        )
        val id = dao.insertAsset(entity)
        logAction("إضافة أصل استثماري", "تم إضافة ${item.title} بقيمة ${item.estimatedValue} ${item.currency}")
        return id
    }

    suspend fun updateAsset(item: AssetItem) {
        val entity = AssetEntity(
            id = item.id,
            title = item.title,
            description = item.description,
            category = item.category.name,
            purchasePrice = item.purchasePrice,
            estimatedValue = item.estimatedValue,
            monthlyIncome = item.monthlyIncome,
            rentalValue = item.rentalValue,
            status = item.status.name,
            liquiditySpeedDays = item.liquiditySpeedDays,
            conditionScore = item.conditionScore,
            currency = item.currency
        )
        dao.updateAsset(entity)
    }

    suspend fun deleteAsset(id: Long) {
        dao.deleteAssetById(id)
    }

    // Budgets
    fun getBudgets(): Flow<List<BudgetItem>> {
        return dao.getAllBudgets().map { list ->
            list.map {
                BudgetItem(
                    id = it.id,
                    category = it.category,
                    monthlyLimit = it.monthlyLimit,
                    currentSpent = it.currentSpent,
                    currency = it.currency
                )
            }
        }
    }

    suspend fun addBudget(budget: BudgetItem): Long {
        val entity = BudgetEntity(
            id = budget.id,
            category = budget.category,
            monthlyLimit = budget.monthlyLimit,
            currentSpent = budget.currentSpent,
            currency = budget.currency
        )
        return dao.insertBudget(entity)
    }

    suspend fun deleteBudget(id: Long) {
        dao.deleteBudgetById(id)
    }

    // Goals
    fun getGoals(): Flow<List<GoalItem>> {
        return dao.getAllGoals().map { list ->
            list.map {
                GoalItem(
                    id = it.id,
                    title = it.title,
                    targetAmount = it.targetAmount,
                    currentAmount = it.currentAmount,
                    deadlineMonths = it.deadlineMonths,
                    monthlyContribution = it.monthlyContribution,
                    currency = it.currency
                )
            }
        }
    }

    suspend fun addGoal(goal: GoalItem): Long {
        val entity = GoalEntity(
            id = goal.id,
            title = goal.title,
            targetAmount = goal.targetAmount,
            currentAmount = goal.currentAmount,
            deadlineMonths = goal.deadlineMonths,
            monthlyContribution = goal.monthlyContribution,
            currency = goal.currency
        )
        return dao.insertGoal(entity)
    }

    suspend fun deleteGoal(id: Long) {
        dao.deleteGoalById(id)
    }

    // Scenarios
    fun getSavedScenarios(): Flow<List<ScenarioParameters>> {
        return dao.getAllSavedScenarios().map { list ->
            list.map {
                ScenarioParameters(
                    id = it.id,
                    name = it.name,
                    type = try { ScenarioType.valueOf(it.scenarioType) } catch (e: Exception) { ScenarioType.CUSTOM },
                    durationMonths = it.durationMonths,
                    initialCost = it.initialCost,
                    recurringMonthlyExpense = it.recurringMonthlyExpense,
                    recurringMonthlyRevenue = it.recurringMonthlyRevenue,
                    revenueChangePercent = it.revenueChangePercent,
                    expenseChangePercent = it.expenseChangePercent,
                    growthRateMonthly = it.growthRateMonthly,
                    probabilityOfSuccess = it.probabilityOfSuccess,
                    assumptions = it.assumptionsJson.split("|").filter { a -> a.isNotBlank() },
                    notes = it.notes
                )
            }
        }
    }

    suspend fun saveScenario(scenario: ScenarioParameters): Long {
        val entity = SavedScenarioEntity(
            id = scenario.id,
            name = scenario.name,
            scenarioType = scenario.type.name,
            durationMonths = scenario.durationMonths,
            initialCost = scenario.initialCost,
            recurringMonthlyExpense = scenario.recurringMonthlyExpense,
            recurringMonthlyRevenue = scenario.recurringMonthlyRevenue,
            revenueChangePercent = scenario.revenueChangePercent,
            expenseChangePercent = scenario.expenseChangePercent,
            growthRateMonthly = scenario.growthRateMonthly,
            probabilityOfSuccess = scenario.probabilityOfSuccess,
            assumptionsJson = scenario.assumptions.joinToString("|"),
            notes = scenario.notes
        )
        val id = dao.insertSavedScenario(entity)
        logAction("حفظ سيناريو محاكاة", "تم حفظ سيناريو '${scenario.name}'")
        return id
    }

    suspend fun deleteScenario(id: Long) {
        dao.deleteSavedScenarioById(id)
    }

    // Chat
    fun getChatMessages(): Flow<List<ChatMessage>> {
        return dao.getAllChatMessages().map { list ->
            list.map {
                ChatMessage(
                    id = it.id,
                    conversationId = it.conversationId,
                    sender = it.sender,
                    content = it.content,
                    timestamp = it.timestamp,
                    isHelpful = it.isHelpful
                )
            }
        }
    }

    fun getChatSessions(): Flow<List<ChatSession>> {
        return dao.getAllChatMessages().map { messages ->
            messages.groupBy { it.conversationId }
                .map { (convId, msgList) ->
                    val firstUserMsg = msgList.firstOrNull { it.sender == "user" }?.content
                        ?.take(45) ?: "جلسة استشارة مسار"
                    val lastMsg = msgList.lastOrNull()?.content?.take(60) ?: ""
                    val latestTime = msgList.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
                    ChatSession(
                        id = convId,
                        title = firstUserMsg,
                        lastMessage = lastMsg,
                        timestamp = latestTime,
                        messageCount = msgList.size
                    )
                }
                .sortedByDescending { it.timestamp }
        }
    }

    suspend fun sendChatMessage(
        content: String,
        conversationId: String,
        profile: UserProfile,
        metrics: FinancialMetrics
    ): String {
        // Fetch recent history for this conversation before adding the new message
        val historyEntities = dao.getChatHistoryList(conversationId)
        val history = historyEntities.map {
            ChatMessage(
                id = it.id,
                conversationId = it.conversationId,
                sender = it.sender,
                content = it.content,
                timestamp = it.timestamp,
                isHelpful = it.isHelpful
            )
        }

        // Save user message
        dao.insertChatMessage(
            ChatMessageEntity(
                conversationId = conversationId,
                sender = "user",
                content = content
            )
        )

        // Get AI advisor reply from Gemini (or fallback engine)
        val reply = GeminiService.askAdvisor(
            userMessage = content,
            history = history,
            profile = profile,
            metrics = metrics
        )

        // Save advisor response
        dao.insertChatMessage(
            ChatMessageEntity(
                conversationId = conversationId,
                sender = "advisor",
                content = reply
            )
        )

        return reply
    }

    suspend fun deleteChatSession(conversationId: String) {
        dao.deleteChatByConversation(conversationId)
    }

    suspend fun clearChat() {
        dao.deleteAllChatMessages()
    }

    // Audit logs
    fun getAuditLogs(): Flow<List<AuditLog>> {
        return dao.getAllAuditLogs().map { list ->
            list.map {
                AuditLog(
                    id = it.id,
                    actionName = it.actionName,
                    details = it.details,
                    timestamp = it.timestamp
                )
            }
        }
    }

    suspend fun logAction(actionName: String, details: String) {
        dao.insertAuditLog(AuditLogEntity(actionName = actionName, details = details))
    }

    // Seed Initial Starter Data if DB is empty
    suspend fun seedInitialDataIfNeeded() {
        val existingProfile = dao.getUserProfileOnce()
        if (existingProfile == null) {
            val initialProfile = UserProfileEntity(
                id = "default_user",
                businessName = "استوديو مسار الرقمي",
                businessType = BusinessType.FREELANCER.name,
                baseCurrency = "USD",
                currentCapital = 9500.0,
                monthlyRevenue = 3600.0,
                monthlyExpenses = 2100.0,
                selectedGoals = "حماية السيولة لمدة 12 شهر,تطوير منتج SaaS جديد,شراء محطة عمل جديدة",
                onboardingCompleted = true,
                isPro = true
            )
            dao.insertOrUpdateProfile(initialProfile)

            // Seed sample transactions
            dao.insertTransaction(
                TransactionEntity(
                    title = "عقود استشارات وتطوير برمجيات",
                    amount = 3600.0,
                    type = TransactionType.INCOME.name,
                    category = "إيرادات عملاء",
                    date = "2026-03-01",
                    currency = "USD",
                    valueType = ValueType.ACTUAL.name,
                    priority = ExpensePriority.ESSENTIAL.name,
                    nature = ExpenseNature.VARIABLE.name,
                    recurrence = RecurrenceType.MONTHLY.name,
                    note = "دخل شهري متكرر من عميلين"
                )
            )

            dao.insertTransaction(
                TransactionEntity(
                    title = "إيجار مساحة العمل المشتركة",
                    amount = 450.0,
                    type = TransactionType.EXPENSE.name,
                    category = "مقر ومكتب",
                    date = "2026-03-01",
                    currency = "USD",
                    valueType = ValueType.ACTUAL.name,
                    priority = ExpensePriority.IMPORTANT.name,
                    nature = ExpenseNature.FIXED.name,
                    recurrence = RecurrenceType.MONTHLY.name,
                    note = "مكتب مشترك مع إنترنت فائق السرعة"
                )
            )

            dao.insertTransaction(
                TransactionEntity(
                    title = "اشتراكات برمجيات وسيرفرات AWS",
                    amount = 380.0,
                    type = TransactionType.EXPENSE.name,
                    category = "تقنية وسيرفرات",
                    date = "2026-03-01",
                    currency = "USD",
                    valueType = ValueType.ACTUAL.name,
                    priority = ExpensePriority.ESSENTIAL.name,
                    nature = ExpenseNature.FIXED.name,
                    recurrence = RecurrenceType.MONTHLY.name,
                    note = "استضافة واستخدام واجهات برمجية"
                )
            )

            dao.insertTransaction(
                TransactionEntity(
                    title = "حملات إعلانية وتوليد عملاء",
                    amount = 550.0,
                    type = TransactionType.EXPENSE.name,
                    category = "تسويق وإعلانات",
                    date = "2026-03-01",
                    currency = "USD",
                    valueType = ValueType.ACTUAL.name,
                    priority = ExpensePriority.OPTIONAL.name,
                    nature = ExpenseNature.VARIABLE.name,
                    recurrence = RecurrenceType.MONTHLY.name,
                    note = "إعلانات Google و LinkedIn"
                )
            )

            // Seed Assets
            dao.insertAsset(
                AssetEntity(
                    title = "قالب برمجيات SaaS للتجارة (Micro-SaaS Codebase)",
                    description = "نظام متكامل لإدارة الاشتراكات والتجارة مبني بتقنيات حديثة",
                    category = AssetCategory.SAAS.name,
                    purchasePrice = 3000.0,
                    estimatedValue = 8500.0,
                    monthlyIncome = 650.0,
                    rentalValue = 180.0,
                    status = AssetStatus.ACTIVE.name,
                    liquiditySpeedDays = 21,
                    conditionScore = 92,
                    currency = "USD"
                )
            )

            dao.insertAsset(
                AssetEntity(
                    title = "محطة عمل متقدمة (MacBook Pro M3 Max)",
                    description = "جهاز عمل احترافي بكامل الملحقات وشاشات 4K",
                    category = AssetCategory.PHYSICAL.name,
                    purchasePrice = 4200.0,
                    estimatedValue = 3100.0,
                    monthlyIncome = 0.0,
                    rentalValue = 120.0,
                    status = AssetStatus.ACTIVE.name,
                    liquiditySpeedDays = 7,
                    conditionScore = 88,
                    currency = "USD"
                )
            )

            // Seed Budgets
            dao.insertBudget(
                BudgetEntity(
                    category = "تقنية وسيرفرات",
                    monthlyLimit = 500.0,
                    currentSpent = 380.0,
                    currency = "USD"
                )
            )
            dao.insertBudget(
                BudgetEntity(
                    category = "تسويق وإعلانات",
                    monthlyLimit = 700.0,
                    currentSpent = 550.0,
                    currency = "USD"
                )
            )

            // Seed Goals
            dao.insertGoal(
                GoalEntity(
                    title = "بناء احتياطي طوارئ 6 أشهر",
                    targetAmount = 15000.0,
                    currentAmount = 9500.0,
                    deadlineMonths = 6,
                    monthlyContribution = 900.0,
                    currency = "USD"
                )
            )

            // Seed Welcome Chat Message
            dao.insertChatMessage(
                ChatMessageEntity(
                    sender = "advisor",
                    content = "مرحباً بك في **مسار MASAR 2.0**! 🚀\nأنا مرشدك المالي والتجاري الذكي. تم تحليل وضعك المالي ورصيدك المتاح (${9500}$)، وتدفقك النقدي إيجابي (+1500$/شهر).\n\nيمكنك سؤالي عن قرارات التوظيف، الاستئجار، تسييل الأصول، أو محاكاة أي قرار استثماري قبل تنفيذه!"
                )
            )

            logAction("تهيئة النظام", "تم إنشاء قاعدة بيانات مسار بنجاح")
        }
    }

    suspend fun wipeAllData() {
        dao.wipeUserProfile()
        dao.deleteAllTransactions()
        dao.deleteAllChatMessages()
        logAction("حذف الحساب", "تم مسح جميع البيانات المحلية استجابة لطلب المستخدم")
    }
}
