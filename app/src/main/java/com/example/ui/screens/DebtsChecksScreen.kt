package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtLoan
import com.example.data.model.DebtType
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PersianFormatters
import com.example.ui.components.PoolakiPlusBadge
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsChecksScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val debtLoans by viewModel.debtLoans.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت اقساط، چک و تعهدات", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Color(0xFFE6E1E5))
                    }
                },
                actions = {
                    if (isPremium) PoolakiPlusBadge(Modifier.padding(end = 8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1B1F))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("add_debt_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن تعهد")
            }
        },
        containerColor = Color(0xFF1C1B1F)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFFD0BCFF))
                        Column {
                            Text(
                                text = "یادآور خودکار سررسید (AlarmManager)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE6E1E5)
                            )
                            Text(
                                text = "چک‌های صیادی و اقساط وام پیش از سررسید یادآوری می‌شوند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }
                }
            }

            items(debtLoans, key = { it.id }) { debtLoan ->
                DebtLoanCard(
                    debtLoan = debtLoan,
                    currencyUnit = currencyUnit,
                    onTogglePaid = { viewModel.toggleDebtPaid(debtLoan) },
                    onDelete = { viewModel.deleteDebtLoan(debtLoan) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddDebtLoanDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, type, amount, dueDate, contact ->
                viewModel.addDebtLoan(title, type, amount, dueDate, contact)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DebtLoanCard(
    debtLoan: DebtLoan,
    currencyUnit: CurrencyUnit,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val isPaid = debtLoan.isPaid
    val typeTitle = when (debtLoan.type) {
        DebtType.LOAN_INSTALLMENT -> "قسط وام"
        DebtType.SAYAD_CHECK -> "چک صیادی"
        DebtType.DEBT_OWED -> "بدهی"
        DebtType.RECEIVABLE_DUE -> "طلب"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debt_item_${debtLoan.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) Color(0xFF242328) else Color(0xFF2B2930)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = isPaid,
                    onCheckedChange = { onTogglePaid() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFD0BCFF),
                        checkmarkColor = Color(0xFF381E72),
                        uncheckedColor = Color(0xFF49454F)
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = debtLoan.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isPaid) Color(0xFF938F99) else Color(0xFFE6E1E5)
                    )

                    Text(
                        text = "$typeTitle • سررسید: ${PersianFormatters.formatDate(debtLoan.dueDateMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCAC4D0)
                    )

                    debtLoan.contactName?.let { contact ->
                        Text(
                            text = "طرف حساب: $contact",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD0BCFF)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PersianFormatters.formatMoney(debtLoan.amountToman, currencyUnit),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isPaid) Color(0xFF938F99) else Color(0xFFD0BCFF)
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFCAC4D0))
                }
            }
        }
    }
}

@Composable
fun AddDebtLoanDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: DebtType, amount: Long, dueDateMillis: Long, contact: String?) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var contactText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DebtType.LOAN_INSTALLMENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن قسط / چک جدید") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("عنوان (مثال: قسط وام مسکن، چک لپ‌تاپ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ به تومان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactText,
                    onValueChange = { contactText = it },
                    label = { Text("طرف حساب / بانک (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    val due = System.currentTimeMillis() + (86400000L * 7) // 7 days from now
                    if (titleText.isNotBlank() && amount > 0) {
                        onConfirm(titleText, selectedType, amount, due, contactText.ifBlank { null })
                    }
                }
            ) {
                Text("ثبت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
