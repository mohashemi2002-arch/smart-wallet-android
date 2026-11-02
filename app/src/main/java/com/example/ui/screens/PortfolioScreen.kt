package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Asset
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: FinanceViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNavigateToMarket: () -> Unit = {}
) {
    val assets by viewModel.assets.collectAsState()
    val totalAssetValue by viewModel.totalAssetValue.collectAsState()
    val assetCount by viewModel.assetCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            TopBarPortfolio(onBack = onBack)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "ارزش کل دارایی",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${formatMoney(totalAssetValue.valueOrZero())} تومان",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryAssetCard(
                    title = "تعداد دارایی‌ها",
                    value = "${assets.size}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SummaryAssetCard(
                    title = "تغییرات قیمت",
                    value = "+۲.۵٪",
                    tint = MaterialTheme.colorScheme.success,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            AssetListSection(assets = assets, onNavigateToMarket = onNavigateToMarket)
        }
    }
}

@Composable
private fun TopBarPortfolio(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
        }
        Text(
            text = "دارایی‌ها",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SummaryAssetCard(
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

@Composable
private fun AssetListSection(
    assets: List<Asset>,
    onNavigateToMarket: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(assets) { asset ->
            AssetRowCard(asset = asset)
        }
    }
}

@Composable
private fun AssetRowCard(asset: Asset) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تعداد: ${asset.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "قیمت واحد: ${formatMoney(asset.pricePerUnit)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "قیمت کل: ${formatMoney(asset.totalValue)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${asset.priceChangePercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (asset.priceChangePercent >= 0) Color.Green else Color.Red
                )
            }
        }
    }
}

private fun formatMoney(value: Long): String {
    return if (value < 1000) {
        value.toString()
    } else {
        val formatted = value.toString().reversed()
        val formattedValue = formattedValue.reversed().chunked(3)
        val formattedValueArray = formattedValue.joinToString(",")
        return formattedValueArray
    }
}

private fun Long.valueOrZero(): Long = this
