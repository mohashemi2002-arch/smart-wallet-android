import re

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'r') as f:
    content = f.read()

import_lines = "import androidx.compose.ui.window.DialogProperties\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll\n"
content = content.replace("import androidx.compose.foundation.verticalScroll\n", import_lines)

# ModalBottomSheet wrapper
bottom_sheet_start = """    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222834),
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
    ) {
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {"""

content = re.sub(r'    ModalBottomSheet\([\s\S]*?Column\(\n            modifier = Modifier\n                \.fillMaxWidth\(\)\n                \.padding\(16\.dp\)\n                \.verticalScroll\(rememberScrollState\(\)\),\n            verticalArrangement = Arrangement\.spacedBy\(16\.dp\)\n        \) \{', bottom_sheet_start, content)

buttons_pattern = r'            Row\(modifier = Modifier\.fillMaxWidth\(\), horizontalArrangement = Arrangement\.spacedBy\(16\.dp\)\) \{[\s\S]*?Spacer\(modifier = Modifier\.height\(32\.dp\)\)\n        \}\n    \}'

new_footer = """            }
            Surface(
                tonalElevation = 3.dp,
                color = Color(0xFF181D26),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("حذف", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val newBalance = (balanceTextRial.toLongOrNull() ?: 0L) / 10L
                            onUpdate(account.copy(
                                bankName = bankName,
                                accountName = accountName,
                                cardNumber = cardNumber,
                                balanceToman = newBalance,
                                smsShortcode = smsShortcode.ifBlank { null }
                            ))
                        },
                        modifier = Modifier.weight(1.5f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
                    ) {
                        Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }"""

content = re.sub(buttons_pattern, new_footer, content)

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'w') as f:
    f.write(content)
