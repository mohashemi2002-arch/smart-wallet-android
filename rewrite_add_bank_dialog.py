import re

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'r') as f:
    content = f.read()

import_lines = "import androidx.compose.ui.window.DialogProperties\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll\n"
content = content.replace("import androidx.compose.ui.window.Dialog\n", "import androidx.compose.ui.window.Dialog\n" + import_lines)

dialog_start = """    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .navigationBarsPadding()
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF222834),
            tonalElevation = 8.dp
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {"""

content = re.sub(r'    Dialog\(onDismissRequest = onDismiss\) \{\n        Surface\([\s\S]*?Column\(\n                modifier = Modifier\n                    \.fillMaxWidth\(\)\n                    \.padding\(20\.dp\),\n                verticalArrangement = Arrangement\.spacedBy\(16\.dp\)\n            \) \{', dialog_start, content)


button_pattern = r'                // Confirm / Submit Button\n                Button\([\s\S]*?onClick = \{[\s\S]*?\}\n                \) \{\n                    Text\("ثبت  >", fontWeight = FontWeight.Bold, fontSize = 16.sp\)\n                \}\n            \}\n        \}\n    \}'

new_footer = """                }
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
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3848))
                        ) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val finalBankName = if (bankName.isNotBlank() && bankName != "بانک ناشناخته") bankName else "بانک تخصصی"
                                val finalAccName = accountName.ifBlank { if (cardNumber.length >= 4) "کارت ${cardNumber.takeLast(4)}" else "حساب اصلی" }
                                val rialAmount = balanceTextRial.toLongOrNull() ?: 0L
                                val tomanAmount = if (initialBalanceEnabled) rialAmount / 10L else 0L

                                onConfirm(
                                    finalBankName,
                                    finalAccName,
                                    cardNumber.ifBlank { "6037-XXXX-XXXX-1111" },
                                    null,
                                    tomanAmount,
                                    selectedColorHex,
                                    smsShortcode.ifBlank { null },
                                    accountType,
                                    readSmsEnabled,
                                    initialBalanceEnabled
                                )
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("submit_bank_account_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5C82E6),
                                contentColor = Color.White
                            )
                        ) {
                            Text("ثبت و ذخیره کارت", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }"""

content = re.sub(button_pattern, new_footer, content)

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'w') as f:
    f.write(content)
