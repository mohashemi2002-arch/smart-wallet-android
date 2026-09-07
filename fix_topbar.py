with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'r') as f:
    content = f.read()

target = r'''                actions = {
                    if (pendingTransactions.isNotEmpty()) {
                        TextButton(onClick = { viewModel.approveAllPending(onComplete = {}, onError = {}) }) {
                            Text("تایید همه", color = Color(0xFF5C82E6), fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.History, contentDescription = "تاریخچه پیامک‌های بانکی")
                        }
                    }
                },'''

replacement = r'''                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "تاریخچه پیامک‌های بانکی")
                    }
                    if (pendingTransactions.isNotEmpty()) {
                        TextButton(onClick = { viewModel.approveAllPending(onComplete = {}, onError = {}) }) {
                            Text("تایید همه", color = Color(0xFF5C82E6), fontWeight = FontWeight.Bold)
                        }
                    }
                },'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'w') as f:
    f.write(content)
