package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingReviewBottomSheet(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    onIgnore: (Transaction) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amountToman.toString()) }
    var categoryText by remember { mutableStateOf(transaction.categoryName) }
    var merchantText by remember { mutableStateOf(transaction.merchantName) }
    var type by remember { mutableStateOf(transaction.type) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding().navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "تایید تراکنش پیامکی",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("مبلغ (تومان)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = merchantText,
                onValueChange = { merchantText = it },
                label = { Text("فروشگاه / مبدا") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = categoryText,
                onValueChange = { categoryText = it },
                label = { Text("دسته‌بندی") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    label = { Text("برداشت") }
                )
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME },
                    label = { Text("واریز") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newAmount = amountText.toLongOrNull() ?: transaction.amountToman
                        val updated = transaction.copy(
                            amountToman = newAmount,
                            categoryName = categoryText,
                            merchantName = merchantText,
                            type = type,
                            status = TransactionStatus.CONFIRMED
                        )
                        onConfirm(updated)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تایید و ثبت")
                }
                
                OutlinedButton(
                    onClick = { onIgnore(transaction) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("نادیده گرفتن")
                }
            }
        }
    }
}
