with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import re

# We need to make sure `showSmsSenderSettings` is defined in PoolakiApp
new_poolaki = """@Composable
fun PoolakiApp(financeViewModel: FinanceViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var showPendingSmsInbox by remember { mutableStateOf(false) }
    var showSmsSenderSettings by remember { mutableStateOf(false) }

    if (showSmsSenderSettings) {
        com.example.ui.screens.SmsSenderSettingsScreen(onNavigateBack = { showSmsSenderSettings = false })
    } else if (showPendingSmsInbox) {"""

content = re.sub(r'@Composable\nfun PoolakiApp\(financeViewModel: FinanceViewModel = viewModel\(\)\) \{\n    var selectedTab by remember \{ mutableStateOf\(ScreenTab\.DASHBOARD\) \}\n    var showPendingSmsInbox by remember \{ mutableStateOf\(false\) \}\n\n    if \(showSmsSenderSettings\) \{', new_poolaki, content, flags=re.DOTALL)
content = re.sub(r'@Composable\nfun PoolakiApp\(financeViewModel: FinanceViewModel = viewModel\(\)\) \{\n    var selectedTab by remember \{ mutableStateOf\(ScreenTab\.DASHBOARD\) \}\n    var showPendingSmsInbox by remember \{ mutableStateOf\(false\) \}\n    var showSmsSenderSettings by remember \{ mutableStateOf\(false\) \}\n\n    if \(showSmsSenderSettings\) \{', new_poolaki, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
