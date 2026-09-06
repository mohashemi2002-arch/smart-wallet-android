with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import re

new_nav_state = """    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var showPendingSmsInbox by remember { mutableStateOf(false) }
    var showSmsSenderSettings by remember { mutableStateOf(false) }

    if (showSmsSenderSettings) {
        com.example.ui.screens.SmsSenderSettingsScreen(
            onNavigateBack = { showSmsSenderSettings = false }
        )
    } else if (showPendingSmsInbox) {"""

content = content.replace("    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }\n    var showPendingSmsInbox by remember { mutableStateOf(false) }\n\n    if (showPendingSmsInbox) {", new_nav_state)

new_settings_call = """                ScreenTab.SETTINGS -> SettingsScreen(
                    onNavigateBack = { selectedTab = ScreenTab.DASHBOARD },
                    onNavigateToSmsSenders = { showSmsSenderSettings = true }
                )"""

content = content.replace("                ScreenTab.SETTINGS -> SettingsScreen(\n                    onNavigateBack = { selectedTab = ScreenTab.DASHBOARD }\n                )", new_settings_call)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
