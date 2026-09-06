import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = '''    if (showAllTransactionsDialog) {
        AllTransactionsDialog(
            transactions = transactions,
            currencyUnit = currencyUnit,
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )
    }'''

replacement = '''    if (showAllTransactionsDialog) {
        AllTransactionsDialog(
            transactions = transactions,
            currencyUnit = currencyUnit,
            selectedFilter = selectedDateFilter,
            onFilterSelected = { filter -> viewModel.setDateFilter(filter) },
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )
    }'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)

