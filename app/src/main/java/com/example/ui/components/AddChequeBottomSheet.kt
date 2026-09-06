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
import com.example.data.model.ChequeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChequeBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (sayadNumber: String, type: ChequeType, amount: Long, bankName: String, partyName: String) -> Unit
) {
    var sayadNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ChequeType.ISSUED) }

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
            Text("ثبت چک صیادی", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = selectedType == ChequeType.ISSUED,
                    onClick = { selectedType = ChequeType.ISSUED },
                    label = { Text("صادر شده (پرداختی)", color = Color.White) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = selectedType == ChequeType.RECEIVED,
                    onClick = { selectedType = ChequeType.RECEIVED },
                    label = { Text("دریافت شده", color = Color.White) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = sayadNumber,
                onValueChange = { sayadNumber = it.filter { c -> c.isDigit() } },
                label = { Text("شناسه صیادی (۱۶ رقم)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                label = { Text("مبلغ چک (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("بانک صادرکننده") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = partyName,
                onValueChange = { partyName = it },
                label = { Text("در وجه / از طرف") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val a = amountText.toLongOrNull() ?: 0L
                    onConfirm(sayadNumber.ifBlank { "0000000000000000" }, selectedType, a, bankName.ifBlank { "نامشخص" }, partyName.ifBlank { "نامشخص" })
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
            ) {
                Text("ثبت چک", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
