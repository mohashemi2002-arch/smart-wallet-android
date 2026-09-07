package com.example.ui.components

import com.example.ui.components.formatAsToman

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
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModel.TransactionDateFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsDialog(
    transactions: List<Transaction>,
    allTransactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {
    var showCalendarDialog by remember { mutableStateOf(false) }
    val activeDateFilter by viewModel.activeDateFilter.collectAsState()

    if (showCalendarDialog) {
        JalaliCalendarDialog(
            initialFilter = activeDateFilter,
            transactions = allTransactions,
            onFilterSelected = { filter -> viewModel.setFilter(filter) },
            onDismiss = { showCalendarDialog = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B1120)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تراکنش‌ها",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
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
                        color = Color(0xFF333E52)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.previousPeriod() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "قبلی",
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                modifier = Modifier.clickable { showCalendarDialog = true }.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = displayTitle,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            
                            IconButton(
                                onClick = { viewModel.nextPeriod() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "بعدی",
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Total Sum Display
                val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman }
                val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman }
                val netTotal = totalIncome - totalExpense
                val isPositiveNet = netTotal >= 0
                val signStr = if (isPositiveNet) "+" else "-"

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مجموع:",
                            color = Color(0xFF94A3B8),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$signStr ${Math.abs(netTotal).formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان"}",
                            color = if (isPositiveNet) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Days
                val grouped = remember(transactions) {
                    transactions.groupBy { tx ->
                        PersianFormatters.formatDate(tx.dateMillis)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    if (grouped.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "هیچ تراکنشی برای این تاریخ ثبت نشده است",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    grouped.forEach { (dateStr, dailyTxs) ->
                        val incomeSum = dailyTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman }
                        val expenseSum = dailyTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman }
                        val net = incomeSum - expenseSum
                        val isPositive = net >= 0
                        
                        val sumText = (if (isPositive) "+" else "-") + " " + Math.abs(net).formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان"

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
                color = if (isPositive) Color(0xFF4ADE80) else Color(0xFFF87171)
            )
        }

        items.forEach { tx ->
            val isInc = tx.type == TransactionType.INCOME
            val sign = if (isInc) "+" else "-"
            val iconInfo = CategoryIconMap.getIconForCategory(tx.categoryName)
            val title = tx.categoryName.ifBlank { "نامشخص" }
            val amountText = "$sign ${tx.amountToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان"}"

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
                        .background(Color(0xFF1E2638), RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Amount
                    Column {
                        Text(
                            text = amountText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isInc) Color(0xFF4ADE80) else Color(0xFFF87171)
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
        }
    }
}
