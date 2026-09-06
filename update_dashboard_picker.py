import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = '''        com.example.ui.components.JalaliDatePickerDialog(
            initialMillis = when(val filter = selectedDateFilter) {
                is FinanceViewModel.DateFilter.SpecificDay -> filter.startMillis
                is FinanceViewModel.DateFilter.FullMonth -> filter.startMillis
                null -> null
            },
            onDateSelected = { start, end, title ->
                // Handled in dialog internally or via manual mapping if used
            },
            onDismiss = { showDatePickerDialog = false }
        )'''

replacement = '''        com.example.ui.components.JalaliCalendarDialog(
            initialFilter = selectedDateFilter,
            transactions = allTransactions,
            onFilterSelected = { filter -> viewModel.setDateFilter(filter) },
            onDismiss = { showDatePickerDialog = false }
        )'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
