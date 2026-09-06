import re

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'r') as f:
    content = f.read()

# Let's fix the colors and styling of the input fields in EditAccountBottomSheet to match AddBankAccountDialog

old_fields = """            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("نام بانک") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("عنوان (مثلاً کارت حقوق)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("شماره کارت") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
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
            )"""

new_fields = """            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("نام بانک") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A212D),
                    unfocusedContainerColor = Color(0xFF1A212D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B6FE8),
                    unfocusedBorderColor = Color(0xFF2E3848)
                )
            )
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("عنوان (مثلاً کارت حقوق)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A212D),
                    unfocusedContainerColor = Color(0xFF1A212D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B6FE8),
                    unfocusedBorderColor = Color(0xFF2E3848)
                )
            )
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it.filter { c -> c.isDigit() }.take(16) },
                label = { Text("شماره کارت (۱۶ رقم)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            OutlinedTextField(
                value = balanceTextRial,
                onValueChange = { balanceTextRial = it.filter { c -> c.isDigit() } },
                label = { Text("موجودی (ریال)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A212D),
                    unfocusedContainerColor = Color(0xFF1A212D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B6FE8),
                    unfocusedBorderColor = Color(0xFF2E3848)
                )
            )
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
            )"""

content = content.replace(old_fields, new_fields)

# Also fix the duplicate import of rememberScrollState in EditAccountBottomSheet.kt
content = content.replace("import androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons")

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'w') as f:
    f.write(content)
