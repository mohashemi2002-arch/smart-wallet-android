package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (title: String, totalAmount: Long, count: Int, amount: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var countText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222834),
        modifier = Modifier.imePadding().navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("افزودن وام جدید", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان وام (مثلا وام مسکن)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = totalAmountText,
                onValueChange = { totalAmountText = it.filter { c -> c.isDigit() } },
                label = { Text("مبلغ کل وام (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it.filter { c -> c.isDigit() } },
                    label = { Text("تعداد اقساط") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("مبلغ هر قسط (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    val t = title.ifBlank { "وام جدید" }
                    val tot = totalAmountText.toLongOrNull() ?: 0L
                    val c = countText.toIntOrNull() ?: 1
                    val a = amountText.toLongOrNull() ?: 0L
                    onConfirm(t, tot, c, a)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
            ) {
                Text("ثبت وام", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
