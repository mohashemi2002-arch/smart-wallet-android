package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PersianFormatters
import com.example.ui.components.PoolakiPlusBadge
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()

    var selectedCategoryForEdit by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بودجه‌بندی و مدیریت سقف مصرف", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
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
                BudgetDonutChart(categories, transactions, currencyUnit)
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "سیستم هشدار هوشمند مصرف",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE6E1E5)
                        )
                        Text(
                            text = "سقف بودجه هر دسته‌بندی را تعیین کنید. هنگام نزدیک شدن به سقف مصرف (۸۰٪ و ۱۰۰٪) هشدار ارسال می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }
            }

            items(categories, key = { it.id }) { category ->
                val spentToman = transactions
                    .filter { it.categoryName == category.name && it.type == TransactionType.EXPENSE }
                    .sumOf { it.amountToman }

                CategoryBudgetCard(
                    category = category,
                    spentToman = spentToman,
                    currencyUnit = currencyUnit,
                    onEditBudget = { selectedCategoryForEdit = category }
                )
            }
        }
    }

    selectedCategoryForEdit?.let { category ->
        EditBudgetDialog(
            category = category,
            onDismiss = { selectedCategoryForEdit = null },
            onConfirm = { newLimit ->
                viewModel.updateCategoryBudget(category, newLimit)
                selectedCategoryForEdit = null
            }
        )
    }
}

@Composable
fun CategoryBudgetCard(
    category: Category,
    spentToman: Long,
    currencyUnit: CurrencyUnit,
    onEditBudget: () -> Unit
) {
    val limitToman = category.budgetLimitToman
    val ratio = if (limitToman > 0) spentToman.toFloat() / limitToman.toFloat() else 0f
    val percentInt = (ratio * 100).toInt()

    val progressColor = when {
        ratio >= 1.0f -> Color(0xFFF2B8B5)
        ratio >= 0.8f -> Color(0xFFFFB74D)
        else -> Color(0xFFB1E5D3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_budget_card_${category.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE6E1E5)
                )

                IconButton(onClick = onEditBudget, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش سقف", tint = Color(0xFFD0BCFF), modifier = Modifier.size(18.dp))
                }
            }

            LinearProgressIndicator(
                progress = { ratio.coerceAtMost(1.0f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = Color(0xFF49454F),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مصرف شده: ${PersianFormatters.formatMoney(spentToman, currencyUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = progressColor,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "سقف: ${PersianFormatters.formatMoney(limitToman, currencyUnit)} (${PersianFormatters.toPersianDigits(percentInt.toString())}٪)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCAC4D0)
                )
            }

            if (ratio >= 1.0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF2B8B5), modifier = Modifier.size(16.dp))
                    Text(
                        text = "هشدار: از سقف تعیین شده برای این دسته‌بندی عبور کرده‌اید!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF2B8B5),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EditBudgetDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (newLimitToman: Long) -> Unit
) {
    var limitText by remember { mutableStateOf(category.budgetLimitToman.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش سقف بودجه ${category.name}") },
        text = {
            Column {
                Text("سقف بودجه ماهانه را به تومان وارد نمایید:", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newLimit = limitText.toLongOrNull() ?: 0L
                    onConfirm(newLimit)
                }
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun BudgetDonutChart(
    categories: List<Category>,
    transactions: List<Transaction>,
    currencyUnit: CurrencyUnit
) {
    var totalBudget = 0L
    var totalSpent = 0L

    val categorySpends = categories.map { category ->
        val spent = transactions
            .filter { it.categoryName == category.name && it.type == TransactionType.EXPENSE }
            .sumOf { it.amountToman }
        
        totalBudget += category.budgetLimitToman
        totalSpent += spent
        
        Pair(category, spent)
    }.filter { it.second > 0 } // Only chart categories with some spending

    val remainingBudget = (totalBudget - totalSpent).coerceAtLeast(0L)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "وضعیت کلی بودجه",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFE6E1E5)
            )

            if (totalBudget > 0) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 24.dp.toPx()
                        val size = size.width - strokeWidth
                        
                        var startAngle = -90f
                        
                        // First draw the background/remaining track
                        drawArc(
                            color = Color(0xFF49454F),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = Size(size, size),
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        )

                        // Draw each category slice
                        val colors = listOf(
                            Color(0xFFB1E5D3), Color(0xFFD0BCFF), Color(0xFFF2B8B5),
                            Color(0xFF81D8D0), Color(0xFFF6D88A), Color(0xFF90C2F9)
                        )
                        
                        var colorIndex = 0
                        categorySpends.forEach { (_, spent) ->
                            val sweepAngle = (spent.toFloat() / totalBudget.toFloat()) * 360f
                            // Limit total drawn angles to 360 to not overlap crazily if over budget
                            if (startAngle + sweepAngle - (-90f) <= 360f || totalSpent <= totalBudget) {
                                drawArc(
                                    color = colors[colorIndex % colors.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle.coerceAtMost(360f),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    size = Size(size, size),
                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                )
                            }
                            startAngle += sweepAngle
                            colorIndex++
                        }
                        
                        // Draw over budget warning ring if needed
                        if (totalSpent > totalBudget) {
                            drawArc(
                                color = Color(0xFFF2B8B5).copy(alpha = 0.5f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                size = Size(size, size),
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val ratio = (totalSpent.toFloat() / totalBudget.toFloat()) * 100
                        Text(
                            text = "${PersianFormatters.toPersianDigits(ratio.toInt().toString())}٪",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (totalSpent > totalBudget) Color(0xFFF2B8B5) else Color(0xFFB1E5D3)
                        )
                        Text(
                            text = "مصرف شده",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("کل بودجه", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCAC4D0))
                        Text(PersianFormatters.formatMoney(totalBudget, currencyUnit), style = MaterialTheme.typography.titleMedium, color = Color(0xFFE6E1E5))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (totalSpent > totalBudget) "بیشتر از بودجه" else "باقی‌مانده", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCAC4D0))
                        val diff = if (totalSpent > totalBudget) totalSpent - totalBudget else remainingBudget
                        Text(PersianFormatters.formatMoney(diff, currencyUnit), style = MaterialTheme.typography.titleMedium, color = if (totalSpent > totalBudget) Color(0xFFF2B8B5) else Color(0xFFB1E5D3))
                    }
                }
                
            } else {
                Text(
                    text = "هنوز هیچ سقف بودجه‌ای تعیین نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCAC4D0)
                )
            }
        }
    }
}
