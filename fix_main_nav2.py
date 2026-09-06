with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }\n    var showPendingSmsInbox by remember { mutableStateOf(false) }",
    "var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }\n    var showPendingSmsInbox by remember { mutableStateOf(false) }\n    var showSmsSenderSettings by remember { mutableStateOf(false) }"
)

content = content.replace(
    "if (showPendingSmsInbox) {",
    "if (showSmsSenderSettings) {\n        com.example.ui.screens.SmsSenderSettingsScreen(onNavigateBack = { showSmsSenderSettings = false })\n    } else if (showPendingSmsInbox) {"
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
