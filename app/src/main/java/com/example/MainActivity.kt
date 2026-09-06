package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.PoolakiTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : FragmentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    private lateinit var securityManager: com.example.domain.util.AppSecurityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()
        securityManager = com.example.domain.util.AppSecurityManager(this)
        securityManager.applyScreenSecurity(window)

        com.example.domain.worker.WorkerInitializer.enqueueFinancialReminders(this)
        setContent {
            val isUnlocked by securityManager.isUnlocked.collectAsState()
            PoolakiTheme {
                if (isUnlocked) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        PoolakiApp()
                    }
                } else {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        com.example.ui.screens.LockScreen(
                            onUnlockClick = { securityManager.promptBiometric(this@MainActivity) }
                        )
                    }
                }
            }
        }
    }


    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}

enum class ScreenTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("خانه", Icons.Default.Home),
    PORTFOLIO("دارایی", Icons.Default.AccountBalanceWallet),
    MARKET("نرخ زنده", Icons.Default.TrendingUp),
    BUDGET("بودجه", Icons.Default.Savings),
    CALENDAR("سررسید", Icons.Default.CalendarMonth),
    REPORTS("گزارش‌ها", Icons.Default.Assessment),
    SETTINGS("تنظیمات", Icons.Default.Settings)
}

@Composable
fun PoolakiApp(financeViewModel: FinanceViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color(0xFF1E2638),
                contentColor = androidx.compose.ui.graphics.Color(0xFFE6E1E5)
            ) {
                ScreenTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 10.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                            selectedTextColor = androidx.compose.ui.graphics.Color(0xFF7FA2FF),
                            indicatorColor = androidx.compose.ui.graphics.Color(0xFF38496B),
                            unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF8C9BAE),
                            unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF8C9BAE)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ScreenTab.DASHBOARD -> DashboardScreen(
                    viewModel = financeViewModel,
                    onNavigateToSms = { selectedTab = ScreenTab.CALENDAR },
                    onNavigateToBudget = { selectedTab = ScreenTab.BUDGET },
                    onNavigateToPortfolio = { selectedTab = ScreenTab.PORTFOLIO },
                    onNavigateToDebts = { selectedTab = ScreenTab.CALENDAR },
                    onNavigateToMarket = { selectedTab = ScreenTab.MARKET }
                )
                ScreenTab.PORTFOLIO -> PortfolioScreen(
                    viewModel = financeViewModel,
                    onBack = { selectedTab = ScreenTab.DASHBOARD },
                    onNavigateToMarket = { selectedTab = ScreenTab.MARKET }
                )
                ScreenTab.MARKET -> MarketRatesScreen(
                    viewModel = financeViewModel,
                    onBack = { selectedTab = ScreenTab.DASHBOARD }
                )
                ScreenTab.BUDGET -> BudgetScreen(
                    viewModel = financeViewModel,
                    onBack = { selectedTab = ScreenTab.DASHBOARD }
                )
                ScreenTab.CALENDAR -> DebtsChecksScreen(
                    onBack = { selectedTab = ScreenTab.DASHBOARD }
                )
                ScreenTab.REPORTS -> ReportsScreen(
                    onNavigateBack = { selectedTab = ScreenTab.DASHBOARD }
                )
                ScreenTab.SETTINGS -> SettingsScreen(
                    onNavigateBack = { selectedTab = ScreenTab.DASHBOARD }
                )
            }
        }
    }
}
