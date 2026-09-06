import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace static string "مرداد" with dynamic from ViewModel
content = content.replace('import androidx.compose.runtime.Composable', 'import androidx.compose.runtime.*\nimport com.example.ui.components.JalaliDatePickerDialog')

# Insert the date picker state at the beginning of DashboardScreen
target_start = '''fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToSms: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    val isUnlocked by securityManager.isUnlocked.collectAsState()'''

replacement_start = '''fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToSms: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    
    if (showDatePickerDialog) {
        JalaliDatePickerDialog(
            initialMillis = selectedDateFilter?.startMillis,
            onDateSelected = { start, end, title ->
                viewModel.updateDateFilter(start, end, title)
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }'''

content = content.replace(
'''fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToSms: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToMarket: () -> Unit
) {''', replacement_start)

target_header = '''                // 2. Date & Quick Actions Card ("امروز / یکشنبه، ۱۱ مرداد")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("امروز", color = Color(0xFF8C9BAE), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("یک‌شنبه، ۱۱ مرداد", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }'''

replacement_header = '''                // 2. Date & Quick Actions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true }
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedDateFilter?.displayTitle ?: "کل تراکنش‌ها", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.DateRange, contentDescription = "تقویم", tint = Color(0xFF7FA2FF), modifier = Modifier.size(20.dp))
                            }
                        }'''

content = content.replace(target_header, replacement_header)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
