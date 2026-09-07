with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = """                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {"""

replacement = """                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {"""

widget_code = """
@Composable
fun MarketTickerWidget(marketRates: List<com.example.data.model.MarketRate>, onNavigateToMarket: () -> Unit) {
    val majorBenchmarks = marketRates.filter { it.symbol in listOf("USD", "GOLD_18K", "COIN_EMAMI", "USDT") }
    if (majorBenchmarks.isEmpty()) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onNavigateToMarket() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222834))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(16.dp))
                Text("نرخ لحظه‌ای بازار", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(majorBenchmarks) { rate ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(rate.titleFa, color = Color.LightGray, fontSize = 11.sp)
                        val isPos = rate.changePercent >= 0
                        val changeColor = if (isPos) Color(0xFF81C784) else Color(0xFFEF9A9A)
                        Text(
                            text = com.example.ui.components.PersianCurrencyFormatter.formatToman(rate.priceToman, true),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = (if (isPos) "▲" else "▼") + " " + com.example.ui.components.PersianFormatters.toPersianDigits(Math.abs(rate.changePercent).toString()) + "٪",
                            color = changeColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
"""

content += widget_code

# Inject widget item
inject_target = """                        ) {
                            // Dynamic User Accounts
                            
                            items(bankAccounts) { acc ->"""

inject_replace = """                        ) {
                            // Dynamic User Accounts
                            
                            items(bankAccounts) { acc ->"""

content = content.replace("                ) {\n                    Column(\n                        modifier = Modifier\n                            .fillMaxWidth()\n                            .padding(horizontal = 16.dp),\n                        verticalArrangement = Arrangement.spacedBy(14.dp)\n                    ) {", "                ) {\n                    Column(\n                        modifier = Modifier\n                            .fillMaxWidth()\n                            .padding(horizontal = 16.dp),\n                        verticalArrangement = Arrangement.spacedBy(14.dp)\n                    ) {")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
