import re

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'r') as f:
    content = f.read()

# Replace viewModel.approveAll(pendingTransactions) with viewModel.approveAllPending(...)
# Let's find it.

content = content.replace(
    'TextButton(onClick = { viewModel.approveAll(pendingTransactions) })',
    'TextButton(onClick = { viewModel.approveAllPending(onComplete = {}, onError = {}) })'
)

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'w') as f:
    f.write(content)
