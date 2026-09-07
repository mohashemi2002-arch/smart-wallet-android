import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = r'''    var showPendingSmsInbox by remember \{ mutableStateOf\(false\) \}
    var showSmsSenderSettings by remember \{ mutableStateOf\(false\) \}'''

replacement = r'''    var showPendingSmsInbox by remember { mutableStateOf(false) }
    var showSmsSenderSettings by remember { mutableStateOf(false) }
    var showBankSmsHistory by remember { mutableStateOf(false) }'''

content = re.sub(target, replacement, content)

target2 = r'''    \} else if \(showPendingSmsInbox\) \{
        val pendingViewModel: com\.example\.ui\.viewmodel\.PendingSmsViewModel = viewModel\(\)
        com\.example\.ui\.screens\.PendingSmsInboxScreen\(
            viewModel = pendingViewModel,
            onNavigateBack = \{ showPendingSmsInbox = false \}
        \)
    \} else \{'''

replacement2 = r'''    } else if (showBankSmsHistory) {
        val historyViewModel: com.example.ui.viewmodel.BankSmsHistoryViewModel = viewModel()
        com.example.ui.screens.BankSmsHistoryScreen(
            viewModel = historyViewModel,
            onNavigateBack = { showBankSmsHistory = false },
            onManualIngest = {} // handled internally by the screen now
        )
    } else if (showPendingSmsInbox) {
        val pendingViewModel: com.example.ui.viewmodel.PendingSmsViewModel = viewModel()
        com.example.ui.screens.PendingSmsInboxScreen(
            viewModel = pendingViewModel,
            onNavigateBack = { showPendingSmsInbox = false },
            onNavigateToHistory = { showBankSmsHistory = true }
        )
    } else {'''

content = re.sub(target2, replacement2, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
