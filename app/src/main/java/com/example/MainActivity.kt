package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.local.MasarDatabase
import com.example.data.repository.MasarRepository
import com.example.ui.screens.advisor.AdvisorChatScreen
import com.example.ui.screens.assets.AssetsValuationScreen
import com.example.ui.screens.bootstrapping.BootstrappingScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.simulator.SimulatorScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarViewModel
import com.example.ui.viewmodel.MasarViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { MasarDatabase.getInstance(this) }
    private val repository by lazy { MasarRepository(database.masarDao()) }
    private val viewModel: MasarViewModel by viewModels {
        MasarViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.uiState.collectAsState()

                    LaunchedEffect(state.toastMessage) {
                        state.toastMessage?.let { msg ->
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            viewModel.clearToast()
                        }
                    }

                    Crossfade(
                        targetState = state.currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.SPLASH -> SplashScreen(
                                onTimeout = {
                                    if (state.profile.onboardingCompleted) {
                                        viewModel.navigateTo(AppScreen.DASHBOARD)
                                    } else {
                                        viewModel.navigateTo(AppScreen.ONBOARDING)
                                    }
                                }
                            )

                            AppScreen.ONBOARDING -> OnboardingScreen(
                                onComplete = { name, type, curr, cap, rev, exp, goals ->
                                    viewModel.completeOnboarding(name, type, curr, cap, rev, exp, goals)
                                }
                            )

                            AppScreen.DASHBOARD -> DashboardScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) },
                                onSelectScenario = { viewModel.setScenario(it) }
                            )

                            AppScreen.SIMULATOR -> SimulatorScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) },
                                onSimulateScenario = { viewModel.setScenario(it) },
                                onParseNaturalLanguage = { viewModel.parseAndSimulateNaturalLanguage(it) },
                                onSaveScenario = { viewModel.saveCurrentScenario() }
                            )

                            AppScreen.TRANSACTIONS -> TransactionsScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) },
                                onAddTransaction = { viewModel.addTransaction(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) }
                            )

                            AppScreen.ASSETS -> AssetsValuationScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) },
                                onAddAsset = { viewModel.addAsset(it) },
                                onSelectAssetForValuation = { viewModel.selectAssetForValuation(it) },
                                onUpdateStatus = { asset, status -> viewModel.updateAssetStatus(asset, status) },
                                onDeleteAsset = { viewModel.deleteAsset(it) }
                            )

                            AppScreen.ADVISOR -> AdvisorChatScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) },
                                onSendMessage = { viewModel.sendAdvisorMessage(it) },
                                onStartNewChat = { viewModel.startNewChat() },
                                onSwitchChatSession = { viewModel.switchChatSession(it) },
                                onDeleteChatSession = { viewModel.deleteChatSession(it) },
                                onClearChat = { viewModel.clearChat() }
                            )

                            AppScreen.BOOTSTRAPPING -> BootstrappingScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) }
                            )

                            AppScreen.REPORTS -> ReportsScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) }
                            )

                            AppScreen.SETTINGS -> SettingsScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) },
                                onUpdateProfile = { viewModel.updateProfile(it) },
                                onWipeData = { viewModel.wipeAllUserData() }
                            )

                            AppScreen.ADMIN_DIAGNOSTICS -> ReportsScreen(
                                state = state,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
