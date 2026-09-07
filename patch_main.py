import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import_statement = "import com.example.updater.*\nimport androidx.compose.ui.platform.LocalContext\nimport kotlinx.coroutines.launch\n"
content = content.replace("import com.example.ui.viewmodel.FinanceViewModel\n", "import com.example.ui.viewmodel.FinanceViewModel\n" + import_statement)

app_func_def = "fun SmartWalletApp(financeViewModel: FinanceViewModel = viewModel()) {"
app_func_new = """fun SmartWalletApp(financeViewModel: FinanceViewModel = viewModel()) {
    val context = LocalContext.current
    val updateManager = remember { AppUpdateManager(context) }
    var updateRelease by remember { mutableStateOf<GitHubRelease?>(null) }

    LaunchedEffect(Unit) {
        updateRelease = updateManager.checkForUpdates(isManualCheck = false)
    }

    updateRelease?.let { release ->
        UpdatePromptDialog(
            release = release,
            updateManager = updateManager,
            onDismiss = { updateRelease = null }
        )
    }
"""

content = content.replace(app_func_def, app_func_new)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
