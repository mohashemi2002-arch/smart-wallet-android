import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = '''    var selectedBankAccount by remember { mutableStateOf<com.example.data.model.BankAccount?>(null) }'''

replacement = '''    var selectedBankAccount by remember { mutableStateOf<com.example.data.model.BankAccount?>(null) }
    
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    
    if (showDatePickerDialog) {
        com.example.ui.components.JalaliDatePickerDialog(
            initialMillis = selectedDateFilter?.startMillis,
            onDateSelected = { start, end, title ->
                viewModel.updateDateFilter(start, end, title)
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
