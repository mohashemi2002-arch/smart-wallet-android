with open('app/src/main/java/com/example/ui/screens/BankSmsHistoryScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("@OptIn(ExperimentalMaterial3Api::class)\n\n@OptIn(ExperimentalMaterial3Api::class)", "@OptIn(ExperimentalMaterial3Api::class)")

with open('app/src/main/java/com/example/ui/screens/BankSmsHistoryScreen.kt', 'w') as f:
    f.write(content)
