package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PdfReportGenerator
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconMap
import com.example.ui.components.PersianFormatters
import com.example.ui.components.formatAsToman
import com.example.ui.viewmodel.DateRangeType
import com.example.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch

private val ColorIncomeGreen = Color(0xFF10B981)
private val ColorExpenseRed = Color(0xFFEF4444)
private val ColorIncomeSoft = Color(0xFFE6F7F0)
private val ColorExpenseSoft = Color(0xFFFEECEC)
private val ColorNeutralSoft = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val filterState by viewModel.filterState.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val totalInflow by viewModel.totalInflow.collectAsState()
    val totalOutflow by viewModel.totalOutflow.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val expenseTransactions = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
    }
    val categoryData = remember(expenseTransactions) {
        expenseTransactions.groupBy { it.categoryName }
            .mapValues { it.value.sumOf { tx -> tx.amountToman } }
    }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "گزارش‌های پیشرفته",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "تحلیل هوشمند درآمد، هزینه و تراز مالی",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                PdfReportGenerator.generateAndSharePdf(
                                    context = context,
                                    transactions = transactions,
                                    totalInflow = totalInflow,
                                    totalOutflow = totalOutflow,
                                    netBalance = netBalance
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "خروجی PDF",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "خروجی PDF",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Date Range Filter Pills
            item {
                DateRangeFilterSection(
                    selectedType = filterState.dateRangeType,
                    onSelect = { viewModel.updateDateRange(it) }
                )
            }

            // 2. Executive Financial KPI Summary Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExecutiveKpiCard(
                        inflow = totalInflow,
                        outflow = totalOutflow,
                        net = netBalance,
                        transactionCount = transactions.size
                    )
                }
            }

            // 3. Category Expenses & Donut Chart Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExpenseAnalyticsCard(
                        categoryData = categoryData,
                        totalExpense = totalOutflow
                    )
                }
            }

            // 4. Transaction Type Selector & Header
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TransactionTypeFilterRow(
                        selectedTypes = filterState.selectedTypes,
                        onSelectAll = {
                            if (!filterState.selectedTypes.contains(TransactionType.EXPENSE) ||
                                !filterState.selectedTypes.contains(TransactionType.INCOME)
                            ) {
                                viewModel.toggleTypeFilter(TransactionType.EXPENSE)
                                viewModel.toggleTypeFilter(TransactionType.INCOME)
                            }
                        },
                        onToggleType = { viewModel.toggleTypeFilter(it) },
                        totalCount = transactions.size
                    )
                }
            }

            // 5. Transactions List
            if (transactions.isEmpty()) {
                item {
                    EmptyTransactionsPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }
            } else {
                items(
                    items = transactions,
                    key = { it.id }
                ) { tx ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ReportTransactionCard(
                            tx = tx,
                            onEditCategory = { editingTransaction = tx }
                        )
                    }
                }
            }
        }
    }

    // Category Change Bottom Sheet for Confirmed Transaction
    editingTransaction?.let { tx ->
        val catType = if (tx.type == TransactionType.INCOME) "INCOME" else "EXPENSE"
        com.example.ui.components.CategorySelectionBottomSheet(
            categoryType = catType,
            selectedCategoryName = tx.categoryName,
            onCategorySelected = { cat ->
                viewModel.updateTransactionCategory(tx.id, cat.name)
                editingTransaction = null
            },
            onDismiss = { editingTransaction = null }
        )
    }
}

/**
 * Filter carousel for choosing reporting period.
 */
@Composable
private fun DateRangeFilterSection(
    selectedType: DateRangeType,
    onSelect: (DateRangeType) -> Unit
) {
    val ranges = listOf(
        DateRangeType.TODAY to "امروز",
        DateRangeType.THIS_WEEK to "این هفته",
        DateRangeType.THIS_MONTH to "این ماه",
        DateRangeType.LAST_3_MONTHS to "۳ ماه اخیر",
        DateRangeType.ALL_TIME to "همه‌زمان‌ها"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ranges) { (type, label) ->
            val isSelected = selectedType == type
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
            val contentColor = if (isSelected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(type) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Hero Card presenting financial performance KPIs, net balance, and inflows/outflows.
 */
@Composable
private fun ExecutiveKpiCard(
    inflow: Long,
    outflow: Long,
    net: Long,
    transactionCount: Int
) {
    val isPositive = net >= 0
    val savingsPercent = if (inflow > 0) {
        ((net.toDouble() / inflow.toDouble()) * 100).toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Status badge & Net Balance Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "تراز مالی خالص دوره",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = (if (isPositive) "+" else "") + net.formatAsToman(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            ),
                            color = if (isPositive) ColorIncomeGreen else ColorExpenseRed
                        )
                        Text(
                            text = "تومان",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isPositive) ColorIncomeSoft else ColorExpenseSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) ColorIncomeGreen else ColorExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPositive) "تراز مثبت" else "کسری تراز",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) ColorIncomeGreen else ColorExpenseRed
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            // Sub-metrics Grid (Inflow, Outflow, Savings Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Inflow Item
                MetricTile(
                    title = "کل درآمد",
                    amount = "+${inflow.formatAsToman()} تومان",
                    amountColor = ColorIncomeGreen,
                    icon = Icons.Default.ArrowDownward,
                    iconBgColor = ColorIncomeSoft,
                    iconTint = ColorIncomeGreen,
                    modifier = Modifier.weight(1f)
                )

                // Outflow Item
                MetricTile(
                    title = "کل هزینه",
                    amount = "-${outflow.formatAsToman()} تومان",
                    amountColor = ColorExpenseRed,
                    icon = Icons.Default.ArrowUpward,
                    iconBgColor = ColorExpenseSoft,
                    iconTint = ColorExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Summary Bar / Extra metrics
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "نرخ پس‌انداز دوره:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${PersianFormatters.toPersianDigits(savingsPercent.toString())}٪",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${PersianFormatters.toPersianDigits(transactionCount.toString())} تراکنش",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    amount: String,
    amountColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Card holding the Donut Chart and top categories breakdown.
 */
@Composable
private fun ExpenseAnalyticsCard(
    categoryData: Map<String, Long>,
    totalExpense: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "ترکیب و سهم هزینه‌ها",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (categoryData.isNotEmpty()) {
                    Text(
                        text = "${PersianFormatters.toPersianDigits(categoryData.size.toString())} دسته‌بندی",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (categoryData.isEmpty() || totalExpense <= 0) {
                // Empty chart message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "هزینه‌ای در این بازه زمانی ثبت نشده است",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Modern Donut Chart
                ModernDonutChart(
                    data = categoryData,
                    totalAmount = totalExpense,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )

                // Category Progress List
                Text(
                    text = "بیشترین هزینه‌ها به تفکیک دسته:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CategoryExpenseBreakdown(
                    data = categoryData,
                    totalExpense = totalExpense,
                    maxItemsToShow = 5
                )
            }
        }
    }
}

/**
 * Filter pills for toggling between All, Income, and Expense transactions.
 */
@Composable
private fun TransactionTypeFilterRow(
    selectedTypes: Set<TransactionType>,
    onSelectAll: () -> Unit,
    onToggleType: (TransactionType) -> Unit,
    totalCount: Int
) {
    val isAllSelected = selectedTypes.contains(TransactionType.INCOME) &&
            selectedTypes.contains(TransactionType.EXPENSE)
    val isOnlyExpense = selectedTypes.contains(TransactionType.EXPENSE) &&
            !selectedTypes.contains(TransactionType.INCOME)
    val isOnlyIncome = selectedTypes.contains(TransactionType.INCOME) &&
            !selectedTypes.contains(TransactionType.EXPENSE)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ریز تراکنش‌ها",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${PersianFormatters.toPersianDigits(totalCount.toString())} مورد",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Segmented filter buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ALL
            FilterTabButton(
                label = "همه",
                isSelected = isAllSelected,
                onClick = onSelectAll
            )

            // EXPENSE
            FilterTabButton(
                label = "هزینه",
                isSelected = isOnlyExpense,
                activeColor = ColorExpenseRed,
                onClick = {
                    if (isOnlyExpense) onSelectAll() else {
                        if (selectedTypes.contains(TransactionType.INCOME)) {
                            onToggleType(TransactionType.INCOME)
                        }
                    }
                }
            )

            // INCOME
            FilterTabButton(
                label = "درآمد",
                isSelected = isOnlyIncome,
                activeColor = ColorIncomeGreen,
                onClick = {
                    if (isOnlyIncome) onSelectAll() else {
                        if (selectedTypes.contains(TransactionType.EXPENSE)) {
                            onToggleType(TransactionType.EXPENSE)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterTabButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

/**
 * Modern card representing a single transaction in the reports feed.
 */
@Composable
private fun ReportTransactionCard(
    tx: Transaction,
    onEditCategory: () -> Unit
) {
    val isIncome = tx.type == TransactionType.INCOME
    val (catColor, catIcon) = CategoryIconMap.getIconForCategory(tx.categoryName)
    val amountColor = if (isIncome) ColorIncomeGreen else ColorExpenseRed
    val prefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditCategory() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left (Right in RTL): Icon and Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(catColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = tx.categoryName,
                        tint = catColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tx.categoryName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!tx.merchantName.isNullOrBlank() && tx.merchantName != "ناشناس") {
                            Text(
                                text = tx.merchantName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!tx.bankName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = tx.bankName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = PersianFormatters.formatDate(tx.dateMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Right (Left in RTL): Amount
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$prefix${tx.amountToman.formatAsToman()}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    color = amountColor
                )
                Text(
                    text = "تومان",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Beautiful empty state when no transactions match the selected filters.
 */
@Composable
private fun EmptyTransactionsPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "تراکنشی در این بازه زمانی یافت نشد",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "می‌توانید بازه زمانی را تغییر دهید یا تراکنش‌های جدید ثبت کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
