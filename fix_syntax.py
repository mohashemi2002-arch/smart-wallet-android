with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("            }\n        }\n    }\n\n    }\n\n    if (showAllTransactionsDialog) {", "            }\n        }\n    }\n\n    if (showAllTransactionsDialog) {")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
