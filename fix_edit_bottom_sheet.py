import re

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'r') as f:
    content = f.read()

# Add imports
if "import androidx.compose.material.icons.filled.MailOutline" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Delete", "import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.MailOutline")

# Add state variables
state_vars = """    var balanceTextRial by remember { mutableStateOf((account.balanceToman * 10).toString()) }
    var smsShortcode by remember { mutableStateOf(account.smsShortcode ?: "") }
    var showSmsPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }"""

content = re.sub(r'    var balanceTextRial by remember \{ mutableStateOf\(\(account\.balanceToman \* 10\)\.toString\(\)\) \}\n    var showDeleteConfirm by remember \{ mutableStateOf\(false\) \}', state_vars, content)

# Add SMS UI elements
sms_ui = """            OutlinedTextField(
                value = balanceTextRial,
                onValueChange = { balanceTextRial = it.filter { c -> c.isDigit() } },
                label = { Text("موجودی (ریال)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = smsShortcode,
                onValueChange = { smsShortcode = it },
                label = { Text("سرشماره پیامک این بانک") },
                placeholder = { Text("مثال: 9830009417") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { showSmsPicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.MailOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("📩 انتخاب سرشماره از بین پیامک‌های گوشی")
            }"""

content = re.sub(r'            OutlinedTextField\(\n                value = balanceTextRial,\n                onValueChange = \{ balanceTextRial = it\.filter \{ c -> c\.isDigit\(\) \} \},\n                label = \{ Text\("موجودی \(ریال\)"\) \},\n                keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType\.Number\),\n                modifier = Modifier\.fillMaxWidth\(\)\n            \)', sms_ui, content)

# Update onUpdate call
on_update_old = """                        onUpdate(account.copy(
                            bankName = bankName,
                            accountName = accountName,
                            cardNumber = cardNumber,
                            balanceToman = newBalance
                        ))"""
on_update_new = """                        onUpdate(account.copy(
                            bankName = bankName,
                            accountName = accountName,
                            cardNumber = cardNumber,
                            balanceToman = newBalance,
                            smsShortcode = smsShortcode.ifBlank { null }
                        ))"""
content = content.replace(on_update_old, on_update_new)

# Add SmsSenderPickerSheet composable call
bottom_sheet_call = """    if (showDeleteConfirm) {"""
bottom_sheet_call_new = """    if (showSmsPicker) {
        SmsSenderPickerSheet(
            onDismiss = { showSmsPicker = false },
            onSenderSelected = { selectedAddress ->
                smsShortcode = selectedAddress
                showSmsPicker = false
            }
        )
    }

    if (showDeleteConfirm) {"""
content = content.replace(bottom_sheet_call, bottom_sheet_call_new)

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'w') as f:
    f.write(content)
