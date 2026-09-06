import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

old_call = """        AllTransactionsDialog(
            transactions = transactions,
            currencyUnit = currencyUnit,
            onDismiss = { showAllTransactionsDialog = false }
        )"""

new_call = """        AllTransactionsDialog(
            transactions = transactions,
            currencyUnit = currencyUnit,
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )"""

content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
