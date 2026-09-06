import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()", "val activeDateFilter by viewModel.activeDateFilter.collectAsState()")
content = content.replace("selectedDateFilter", "activeDateFilter")

target_text_when = '''                                                text = when (val filter = activeDateFilter) {
                                                    is FinanceViewModel.DateFilter.SpecificDay -> "${filter.jDay} ${com.example.util.JalaliCalendarHelper.monthNames[filter.jMonth - 1]}"
                                                    is FinanceViewModel.DateFilter.FullMonth -> "${com.example.util.JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                                                    null -> "کل تراکنش‌ها"
                                                },'''

replacement_text_when = '''                                                text = when (val filter = activeDateFilter) {
                                                    is FinanceViewModel.TransactionDateFilter.DayFilter -> "${filter.jDay} ${com.example.util.JalaliCalendarHelper.monthNames[filter.jMonth - 1]}"
                                                    is FinanceViewModel.TransactionDateFilter.MonthFilter -> "${com.example.util.JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                                                    is FinanceViewModel.TransactionDateFilter.AllTime -> "کل تراکنش‌ها"
                                                },'''

content = content.replace(target_text_when, replacement_text_when)

target_dialog = '''        AllTransactionsDialog(
            transactions = transactions,
            allTransactions = allTransactions,
            currencyUnit = currencyUnit,
            activeDateFilter = activeDateFilter,
            onFilterSelected = { filter -> viewModel.setDateFilter(filter) },
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )'''

replacement_dialog = '''        AllTransactionsDialog(
            transactions = transactions,
            allTransactions = allTransactions,
            currencyUnit = currencyUnit,
            viewModel = viewModel,
            onDismiss = { showAllTransactionsDialog = false },
            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
        )'''

content = content.replace(target_dialog, replacement_dialog)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
