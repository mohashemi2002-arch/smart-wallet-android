import re
with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'r') as f:
    content = f.read()

# Add states for dialogs
states = """    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var showAddChequeDialog by remember { mutableStateOf(false) }"""
content = content.replace("    var selectedTabIndex by remember { mutableIntStateOf(0) }", states)

# Update FloatingActionButton
fab_code = """
            FloatingActionButton(onClick = {
                if (selectedTabIndex == 0) showAddLoanDialog = true else showAddChequeDialog = true
            }) {"""
content = content.replace("            FloatingActionButton(onClick = { android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current, \"در حال توسعه...\", android.widget.Toast.LENGTH_SHORT).show() }) {", fab_code)

# Add dialogs at the end
dialogs_code = """
    if (showAddLoanDialog) {
        com.example.ui.components.AddLoanBottomSheet(
            onDismiss = { showAddLoanDialog = false },
            onConfirm = { title, total, count, amount ->
                loansViewModel.addLoan(title, total, count, amount, System.currentTimeMillis() + 86400000L * 30L)
                showAddLoanDialog = false
                android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current, "وام با موفقیت ثبت شد", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddChequeDialog) {
        com.example.ui.components.AddChequeBottomSheet(
            onDismiss = { showAddChequeDialog = false },
            onConfirm = { sayad, type, amount, bank, party ->
                chequesViewModel.addCheque(sayad, type, amount, System.currentTimeMillis() + 86400000L * 7L, bank, party, null)
                showAddChequeDialog = false
                android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current, "چک با موفقیت ثبت شد", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}"""
content = content.replace("    }\n}", dialogs_code)

with open('app/src/main/java/com/example/ui/screens/DebtsChecksScreen.kt', 'w') as f:
    f.write(content)
