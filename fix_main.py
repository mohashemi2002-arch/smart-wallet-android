import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

fixed = re.sub(
    r'when \(selectedTab\) \{.*?\n            \}',
    '''when (selectedTab) {
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
            }''',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(fixed)

