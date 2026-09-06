import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# First we need to collect allTransactions in DashboardScreen
target_collect = '''    val transactions by viewModel.transactions.collectAsState()'''
replacement_collect = '''    val transactions by viewModel.transactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()'''

content = content.replace(target_collect, replacement_collect)

target_dialog = '''    if (showAllTransactionsDialog) {
        AllTransactionsDialog(
            transactions = transactions,
            currencyUnit = currencyUnit,
            selectedFilter = selectedDateFilter,
            onFilterSelected = { filter -> viewModel.setDateFilter(filter) },
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )
    }'''

replacement_dialog = '''    if (showAllTransactionsDialog) {
        AllTransactionsDialog(
            transactions = transactions,
            allTransactions = allTransactions,
            currencyUnit = currencyUnit,
            selectedFilter = selectedDateFilter,
            onFilterSelected = { filter -> viewModel.setDateFilter(filter) },
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )
    }'''

content = content.replace(target_dialog, replacement_dialog)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
