package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.util.JalaliCalendarHelper
import com.example.ui.viewmodel.FinanceViewModel.DateFilter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.PersianFormatters
import com.example.ui.components.CategoryIconMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsDialog(
    transactions: List<Transaction>,
    allTransactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    selectedFilter: DateFilter?,
    onFilterSelected: (DateFilter?) -> Unit,
    onDismiss: () -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {
    var showCalendarDialog by remember { mutableStateOf(false) }

    if (showCalendarDialog) {
        JalaliCalendarDialog(
            initialFilter = selectedFilter,
            transactions = allTransactions,
            onFilterSelected = { filter -> onFilterSelected(filter) },
            onDismiss = { showCalendarDialog = false }
        )
    }
    var searchQuery by remember { mutableStateOf("") }
    
    // Group transactions by Date string (e.g., "جمعه ، ۹ مرداد ۱۴۰۵")
    // For simplicity, we just format the date.
    val grouped = remember(transactions) {
        transactions.groupBy { tx ->
            PersianFormatters.formatDate(tx.dateMillis)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E2638)),
            color = Color(0xFF1E2638)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Toggle search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = Color(0xFFE6E1E5)
                        )
                    }
                    Text(
                        text = "همه‌ی تراکنش‌ها",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE6E1E5)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = Color(0xFFE6E1E5)
                        )
                    }
                }

                // Month Selector Pill Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF2C374D),
                        modifier = Modifier.clip(RoundedCornerShape(50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "قبلی",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مرداد ۱۴۰۵", // Mock for now
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "بعدی",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // List of Days
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    if (grouped.isEmpty()) {
                        item {
                            Text("تراکنشی یافت نشد.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                        }
                    }

                    grouped.forEach { (dateStr, dailyTxs) ->
                        val incomeSum = dailyTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman }
                        val expenseSum = dailyTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman }
                        val net = incomeSum - expenseSum
                        val isPositive = net >= 0
                        
                        val sumText = (if (isPositive) "+" else "-") + " " + PersianFormatters.formatMoney(Math.abs(net), currencyUnit)

                        item {
                            DayTransactionGroup(
                                dateText = dateStr,
                                totalSumText = sumText,
                                isPositive = isPositive,
                                items = dailyTxs,
                                currencyUnit = currencyUnit,
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
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
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
                color = if (isPositive) Color(0xFF81C784) else Color(0xFFE57373)
            )
        }

        items.forEach { tx ->
            val isInc = tx.type == TransactionType.INCOME
            val sign = if (isInc) "+" else "-"
            val iconInfo = CategoryIconMap.getIconForCategory(tx.categoryName)
            val title = tx.categoryName.ifBlank { "نامشخص" }
            val amountText = "$sign ${PersianFormatters.formatMoney(tx.amountToman, currencyUnit)}"

            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            var hasVibrated by remember { mutableStateOf(false) }
        
            val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                        onDeleteTransaction?.invoke(tx)
                        true
                    } else {
                        false
                    }
                }
            )
        
            LaunchedEffect(dismissState.targetValue) {
                if (dismissState.targetValue == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart && !hasVibrated) {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    hasVibrated = true
                } else if (dismissState.targetValue == androidx.compose.material3.SwipeToDismissBoxValue.Settled) {
                    hasVibrated = false
                }
            }

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = onDeleteTransaction != null,
                backgroundContent = {
                    val color = if (dismissState.dismissDirection == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                        Color(0xFFE53935)
                    } else {
                        Color.Transparent
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, RoundedCornerShape(12.dp))
                            .padding(end = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (dismissState.dismissDirection == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = Color.White
                            )
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E2638))
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Amount
                    Column {
                        Text(
                            text = amountText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isInc) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    }
    
                    // Right: Icon & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFE6E1E5)
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = iconInfo.first,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = iconInfo.second,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFF38496B).copy(alpha = 0.5f), thickness = 0.8.dp)
        }
    }
}
