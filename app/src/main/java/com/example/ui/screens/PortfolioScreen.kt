package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Asset
import com.example.data.model.AssetType
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.components.PersianFormatters
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: FinanceViewModel, // kept for backward compatibility signature if needed, but not heavily used here
    portfolioViewModel: PortfolioViewModel = viewModel(),
    onBack: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    val totalPortfolioValue by portfolioViewModel.totalPortfolioValue.collectAsState()
    val assetMetrics by portfolioViewModel.assetMetrics.collectAsState()
    var selectedAssetForUpdate by remember { mutableStateOf<Asset?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("کیف پول دارایی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMarket) {
                        Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی نرخ‌ها")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "ثبت دارایی")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Card: Total Net Worth
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ارزش کل دارایی‌ها",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = PersianFormatters.formatMoney(totalPortfolioValue),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "سبد دارایی شما",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assetMetrics) { metric ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedAssetForUpdate = metric.asset },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(metric.asset.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${PersianFormatters.toPersianDigits(metric.asset.totalQuantity.toString())} ${metric.asset.unit}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = PersianFormatters.formatMoney(metric.currentValueToman),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val pnlColor = if (metric.pnlToman >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                val pnlSign = if (metric.pnlToman >= 0) "+" else ""
                                Text(
                                    text = "$pnlSign${PersianFormatters.toPersianDigits(String.format(Locale.US, "%.1f", metric.pnlPercentage))} %",
                                    color = pnlColor,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(96.dp)) }
            }
        }

        if (selectedAssetForUpdate != null) {
            var newPriceText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { selectedAssetForUpdate = null },
                title = { Text("بروزرسانی قیمت ${selectedAssetForUpdate!!.name}") },
                text = {
                    OutlinedTextField(
                        value = newPriceText,
                        onValueChange = { newPriceText = it.filter { c -> c.isDigit() } },
                        label = { Text("قیمت روز جدید (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val newPrice = newPriceText.toLongOrNull() ?: 0L
                        if (newPrice > 0) {
                            portfolioViewModel.updateAssetPrice(selectedAssetForUpdate!!, newPrice)
                        }
                        selectedAssetForUpdate = null
                    }) {
                        Text("ثبت قیمت")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAssetForUpdate = null }) {
                        Text("انصراف")
                    }
                }
            )
        }

        if (showAddDialog) {
            com.example.ui.components.AddAssetBottomSheet(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, type, quantity, priceToman, unit ->
                    portfolioViewModel.addAsset(name, type, quantity, priceToman, unit)
                    showAddDialog = false
                }
            )
        }
    }
}
