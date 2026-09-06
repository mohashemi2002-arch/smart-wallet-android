with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "    var showPendingSmsInbox by remember { mutableStateOf(false) }",
    "    var showPendingSmsInbox by remember { mutableStateOf(false) }\n    var showSmsSenderSettings by remember { mutableStateOf(false) }"
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
