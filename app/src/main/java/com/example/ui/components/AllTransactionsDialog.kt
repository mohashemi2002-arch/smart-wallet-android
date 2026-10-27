package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BankAccount
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModel.TransactionDateFilter
import com.example.util.JalaliCalendarHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsDialog(
    transactions: List<Transaction>,
    allTransactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    viewModel: FinanceViewModel,
    bankAccounts: List<BankAccount> = emptyList(),
    onDismiss: () -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null,
    onEditTransaction: ((Transaction) -> Unit)? = null
) {
    var showCalendarDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    val activeDateFilter by viewModel.activeDateFilter.collectAsState()

    if (showCalendarDialog) {
        JalaliCalendarDialog(
            initialFilter = activeDateFilter,
            transactions = allTransactions,
            onFilterSelected = { filter -> viewModel.setFilter(filter) },
            onDismiss = { showCalendarDialog = false }
        )
    }

    if (transactionToEdit != null) {
        AddOrEditTransactionDialog(
            transactionToEdit = transactionToEdit,
            bankAccounts = bankAccounts,
            currencyUnit = currencyUnit,
            onDismiss = { transactionToEdit = null },
            onDelete = { tx ->
                onDeleteTransaction?.invoke(tx)
                transactionToEdit = null
            },
            onConfirm = { amount, type, category, merchant, src, dest, dateMillis ->
                viewModel.addOrUpdateTransaction(
                    id = transactionToEdit?.id ?: 0L,
                    amountToman = amount,
                    type = type,
                    categoryName = category,
                    merchantName = merchant,
                    sourceAccount = src,
                    destinationAccount = dest,
                    dateMillis = dateMillis
                )
                transactionToEdit = null
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F172A)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "همه‌ی تراکنش‌ها",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Month Picker Pill
                    val displayTitle = when (val filter = activeDateFilter) {
                        is TransactionDateFilter.DayFilter -> {
                            "${filter.jDay} ${JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                        }
                        is TransactionDateFilter.MonthFilter -> {
                            "${JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                        }
                        is TransactionDateFilter.AllTime -> "کل تراکنش‌ها"
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.previousPeriod() },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "ماه قبل",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showCalendarDialog = true }
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = displayTitle,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.nextPeriod() },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "ماه بعد",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Overall Net Summary
                val totalIncome = transactions.filter { it.type == TransactionType.INCOME && it.categoryName != "جابجایی" }.sumOf { it.amountToman }
                val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE && it.categoryName != "جابجایی" }.sumOf { it.amountToman }
                val netTotal = totalIncome - totalExpense
                val isPositiveNet = netTotal >= 0
                val signStr = if (isPositiveNet) "+" else "-"

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تراز کل دوره:",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$signStr ${Math.abs(netTotal).formatAsToman()} ${if (currencyUnit == CurrencyUnit.RIAL) "ریال" else "تومان"}",
                            color = if (isPositiveNet) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Group by Persian Date
                val grouped = remember(transactions) {
                    transactions.groupBy { tx ->
                        PersianFormatters.formatDate(tx.dateMillis)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
                ) {
                    if (grouped.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(54.dp).padding(bottom = 12.dp)
                                )
                                Text(
                                    text = "هیچ تراکنشی برای این بازه ثبت نشده است",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    grouped.forEach { (dateStr, dailyTxs) ->
                        // Calculate daily net (excluding internal transfers)
                        val incomeSum = dailyTxs.filter { it.type == TransactionType.INCOME && it.categoryName != "جابجایی" }.sumOf { it.amountToman }
                        val expenseSum = dailyTxs.filter { it.type == TransactionType.EXPENSE && it.categoryName != "جابجایی" }.sumOf { it.amountToman }
                        val net = incomeSum - expenseSum
                        val isPositive = net >= 0

                        val hasRealIncomeOrExpense = dailyTxs.any { it.categoryName != "جابجایی" }
                        val sumText = if (hasRealIncomeOrExpense) {
                            (if (isPositive) "+" else "-") + " " + Math.abs(net).formatAsToman() + if (currencyUnit == CurrencyUnit.RIAL) " ریال" else " تومان"
                        } else {
                            // Only transfers in this day
                            "${dailyTxs.sumOf { it.amountToman }.formatAsToman()} ${if (currencyUnit == CurrencyUnit.RIAL) "ریال" else "تومان"}"
                        }

                        item(key = dateStr) {
                            DayTransactionGroup(
                                dateText = dateStr,
                                totalSumText = sumText,
                                isPositive = if (hasRealIncomeOrExpense) isPositive else true,
                                items = dailyTxs,
                                currencyUnit = currencyUnit,
                                onTransactionClick = { tx -> transactionToEdit = tx },
                                onDeleteTransaction = onDeleteTransaction
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayTransactionGroup(
    dateText: String,
    totalSumText: String,
    isPositive: Boolean,
    items: List<Transaction>,
    currencyUnit: CurrencyUnit,
    onTransactionClick: (Transaction) -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Daily Header matching Screenshot 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFCAC4D0)
            )
            Text(
                text = totalSumText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Color(0xFF4ADE80) else Color(0xFFF87171)
            )
        }

        items.forEach { tx ->
            val isTransfer = tx.categoryName == "جابجایی" ||
                    tx.notes?.contains("انتقال") == true ||
                    tx.merchantName.contains("انتقال")

            val isInc = tx.type == TransactionType.INCOME
            val sign = if (isTransfer) "" else (if (isInc) "+" else "-")

            val amountValue = if (currencyUnit == CurrencyUnit.RIAL) tx.amountToman * 10 else tx.amountToman
            val amountFormatted = PersianFormatters.toPersianDigits(
                java.text.NumberFormat.getInstance(java.util.Locale.US).format(amountValue)
            )
            val currencySuffix = if (currencyUnit == CurrencyUnit.RIAL) "ریال" else "تومان"
            val amountText = if (isTransfer) "$amountFormatted $currencySuffix ◄►" else "$sign $amountFormatted $currencySuffix"

            val iconInfo = CategoryIconMap.getIconForCategory(tx.categoryName)
            val title = if (isTransfer) "جابجایی" else tx.categoryName.ifBlank { "متفرقه" }

            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteTransaction?.invoke(tx)
                        true
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = onDeleteTransaction != null,
                backgroundContent = {
                    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        Color(0xFFE53935)
                    } else Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, RoundedCornerShape(14.dp))
                            .padding(end = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = Color.White
                            )
                        }
                    }
                }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTransactionClick(tx) },
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Formatted Amount
                        Text(
                            text = amountText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                isTransfer -> Color(0xFFE2E8F0)
                                isInc -> Color(0xFF4ADE80)
                                else -> Color(0xFFF87171)
                            }
                        )

                        // Right: Title & Subtitle + Category/Bank Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFE6E1E5)
                                )

                                if (isTransfer) {
                                    // Compact transfer subtitle matching Screenshot 3: از [لوگو] به حساب [لوگو]
                                    val desc = tx.notes ?: tx.merchantName
                                    val (srcBank, destBank) = remember(desc) {
                                        if (desc.contains("به")) {
                                            val s = desc.substringBefore("به").replace("انتقال", "").replace("از", "").trim()
                                            val d = desc.substringAfter("به").replace("حساب", "").trim()
                                            Pair(s.ifBlank { tx.bankName ?: "بانک" }, d.ifBlank { "بانک" })
                                        } else {
                                            Pair(tx.bankName ?: "بانک مبدا", "بانک مقصد")
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "به حساب",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        BankLogoImage(
                                            bankName = destBank,
                                            containerSize = 18.dp,
                                            logoSize = 14.dp
                                        )
                                        Text(
                                            text = "از",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        BankLogoImage(
                                            bankName = srcBank,
                                            containerSize = 18.dp,
                                            logoSize = 14.dp
                                        )
                                    }
                                } else if (!tx.merchantName.isNullOrBlank() && tx.merchantName != title) {
                                    Text(
                                        text = tx.merchantName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Category or Transfer Icon
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTransfer) Color(0xFF059669).copy(alpha = 0.2f) else iconInfo.first.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isTransfer) Icons.Default.SwapHoriz else iconInfo.second,
                                        contentDescription = null,
                                        tint = if (isTransfer) Color(0xFF10B981) else iconInfo.first,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
