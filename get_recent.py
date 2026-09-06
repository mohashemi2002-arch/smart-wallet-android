with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()
start_idx = content.find('            // 5. Recent Transactions')
end_idx = content.find('        if (showAddDialog) {', start_idx)
print(content[start_idx:end_idx])
