with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

target = "    val scope = rememberCoroutineScope()\n    val updateManager = remember { AppUpdateManager(context) }"
replacement = "    val context = LocalContext.current\n    val scope = rememberCoroutineScope()\n    val updateManager = remember { AppUpdateManager(context) }"

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
