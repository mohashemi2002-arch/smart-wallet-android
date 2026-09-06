import re
with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add a state for PENDING_SMS
# Actually, since it's an enum, we could add PENDING_SMS. 
# Better: Create a separate boolean state for showPendingSmsInbox
replacement = """    var showPendingSmsInbox by remember { mutableStateOf(false) }

    if (showPendingSmsInbox) {
        val pendingViewModel: com.example.ui.viewmodel.PendingSmsViewModel = viewModel()
        com.example.ui.screens.PendingSmsInboxScreen(
            viewModel = pendingViewModel,
            onNavigateBack = { showPendingSmsInbox = false }
        )
    } else {
"""
content = content.replace("    Scaffold(", replacement + "    Scaffold(")
content = content.replace('onNavigateToSms = { selectedTab = ScreenTab.CALENDAR }', 'onNavigateToSms = { showPendingSmsInbox = true }')
content = content.replace("            }\n        }\n    }\n}", "            }\n        }\n    }\n}\n}") # Need an extra brace at the end of else block

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
