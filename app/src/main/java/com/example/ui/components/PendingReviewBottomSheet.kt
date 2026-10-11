package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Transaction
import com.example.data.model.BankAccount
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingReviewBottomSheet(
    transaction: Transaction,
    banks: List<BankAccount> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    onIgnore: (Transaction) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amountToman.toString()) }
    var categoryText by remember { mutableStateOf(transaction.categoryName) }
    var merchantText by remember { mutableStateOf(transaction.merchantName) }
    var type by remember { mutableStateOf(transaction.type) }
    var expandedAccount by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf(transaction.cardNumber ?: "") }

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

            ExposedDropdownMenuBox(
                expanded = expandedAccount,
                onExpandedChange = { expandedAccount = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayName = if (selectedCard.startsWith("ID:")) {
                    val id = selectedCard.removePrefix("ID:").toLongOrNull()
                    val acc = banks.find { it.id == id }
                    if (acc != null) "${acc.bankName} - ${acc.cardNumber.takeLast(4)}" else selectedCard
                } else {
                    banks.find { it.cardNumber == selectedCard }?.let { "${it.bankName} - ${it.cardNumber.takeLast(4)}" } ?: "انتخاب کارت مبدا/مقصد"
                }
                
                OutlinedTextField(
                    value = displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("کارت مرتبط") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAccount,
                    onDismissRequest = { expandedAccount = false }
                ) {
                    banks.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text("${acc.bankName} - ${acc.cardNumber.takeLast(4)}") },
                            onClick = { selectedCard = "ID:${acc.id}"; expandedAccount = false }
                        )
                    }
                }
            }

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
                        val cleanDigits = amountText.replace(",", "").replace("،", "").replace(" ", "").trim()
                        val englishDigits = com.example.ui.components.PersianFormatters.toEnglishDigits(cleanDigits)
                        val newAmount = englishDigits.toLongOrNull() ?: transaction.amountToman
                        val updated = transaction.copy(
                            amountToman = newAmount,
                            categoryName = categoryText.ifBlank { "متفرقه" },
                            merchantName = merchantText,
                            type = type,
                            cardNumber = selectedCard.takeIf { it.isNotBlank() },
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
