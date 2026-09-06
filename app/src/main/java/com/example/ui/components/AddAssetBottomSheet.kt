package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AssetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AssetType, quantity: Double, priceToman: Long, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var priceRial by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("گرم") }
    var selectedType by remember { mutableStateOf(AssetType.GOLD_18K) }

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
            Text("افزودن دارایی جدید", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("عنوان (مثلاً طلا ۱۸ عیار)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("تعداد / مقدار") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("واحد (مثلاً گرم)") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = priceRial,
                onValueChange = { priceRial = it.filter { c -> c.isDigit() } },
                label = { Text("ارزش هر واحد (ریال)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val q = quantity.toDoubleOrNull() ?: 0.0
                    val p = (priceRial.toLongOrNull() ?: 0L) / 10L
                    onConfirm(name.ifBlank { "دارایی" }, selectedType, q, p, unit)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
            ) {
                Text("ثبت دارایی", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
