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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.data.model.AssetType
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PersianFormatters
import com.example.ui.components.PoolakiPlusBadge
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToMarket: (() -> Unit)? = null
) {
    val assets by viewModel.assets.collectAsState()
    val totalAssetsValueToman by viewModel.totalAssetsValueToman.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()

    var showAddAssetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سبد دارایی‌های غیرنقدی (Portfolio)", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
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
                onClick = { showAddAssetDialog = true },
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("add_asset_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن دارایی")
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
            // 1. Total Portfolio Header Bento Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2B2930)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مجموع ارزش دارایی‌های سرمایه‌ای",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFCAC4D0)
                            )
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFD0BCFF))
                        }

                        Text(
                            text = PersianFormatters.formatMoney(totalAssetsValueToman, currencyUnit),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD0BCFF)
                        )

                        Text(
                            text = "طلا ۱۸ عیار • سکه امامی • دلار • ارز دیجیتال (نرخ لحظه‌ای بازار)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )

                        if (onNavigateToMarket != null) {
                            Button(
                                onClick = onNavigateToMarket,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_live_market_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B6FE8),
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("مشاهده قیمت آنلاین طلا، دلار و سکه", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Feature Gating Notice if Free
            if (!isPremium) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("دارایی‌های شما محفوظ است. جهت به‌روزرسانی نرخ زنده به پلاس ارتقا دهید.", fontSize = 11.sp)
                            TextButton(onClick = { viewModel.togglePremium() }) {
                                Text("ارتقا")
                            }
                        }
                    }
                }
            }

            // 3. Asset List
            items(assets, key = { it.id }) { asset ->
                AssetItemCard(
                    asset = asset,
                    currencyUnit = currencyUnit,
                    onDelete = { viewModel.deleteAsset(asset) }
                )
            }
        }
    }

    if (showAddAssetDialog) {
        AddAssetDialog(
            onDismiss = { showAddAssetDialog = false },
            onConfirm = { name, type, quantity, priceToman, unit ->
                viewModel.addAsset(name, type, quantity, priceToman, unit)
                showAddAssetDialog = false
            }
        )
    }
}

@Composable
fun AssetItemCard(
    asset: Asset,
    currencyUnit: CurrencyUnit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("asset_item_${asset.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE6E1E5)
                )
                Text(
                    text = "مقدار: ${PersianFormatters.formatDouble(asset.quantity)} ${asset.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCAC4D0)
                )
                Text(
                    text = "قیمت واحد: ${PersianFormatters.formatMoney(asset.currentPriceToman, currencyUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCAC4D0)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PersianFormatters.formatMoney(asset.totalValueToman, currencyUnit),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD0BCFF)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFCAC4D0))
                }
            }
        }
    }
}

@Composable
fun AddAssetDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AssetType, quantity: Double, priceToman: Long, unit: String) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var unitText by remember { mutableStateOf("گرم") }
    var selectedType by remember { mutableStateOf(AssetType.GOLD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن دارایی جدید") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("نام دارایی (مثلاً: طلا ۱۸ عیار، دلار)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("مقدار (مثلاً: 12.5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("قیمت واحد به تومان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = unitText,
                    onValueChange = { unitText = it },
                    label = { Text("واحد (گرم، عدد، دلار)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    val price = priceText.toLongOrNull() ?: 0L
                    if (nameText.isNotBlank() && qty > 0) {
                        onConfirm(nameText, selectedType, qty, price, unitText)
                    }
                }
            ) {
                Text("افزودن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
