import re
with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.ui.platform.LocalContext.current", "ctx")
content = content.replace("var showAddChequeDialog by remember { mutableStateOf(false) }", "var showAddChequeDialog by remember { mutableStateOf(false) }\n    val ctx = androidx.compose.ui.platform.LocalContext.current")

with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'w') as f:
    f.write(content)
