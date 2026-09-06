import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Fix AddTransactionDialog Column
content = content.replace(
"""            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {""",
"""            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {"""
)

# Add EditAccountBottomSheet integration
bottom_sheet_code = """
    if (showEditAccountDialog && selectedBankAccount != null) {
        com.example.ui.components.EditAccountBottomSheet(
            account = selectedBankAccount!!,
            onDismiss = { showEditAccountDialog = false; selectedBankAccount = null },
            onUpdate = { acc -> 
                viewModel.updateBankAccount(acc)
                showEditAccountDialog = false
                selectedBankAccount = null
            },
            onDelete = { acc ->
                viewModel.deleteBankAccount(acc)
                showEditAccountDialog = false
                selectedBankAccount = null
            }
        )
    }
"""

content = content.replace("    if (showQuickNoteDialog) {", bottom_sheet_code + "\n    if (showQuickNoteDialog) {")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
