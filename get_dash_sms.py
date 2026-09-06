with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find('            if (pendingTransactions.isNotEmpty()) {')
end_idx = content.find('            // 4. Accounts Slider', start_idx)
print(content[start_idx:end_idx])
