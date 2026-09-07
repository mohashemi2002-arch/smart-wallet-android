import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

import_statement = "import com.example.updater.*\nimport kotlinx.coroutines.launch\n"
content = content.replace("import com.example.ui.viewmodel.SettingsViewModel\n", "import com.example.ui.viewmodel.SettingsViewModel\n" + import_statement)

settings_func_def = """fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSmsSenders: () -> Unit = {},
    financeViewModel: FinanceViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {"""

settings_func_new = """fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSmsSenders: () -> Unit = {},
    financeViewModel: FinanceViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val updateManager = remember { AppUpdateManager(context) }
    var updateRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    updateRelease?.let { release ->
        UpdatePromptDialog(
            release = release,
            updateManager = updateManager,
            onDismiss = { updateRelease = null }
        )
    }
"""

content = content.replace(settings_func_def, settings_func_new)

target = """            item {
                Card("""
                
replacement = """            // System Update
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            isCheckingUpdate = true
                            val release = updateManager.checkForUpdates(isManualCheck = true)
                            isCheckingUpdate = false
                            if (release != null) {
                                updateRelease = release
                            } else {
                                Toast.makeText(context, "شما از آخرین نسخه استفاده می‌کنید.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("بررسی بروزرسانی", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE6E1E5))
                            Text(if (isCheckingUpdate) "در حال بررسی..." else "دریافت آخرین نسخه برنامه", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCAC4D0))
                        }
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFCAC4D0))
                        } else {
                            Icon(Icons.Filled.SystemUpdate, contentDescription = "بررسی بروزرسانی", tint = Color(0xFFCAC4D0))
                        }
                    }
                }
            }
            
            item {
                Card("""
                
content = content.replace(target, replacement, 1)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
