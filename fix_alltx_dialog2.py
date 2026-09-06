import re

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'r') as f:
    content = f.read()

target_sig = '''fun AllTransactionsDialog(
    transactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    selectedFilter: DateFilter?,
    onFilterSelected: (DateFilter?) -> Unit,
    onDismiss: () -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {'''

replacement_sig = '''fun AllTransactionsDialog(
    transactions: List<Transaction>,
    allTransactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    selectedFilter: DateFilter?,
    onFilterSelected: (DateFilter?) -> Unit,
    onDismiss: () -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {'''

content = content.replace(target_sig, replacement_sig)

target_cal = '''        JalaliCalendarDialog(
            initialFilter = selectedFilter,
            transactions = transactions, // Or perhaps pass all transactions if we want dots, but here we only have filtered ones. Wait, if we want dots for all, we need all tx. We'll just pass transactions.
            onFilterSelected = { filter -> onFilterSelected(filter) },
            onDismiss = { showCalendarDialog = false }
        )'''

replacement_cal = '''        JalaliCalendarDialog(
            initialFilter = selectedFilter,
            transactions = allTransactions,
            onFilterSelected = { filter -> onFilterSelected(filter) },
            onDismiss = { showCalendarDialog = false }
        )'''

content = content.replace(target_cal, replacement_cal)

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'w') as f:
    f.write(content)
