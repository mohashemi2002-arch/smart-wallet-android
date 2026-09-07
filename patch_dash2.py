with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = """                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {"""

target_lazy = """            // 1. Top Blue Canvas Header Banner (Matches Screenshot 1, 3, 4)
            item {
                Box("""

replace_lazy = """            // Ticker
            item {
                MarketTickerWidget(marketRates = marketRates, onNavigateToMarket = onNavigateToMarket ?: {})
            }
            
            // 1. Top Blue Canvas Header Banner (Matches Screenshot 1, 3, 4)
            item {
                Box("""

content = content.replace(target_lazy, replace_lazy)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
