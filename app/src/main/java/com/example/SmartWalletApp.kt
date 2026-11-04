package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsChecksScreen
import com.example.ui.screens.MarketRatesScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.FilteredSmsViewerScreen
import com.example.ui.screens.IncomingSmsGroupedScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmsReaderScreen
import com.example.ui.screens.SmsSenderSettingsScreen
import com.example.ui.viewmodel.FinanceViewModel

private enum class WalletDestination {
    DASHBOARD,
    PORTFOLIO,
    BUDGET,
    REPORTS,
    SETTINGS,
    SMS_READER,
    DEBTS_CHECKS,
    MARKET_RATES,
    REGISTRATION,
    SMS_SENDERS,
    SMS_FILTER,
    INCOMING_SMS_GROUPED
}

/**
 * Root navigation for Smart Wallet.
 *
 * Navigation is intentionally state-based because Navigation Compose is not
 * enabled in this project's Gradle dependencies.
 */
@Composable
fun SmartWalletApp() {
    val financeViewModel: FinanceViewModel = viewModel()
    var destination by rememberSaveable {
        mutableStateOf(WalletDestination.DASHBOARD)
    }
    var filterInitialSender by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val showBottomBar = destination in setOf(
        WalletDestination.DASHBOARD,
        WalletDestination.PORTFOLIO,
        WalletDestination.BUDGET,
        WalletDestination.REPORTS,
        WalletDestination.SETTINGS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = destination == WalletDestination.DASHBOARD,
                        onClick = { destination = WalletDestination.DASHBOARD },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "خانه") },
                        label = { Text("خانه") }
                    )
                    NavigationBarItem(
                        selected = destination == WalletDestination.PORTFOLIO,
                        onClick = { destination = WalletDestination.PORTFOLIO },
                        icon = {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = "دارایی"
                            )
                        },
                        label = { Text("دارایی") }
                    )
                    NavigationBarItem(
                        selected = destination == WalletDestination.BUDGET,
                        onClick = { destination = WalletDestination.BUDGET },
                        icon = { Icon(Icons.Filled.Savings, contentDescription = "بودجه") },
                        label = { Text("بودجه") }
                    )
                    NavigationBarItem(
                        selected = destination == WalletDestination.REPORTS,
                        onClick = { destination = WalletDestination.REPORTS },
                        icon = {
                            Icon(Icons.Filled.Assessment, contentDescription = "گزارش‌ها")
                        },
                        label = { Text("گزارش‌ها") }
                    )
                    NavigationBarItem(
                        selected = destination == WalletDestination.SETTINGS,
                        onClick = { destination = WalletDestination.SETTINGS },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "تنظیمات") },
                        label = { Text("تنظیمات") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (destination) {
                WalletDestination.DASHBOARD -> DashboardScreen(
                    viewModel = financeViewModel,
                    onNavigateToSms = {
                        destination = WalletDestination.SMS_READER
                    },
                    onNavigateToBudget = {
                        destination = WalletDestination.BUDGET
                    },
                    onNavigateToPortfolio = {
                        destination = WalletDestination.PORTFOLIO
                    },
                    onNavigateToDebts = {
                        destination = WalletDestination.DEBTS_CHECKS
                    },
                    onNavigateToMarket = {
                        destination = WalletDestination.MARKET_RATES
                    },
                    onNavigateToRegistration = {
                        destination = WalletDestination.REGISTRATION
                    }
                )

                WalletDestination.PORTFOLIO -> PortfolioScreen(
                    viewModel = financeViewModel,
                    onBack = { destination = WalletDestination.DASHBOARD },
                    onNavigateToMarket = {
                        destination = WalletDestination.MARKET_RATES
                    }
                )

                WalletDestination.BUDGET -> BudgetScreen(
                    viewModel = financeViewModel,
                    onBack = { destination = WalletDestination.DASHBOARD }
                )

                WalletDestination.REPORTS -> ReportsScreen(
                    onNavigateBack = { destination = WalletDestination.DASHBOARD }
                )

                WalletDestination.SETTINGS -> SettingsScreen(
                    onNavigateBack = { destination = WalletDestination.DASHBOARD },
                    onNavigateToRegistration = {
                        destination = WalletDestination.REGISTRATION
                    },
                    onNavigateToSmsSenders = {
                        destination = WalletDestination.SMS_SENDERS
                    }
                )

                WalletDestination.SMS_READER -> SmsReaderScreen(
                    viewModel = financeViewModel,
                    onBack = { destination = WalletDestination.DASHBOARD },
                    onNavigateToFilteredSms = { senderNum ->
                        filterInitialSender = senderNum
                        destination = WalletDestination.SMS_FILTER
                    },
                    onNavigateToGroupedSms = { senderNum ->
                        filterInitialSender = senderNum
                        destination = WalletDestination.INCOMING_SMS_GROUPED
                    }
                )

                WalletDestination.SMS_FILTER -> FilteredSmsViewerScreen(
                    initialSenderNumber = filterInitialSender,
                    onNavigateBack = { destination = WalletDestination.SMS_READER }
                )

                WalletDestination.INCOMING_SMS_GROUPED -> IncomingSmsGroupedScreen(
                    initialSenderFilter = filterInitialSender,
                    onNavigateBack = { destination = WalletDestination.SMS_READER }
                )

                WalletDestination.DEBTS_CHECKS -> DebtsChecksScreen(
                    onBack = { destination = WalletDestination.DASHBOARD }
                )

                WalletDestination.MARKET_RATES -> MarketRatesScreen(
                    viewModel = financeViewModel,
                    onBack = { destination = WalletDestination.DASHBOARD }
                )

                WalletDestination.REGISTRATION -> RegistrationScreen(
                    onNavigateBack = { destination = WalletDestination.SETTINGS },
                    onNavigateToLogin = { destination = WalletDestination.SETTINGS },
                    onRegistrationSuccess = {
                        destination = WalletDestination.DASHBOARD
                    }
                )

                WalletDestination.SMS_SENDERS -> SmsSenderSettingsScreen(
                    onNavigateBack = { destination = WalletDestination.SETTINGS }
                )
            }
        }
    }
}
