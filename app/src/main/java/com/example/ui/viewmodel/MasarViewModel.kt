package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatMessage
import com.example.data.repository.ChatSession
import com.example.data.repository.MasarRepository
import com.example.domain.engine.BootstrappingEngine
import com.example.domain.engine.DecisionIntelligence
import com.example.domain.engine.DetailedRiskBreakdown
import com.example.domain.engine.FinancialEngine
import com.example.domain.engine.NaturalLanguageParser
import com.example.domain.engine.RiskEngine
import com.example.domain.engine.ScenarioEngine
import com.example.domain.engine.ValuationEngine
import com.example.domain.model.AssetCategory
import com.example.domain.model.AssetItem
import com.example.domain.model.AssetStatus
import com.example.domain.model.AssetValuationResult
import com.example.domain.model.AuditLog
import com.example.domain.model.BootstrappingOptimization
import com.example.domain.model.BudgetItem
import com.example.domain.model.BusinessType
import com.example.domain.model.DecisionAnalysisResult
import com.example.domain.model.FinancialMetrics
import com.example.domain.model.ForecastPoint
import com.example.domain.model.GoalItem
import com.example.domain.model.LiabilityItem
import com.example.domain.model.RiskLevel
import com.example.domain.model.ScenarioParameters
import com.example.domain.model.ScenarioResult
import com.example.domain.model.ScenarioType
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionType
import com.example.domain.model.UserProfile
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    DASHBOARD,
    SIMULATOR,
    TRANSACTIONS,
    ASSETS,
    ADVISOR,
    BOOTSTRAPPING,
    REPORTS,
    SETTINGS,
    ADMIN_DIAGNOSTICS
}

data class MasarUiState(
    val currentScreen: AppScreen = AppScreen.SPLASH,
    val profile: UserProfile = UserProfile(),
    val transactions: List<TransactionItem> = emptyList(),
    val liabilities: List<LiabilityItem> = emptyList(),
    val assets: List<AssetItem> = emptyList(),
    val budgets: List<BudgetItem> = emptyList(),
    val goals: List<GoalItem> = emptyList(),
    val savedScenarios: List<ScenarioParameters> = emptyList(),
    val currentConversationId: String = "default_session",
    val chatMessages: List<ChatMessage> = emptyList(),
    val chatSessions: List<ChatSession> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    
    // Derived Financial Intelligence
    val metrics: FinancialMetrics = FinancialMetrics(
        currentBalance = 9500.0,
        totalMonthlyRevenue = 3600.0,
        totalMonthlyExpenses = 2100.0,
        netMonthlyCashFlow = 1500.0,
        burnRate = 0.0,
        runwayMonths = 999.0,
        breakEvenRevenue = 2100.0,
        fixedExpensesRatio = 45.0,
        revenueConcentrationRisk = 40.0,
        overallRiskLevel = RiskLevel.LOW,
        confidenceScore = 85
    ),
    val riskBreakdown: DetailedRiskBreakdown = DetailedRiskBreakdown(
        liquidityRisk = RiskLevel.LOW,
        liquidityRiskScore = 15,
        runwayRisk = RiskLevel.LOW,
        runwayRiskScore = 10,
        fixedCostRisk = RiskLevel.LOW,
        fixedCostRiskScore = 20,
        debtRisk = RiskLevel.LOW,
        debtRiskScore = 10,
        concentrationRisk = RiskLevel.LOW,
        concentrationRiskScore = 25,
        overallScore = 16,
        topRiskWarnings = listOf("الوضع المالي مستقر ومتوازن مع مؤشرات أمان جيدة.")
    ),
    val forecastPoints: List<ForecastPoint> = emptyList(),
    val bootstrapping: BootstrappingOptimization = BootstrappingOptimization(
        currentEssentialExpenses = 1200.0,
        currentImportantExpenses = 600.0,
        currentOptionalExpenses = 300.0,
        totalMonthlySavingPotential = 420.0,
        extendedRunwayMonths = 999.0,
        recommendations = emptyList()
    ),
    
    // Active Simulation & Decision State
    val activeScenario: ScenarioParameters = ScenarioEngine.getPredefinedTemplates().first(),
    val activeSimulationResult: ScenarioResult? = null,
    val activeDecisionAnalysis: DecisionAnalysisResult? = null,
    val activeValuationResult: AssetValuationResult? = null,
    
    // UI controls
    val isChatLoading: Boolean = false,
    val isSimulating: Boolean = false,
    val toastMessage: String? = null,
    val currencySymbol: String = "$"
)

class MasarViewModel(private val repository: MasarRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MasarUiState())
    val uiState: StateFlow<MasarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            observeDatabase()
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            combine(
                repository.getUserProfile(),
                repository.getTransactions(),
                repository.getLiabilities(),
                repository.getAssets(),
                repository.getBudgets(),
                repository.getGoals()
            ) { args: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                val profile = args[0] as? UserProfile
                @Suppress("UNCHECKED_CAST")
                val txs = args[1] as? List<TransactionItem> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val liabilities = args[2] as? List<LiabilityItem> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val assets = args[3] as? List<AssetItem> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val budgets = args[4] as? List<BudgetItem> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val goals = args[5] as? List<GoalItem> ?: emptyList()

                val user = profile ?: UserProfile()
                val calculatedMetrics = FinancialEngine.calculateMetrics(user, txs, liabilities)
                val risk = RiskEngine.evaluateRisks(calculatedMetrics, txs, liabilities)
                val forecast = FinancialEngine.generateForecast(
                    currentCapital = calculatedMetrics.currentBalance,
                    monthlyRevenue = calculatedMetrics.totalMonthlyRevenue,
                    monthlyExpenses = calculatedMetrics.totalMonthlyExpenses,
                    months = 12
                )
                val bootPlan = BootstrappingEngine.analyzeBootstrapping(calculatedMetrics, txs)

                // Run default simulation for current active scenario
                val currentScenario = _uiState.value.activeScenario
                val simResult = ScenarioEngine.simulate(currentScenario, calculatedMetrics)
                val decision = DecisionIntelligence.analyzeDecision(currentScenario, simResult, calculatedMetrics)

                val valuation = if (assets.isNotEmpty()) ValuationEngine.valuateAsset(assets.first()) else null

                val nextScreen = if (_uiState.value.currentScreen == AppScreen.SPLASH) {
                    if (user.onboardingCompleted) AppScreen.DASHBOARD else AppScreen.ONBOARDING
                } else {
                    _uiState.value.currentScreen
                }

                _uiState.value = _uiState.value.copy(
                    currentScreen = nextScreen,
                    profile = user,
                    transactions = txs,
                    liabilities = liabilities,
                    assets = assets,
                    budgets = budgets,
                    goals = goals,
                    metrics = calculatedMetrics,
                    riskBreakdown = risk,
                    forecastPoints = forecast,
                    bootstrapping = bootPlan,
                    activeSimulationResult = simResult,
                    activeDecisionAnalysis = decision,
                    activeValuationResult = valuation,
                    currencySymbol = getCurrencySymbol(user.baseCurrency)
                )
            }.collect {}
        }

        viewModelScope.launch {
            repository.getSavedScenarios().collect { scenarios ->
                _uiState.value = _uiState.value.copy(savedScenarios = scenarios)
            }
        }

        viewModelScope.launch {
            repository.getChatMessages().collect { msgs ->
                _uiState.value = _uiState.value.copy(chatMessages = msgs)
            }
        }

        viewModelScope.launch {
            repository.getChatSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(chatSessions = sessions)
            }
        }

        viewModelScope.launch {
            repository.getAuditLogs().collect { logs ->
                _uiState.value = _uiState.value.copy(auditLogs = logs)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun completeOnboarding(
        businessName: String,
        businessType: BusinessType,
        baseCurrency: String,
        capital: Double,
        revenue: Double,
        expenses: Double,
        goals: List<String>
    ) {
        viewModelScope.launch {
            val profile = UserProfile(
                businessName = businessName.ifBlank { "مشروعي" },
                businessType = businessType,
                baseCurrency = baseCurrency,
                currentCapital = capital,
                monthlyRevenue = revenue,
                monthlyExpenses = expenses,
                selectedGoals = goals,
                onboardingCompleted = true
            )
            repository.saveUserProfile(profile)
            _uiState.value = _uiState.value.copy(currentScreen = AppScreen.DASHBOARD)
            showToast("مرحباً بك في مسار! تم تحليل وضعك المالي بنجاح.")
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            showToast("تم حفظ التعديلات بنجاح")
        }
    }

    // Simulation & Scenario Methods
    fun setScenario(scenario: ScenarioParameters) {
        val simResult = ScenarioEngine.simulate(scenario, _uiState.value.metrics)
        val decision = DecisionIntelligence.analyzeDecision(scenario, simResult, _uiState.value.metrics)
        _uiState.value = _uiState.value.copy(
            activeScenario = scenario,
            activeSimulationResult = simResult,
            activeDecisionAnalysis = decision
        )
    }

    fun parseAndSimulateNaturalLanguage(prompt: String) {
        val parsedScenario = NaturalLanguageParser.parseArabicPrompt(prompt)
        setScenario(parsedScenario)
        showToast("تم تفسير النص ومحاكاة السيناريو بنجاح!")
    }

    fun saveCurrentScenario() {
        viewModelScope.launch {
            repository.saveScenario(_uiState.value.activeScenario)
            showToast("تم حفظ السيناريو في سجلك للرجوع إليه لاحقاً")
        }
    }

    fun deleteScenario(id: Long) {
        viewModelScope.launch {
            repository.deleteScenario(id)
            showToast("تم حذف السيناريو")
        }
    }

    // Transactions Methods
    fun addTransaction(item: TransactionItem) {
        viewModelScope.launch {
            repository.addTransaction(item)
            showToast("تمت إضافة المعاملة بنجاح")
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            showToast("تم حذف المعاملة")
        }
    }

    // Assets & Valuation Methods
    fun addAsset(item: AssetItem) {
        viewModelScope.launch {
            repository.addAsset(item)
            val valuation = ValuationEngine.valuateAsset(item)
            _uiState.value = _uiState.value.copy(activeValuationResult = valuation)
            showToast("تمت إضافة الأصل وتقييمه بنجاح")
        }
    }

    fun selectAssetForValuation(asset: AssetItem) {
        val valuation = ValuationEngine.valuateAsset(asset)
        _uiState.value = _uiState.value.copy(activeValuationResult = valuation)
    }

    fun updateAssetStatus(asset: AssetItem, newStatus: AssetStatus) {
        viewModelScope.launch {
            val updated = asset.copy(status = newStatus)
            repository.updateAsset(updated)
            selectAssetForValuation(updated)
            showToast("تم تحديث حالة الأصل إلى ${newStatus.titleAr}")
        }
    }

    fun deleteAsset(id: Long) {
        viewModelScope.launch {
            repository.deleteAsset(id)
            showToast("تم حذف الأصل")
        }
    }

    // Budgets & Goals
    fun addBudget(budget: BudgetItem) {
        viewModelScope.launch {
            repository.addBudget(budget)
            showToast("تمت إضافة الميزانية")
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
            showToast("تم حذف الميزانية")
        }
    }

    fun addGoal(goal: GoalItem) {
        viewModelScope.launch {
            repository.addGoal(goal)
            showToast("تمت إضافة الهدف المالي")
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            showToast("تم حذف الهدف")
        }
    }

    // Chat Advisor & History Management
    fun startNewChat() {
        val newSessionId = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(
            currentConversationId = newSessionId
        )
        showToast("تم بدء محادثة جديدة")
    }

    fun switchChatSession(sessionId: String) {
        _uiState.value = _uiState.value.copy(
            currentConversationId = sessionId
        )
    }

    fun deleteChatSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteChatSession(sessionId)
            if (_uiState.value.currentConversationId == sessionId) {
                val remainingSessions = _uiState.value.chatSessions.filter { it.id != sessionId }
                val nextSessionId = remainingSessions.firstOrNull()?.id ?: UUID.randomUUID().toString()
                _uiState.value = _uiState.value.copy(currentConversationId = nextSessionId)
            }
            showToast("تم حذف المحادثة")
        }
    }

    fun sendAdvisorMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChatLoading = true)
            try {
                repository.sendChatMessage(
                    content = content,
                    conversationId = _uiState.value.currentConversationId,
                    profile = _uiState.value.profile,
                    metrics = _uiState.value.metrics
                )
            } finally {
                _uiState.value = _uiState.value.copy(isChatLoading = false)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.deleteChatSession(_uiState.value.currentConversationId)
            val newSessionId = UUID.randomUUID().toString()
            _uiState.value = _uiState.value.copy(
                currentConversationId = newSessionId
            )
            showToast("تم مسح المحادثة الحالية")
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            repository.clearChat()
            val newSessionId = UUID.randomUUID().toString()
            _uiState.value = _uiState.value.copy(
                currentConversationId = newSessionId
            )
            showToast("تم مسح كافة سجلات المحادثات")
        }
    }

    fun wipeAllUserData() {
        viewModelScope.launch {
            repository.wipeAllData()
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.ONBOARDING,
                profile = UserProfile(onboardingCompleted = false)
            )
            showToast("تم مسح كافة البيانات من الجهاز")
        }
    }

    fun showToast(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    private fun getCurrencySymbol(currency: String): String {
        return when (currency) {
            "USD" -> "$"
            "SAR" -> "ر.س"
            "AED" -> "د.إ"
            "EGP" -> "ج.م"
            "KWD" -> "د.ك"
            "QAR" -> "ر.ق"
            "EUR" -> "€"
            else -> currency
        }
    }
}

class MasarViewModelFactory(private val repository: MasarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MasarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MasarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
