import re

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'r') as f:
    content = f.read()

# Replace the manual SMS picker modal with SmsSenderPickerSheet
manual_modal_pattern = r'    // Modal: Select Bank Sender from Device SMS \(Screenshot 7\)\n    if \(showSmsPickerModal\) \{[\s\S]*?\n            \}\n        \}\n    \}'
new_modal = """    // Modal: Select Bank Sender from Device SMS (Screenshot 7)
    if (showSmsPickerModal) {
        SmsSenderPickerSheet(
            onDismiss = { showSmsPickerModal = false },
            onSenderSelected = { selectedAddress ->
                smsShortcode = selectedAddress
                showSmsPickerModal = false
            }
        )
    }"""
content = re.sub(manual_modal_pattern, new_modal, content)

# Modify the Row to a simplified text field and button
old_row = r'                    // SMS Shortcode Binding\n                    Row\([\s\S]*?Icon\(Icons\.Default\.Sms, contentDescription = "انتخاب سرشماره", tint = Color\(0xFF90CAF9\)\)\n                        \}\n                    \}'

new_row = """                    // SMS Shortcode Binding
                    OutlinedTextField(
                        value = smsShortcode,
                        onValueChange = { smsShortcode = it },
                        label = { Text("سرشماره پیامک این بانک") },
                        placeholder = { Text("مثال: 9830009417") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1A212D),
                            unfocusedContainerColor = Color(0xFF1A212D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B6FE8),
                            unfocusedBorderColor = Color(0xFF2E3848)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showSmsPickerModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(androidx.compose.material.icons.filled.MailOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📩 انتخاب سرشماره از بین پیامک‌های گوشی")
                    }"""
content = re.sub(old_row, new_row, content)

# Check for MailOutline import
if "import androidx.compose.material.icons.filled.MailOutline" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Sms", "import androidx.compose.material.icons.filled.Sms\nimport androidx.compose.material.icons.filled.MailOutline")

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'w') as f:
    f.write(content)
