import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = r'''                OutlinedTextField\(
                    value = amountText,
                    onValueChange = \{ amountText = it.filter \{ c -> c.isDigit\(\) \} \},
                    label = \{ Text\("مبلغ \(تومان\)"\) \},
                    keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType.Number\),
                    modifier = Modifier.fillMaxWidth\(\)
                \)'''

replacement = r'''                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it in '۰'..'۹' }) {
                            amountText = input
                        }
                    },
                    label = { Text("مبلغ (تومان)") },
                    visualTransformation = com.example.ui.components.ThousandsSeparatorVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("تومان", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth()
                )'''

content = re.sub(target, replacement, content)

# update confirmation parsing
target2 = r'''                    val amount = amountText.toLongOrNull\(\) \?: 0L
                    onConfirm\(amount, selectedType, selectedCategory, merchantText\)'''

replacement2 = r'''                    val amount = com.example.ui.components.PersianCurrencyFormatter.extractCleanAmount(amountText)
                    onConfirm(amount, selectedType, selectedCategory, merchantText)'''

content = re.sub(target2, replacement2, content)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
