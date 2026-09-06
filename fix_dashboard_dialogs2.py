import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target1 = '''            onFilterSelected = { filter -> viewModel.setDateFilter(filter) },'''
replacement1 = '''            onFilterSelected = { filter -> viewModel.setFilter(filter) },'''

content = content.replace(target1, replacement1)

target2 = '''        AllTransactionsDialog(
            transactions = transactions,
            allTransactions = allTransactions,
            currencyUnit = currencyUnit,
            selectedFilter = activeDateFilter,
            onFilterSelected = { filter -> viewModel.setFilter(filter) },
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )'''

replacement2 = '''        AllTransactionsDialog(
            transactions = transactions,
            allTransactions = allTransactions,
            currencyUnit = currencyUnit,
            viewModel = viewModel,
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )'''

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
