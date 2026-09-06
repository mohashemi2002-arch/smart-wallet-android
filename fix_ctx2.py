import re
with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("android.widget.Toast.makeText(ctx", "android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current")

# Add ctx at the top level of DebtsChecksScreen
content = content.replace("    var showAddChequeDialog by remember { mutableStateOf(false) }", "    var showAddChequeDialog by remember { mutableStateOf(false) }\n    val ctx = androidx.compose.ui.platform.LocalContext.current")
content = content.replace("android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current", "android.widget.Toast.makeText(ctx")

with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'w') as f:
    f.write(content)
