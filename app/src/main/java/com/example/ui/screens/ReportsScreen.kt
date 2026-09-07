package com.example.ui.screens

import com.example.ui.components.formatAsToman

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PdfReportGenerator
import com.example.data.model.TransactionType
import com.example.ui.components.PersianFormatters
import com.example.ui.viewmodel.DateRangeType
import com.example.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("گزارش‌های پیشرفته", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            PdfReportGenerator.generateAndSharePdf(context, transactions, totalInflow, totalOutflow, netBalance)
                        }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF خروجی", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterState.dateRangeType == DateRangeType.TODAY,
                        onClick = { viewModel.updateDateRange(DateRangeType.TODAY) },
                        label = { Text("امروز") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterState.dateRangeType == DateRangeType.THIS_WEEK,
                        onClick = { viewModel.updateDateRange(DateRangeType.THIS_WEEK) },
                        label = { Text("این هفته") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterState.dateRangeType == DateRangeType.THIS_MONTH,
                        onClick = { viewModel.updateDateRange(DateRangeType.THIS_MONTH) },
                        label = { Text("این ماه") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterState.dateRangeType == DateRangeType.ALL_TIME,
                        onClick = { viewModel.updateDateRange(DateRangeType.ALL_TIME) },
                        label = { Text("همه‌زمان‌ها") }
                    )
                }
            }
            
            // Type Filters
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterState.selectedTypes.contains(TransactionType.EXPENSE),
                    onClick = { viewModel.toggleTypeFilter(TransactionType.EXPENSE) },
                    label = { Text("هزینه") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF2B8B5))
                )
                FilterChip(
                    selected = filterState.selectedTypes.contains(TransactionType.INCOME),
                    onClick = { viewModel.toggleTypeFilter(TransactionType.INCOME) },
                    label = { Text("درآمد") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFB1E5D3))
                )
            }

            // Chart
            val categoryData = transactions.filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryName }
                .mapValues { it.value.sumOf { tx -> tx.amountToman } }
            if (categoryData.isNotEmpty()) {
                SimpleDonutChart(categoryData, listOf(Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD), Color(0xFF7986CB)), Modifier.fillMaxWidth().height(200.dp).padding(16.dp))
            }
            // KPIs
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("کل درآمد:", color = Color(0xFF388E3C))
                        Text(totalInflow.formatAsToman() + " تومان", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("کل هزینه:", color = Color(0xFFD32F2F))
                        Text(totalOutflow.formatAsToman() + " تومان", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تراز خالص:", fontWeight = FontWeight.Bold)
                        Text(netBalance.formatAsToman() + " تومان", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (transactions.isEmpty()) {
                    item {
                        Text(
                            text = "تراکنشی با این فیلترها یافت نشد.",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
                items(transactions) { tx ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(tx.categoryName, fontWeight = FontWeight.Bold)
                                Text(tx.merchantName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(PersianFormatters.formatDate(tx.dateMillis), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = tx.amountToman.formatAsToman() + " تومان",
                                color = if (tx.type == TransactionType.INCOME) Color(0xFF388E3C) else Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
