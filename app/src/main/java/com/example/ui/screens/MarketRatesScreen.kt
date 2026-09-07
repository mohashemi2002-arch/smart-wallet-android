package com.example.ui.screens

import com.example.ui.components.formatAsToman

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.MarketCategory
import com.example.data.model.MarketRate
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PersianFormatters
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketRatesScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val rates by viewModel.marketRates.collectAsState()
    val isRefreshing by viewModel.isRefreshingMarket.collectAsState()
    val lastUpdated by viewModel.lastMarketUpdatedTime.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<MarketCategory?>(null) }
    var showConverterModal by remember { mutableStateOf(false) }
    var selectedRateForConverter by remember { mutableStateOf<MarketRate?>(null) }
    var showAddToPortfolioDialog by remember { mutableStateOf<MarketRate?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val filteredRates = remember(rates, searchQuery, selectedCategoryFilter) {
        rates.filter { rate ->
            val matchesQuery = searchQuery.isBlank() ||
                    rate.titleFa.contains(searchQuery, ignoreCase = true) ||
                    rate.symbol.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null || rate.category == selectedCategoryFilter
            matchesQuery && matchesCategory
        }
    }

    val usdRate = remember(rates) { rates.find { it.id == "USD" } }
    val goldRate = remember(rates) { rates.find { it.id == "GOLD_18K" } }
    val emamiRate = remember(rates) { rates.find { it.id == "COIN_EMAMI" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "قیمت آنلاین دلار و طلا",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Text(
                                text = "به‌روزرسانی زنده: $lastUpdated",
                                fontSize = 11.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshMarketRates() },
                        modifier = Modifier.testTag("refresh_rates_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "به‌روزرسانی",
                            tint = Color(0xFF90CAF9),
                            modifier = if (isRefreshing) Modifier.rotate(rotationAngle) else Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E2638))
            )
        },
        containerColor = Color(0xFF181D26)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // 1. Highlight Bento Cards (Dollar & Gold 18k)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // USD Dollar Card
                    usdRate?.let { rate ->
                        MarketHighlightCard(
                            title = "دلار آمریکا",
                            symbol = "USD",
                            priceToman = rate.priceToman,
                            changePercent = rate.changePercent,
                            highToman = rate.highToman,
                            lowToman = rate.lowToman,
                            iconBg = Color(0xFF2E7D32),
                            iconText = "💵",
                            currencyUnit = currencyUnit,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedRateForConverter = rate
                                showConverterModal = true
                            }
                        )
                    }

                    // Gold 18k Card
                    goldRate?.let { rate ->
                        MarketHighlightCard(
                            title = "طلا ۱۸ عیار",
                            symbol = "G18",
                            priceToman = rate.priceToman,
                            changePercent = rate.changePercent,
                            highToman = rate.highToman,
                            lowToman = rate.lowToman,
                            iconBg = Color(0xFFF57F17),
                            iconText = "🥇",
                            currencyUnit = currencyUnit,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedRateForConverter = rate
                                showConverterModal = true
                            }
                        )
                    }
                }
            }

            // 2. Emami Coin Highlight Strip
            emamiRate?.let { rate ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRateForConverter = rate
                                showConverterModal = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232A38)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3848))
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
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🪙", fontSize = 20.sp)
                                    }
                                }
                                Column {
                                    Text(rate.titleFa, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text("سکه طرح جدید (امامی)", fontSize = 11.sp, color = Color(0xFF8A9AAD))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = rate.priceToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F),
                                    fontSize = 15.sp
                                )
                                ChangePercentBadge(percent = rate.changePercent)
                            }
                        }
                    }
                }
            }

            // 3. Search and Category Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجوی دلار، طلا، سکه، یورو...", color = Color(0xFF8A9AAD), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8A9AAD)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = Color.White)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E2638),
                            unfocusedContainerColor = Color(0xFF1E2638),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B6FE8),
                            unfocusedBorderColor = Color(0xFF2E3848)
                        )
                    )

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("همه موارد", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B6FE8),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF232A38),
                                    labelColor = Color(0xFF8A9AAD)
                                )
                            )
                        }

                        items(MarketCategory.entries) { category ->
                            FilterChip(
                                selected = selectedCategoryFilter == category,
                                onClick = { selectedCategoryFilter = category },
                                label = { Text(category.titleFa, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B6FE8),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF232A38),
                                    labelColor = Color(0xFF8A9AAD)
                                )
                            )
                        }
                    }
                }
            }

            // 4. Rate Items List
            items(filteredRates, key = { it.id }) { rate ->
                MarketRateRowItem(
                    rate = rate,
                    currencyUnit = currencyUnit,
                    onConvert = {
                        selectedRateForConverter = rate
                        showConverterModal = true
                    },
                    onAddToPortfolio = {
                        showAddToPortfolioDialog = rate
                    }
                )
            }
        }
    }

    // Modal 1: Live Calculator / Converter
    selectedRateForConverter?.let { rate ->
        if (showConverterModal) {
            LiveRateConverterDialog(
                rate = rate,
                currencyUnit = currencyUnit,
                onDismiss = {
                    showConverterModal = false
                    selectedRateForConverter = null
                }
            )
        }
    }

    // Modal 2: Add Market Asset to Portfolio
    showAddToPortfolioDialog?.let { rate ->
        AddMarketAssetDialog(
            rate = rate,
            onDismiss = { showAddToPortfolioDialog = null },
            onConfirm = { name, type, qty, unitPrice ->
                viewModel.addAsset(name, type, qty, unitPrice, rate.unit)
                showAddToPortfolioDialog = null
            }
        )
    }
}

@Composable
fun MarketHighlightCard(
    title: String,
    symbol: String,
    priceToman: Long,
    changePercent: Double,
    highToman: Long,
    lowToman: Long,
    iconBg: Color,
    iconText: String,
    currencyUnit: CurrencyUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232A38)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3848))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBg.copy(alpha = 0.25f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(iconText, fontSize = 18.sp)
                    }
                }
                ChangePercentBadge(percent = changePercent)
            }

            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(symbol, fontSize = 10.sp, color = Color(0xFF8A9AAD))
            }

            Text(
                text = priceToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("کف: ${lowToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان"}", fontSize = 10.sp, color = Color(0xFF8A9AAD))
                Text("سقف: ${highToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان"}", fontSize = 10.sp, color = Color(0xFF8A9AAD))
            }
        }
    }
}

@Composable
fun MarketRateRowItem(
    rate: MarketRate,
    currencyUnit: CurrencyUnit,
    onConvert: () -> Unit,
    onAddToPortfolio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConvert),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232A38)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3848))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = getCategoryBgColor(rate.category),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(getCategoryIconText(rate.id), fontSize = 18.sp)
                    }
                }

                Column {
                    Text(rate.titleFa, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(rate.symbol, fontSize = 11.sp, color = Color(0xFF8A9AAD))
                        Text("• ${rate.unit}", fontSize = 11.sp, color = Color(0xFF6C7B8E))
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = rate.priceToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    ChangePercentBadge(percent = rate.changePercent)
                }

                IconButton(
                    onClick = onAddToPortfolio,
                    modifier = Modifier.background(Color(0xFF1E2638), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCard,
                        contentDescription = "افزودن به دارایی",
                        tint = Color(0xFF90CAF9),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChangePercentBadge(percent: Double) {
    val isPositive = percent >= 0
    val bgColor = if (isPositive) Color(0xFF2E7D32).copy(alpha = 0.25f) else Color(0xFFC62828).copy(alpha = 0.25f)
    val textColor = if (isPositive) Color(0xFF81C784) else Color(0xFFEF9A9A)
    val sign = if (isPositive) "▲ +" else "▼ "

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = "$sign$percent%",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LiveRateConverterDialog(
    rate: MarketRate,
    currencyUnit: CurrencyUnit,
    onDismiss: () -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }

    val qty = quantityText.toDoubleOrNull() ?: 0.0
    val totalToman = (qty * rate.priceToman).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222834),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("محاسبه‌گر نرخ زنده ${rate.titleFa}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181D26), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نرخ پایه زنده:", color = Color(0xFF8A9AAD), fontSize = 12.sp)
                    Text(
                        text = rate.priceToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90CAF9)
                    )
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("تعداد / مقدار (${rate.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF181D26),
                        unfocusedContainerColor = Color(0xFF181D26),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B6FE8)
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3848)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("مبلغ کل معادل:", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                        Text(
                            text = totalToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B6FE8))
            ) {
                Text("متوجه شدم", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AddMarketAssetDialog(
    rate: MarketRate,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AssetType, quantity: Double, unitPrice: Long) -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222834),
        title = {
            Text("افزودن ${rate.titleFa} به سبد دارایی", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "قیمت زنده بازار (${rate.priceToman} تومان) به عنوان قیمت پایه ثبت می‌شود.",
                    fontSize = 12.sp,
                    color = Color(0xFF8A9AAD)
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("مقدار یا تعداد (${rate.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF181D26),
                        unfocusedContainerColor = Color(0xFF181D26),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B6FE8)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 1.0
                    val assetType = when (rate.category) {
                        MarketCategory.GOLD_COIN -> AssetType.GOLD_18K
                        MarketCategory.CURRENCY -> AssetType.CURRENCY_USD
                        MarketCategory.CRYPTO -> AssetType.CRYPTO_USDT
                    }
                    onConfirm(rate.titleFa, assetType, qty, rate.priceToman)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B6FE8))
            ) {
                Text("افزودن به دارایی‌ها", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = Color(0xFF8A9AAD)) }
        }
    )
}

private fun getCategoryBgColor(category: MarketCategory): Color {
    return when (category) {
        MarketCategory.GOLD_COIN -> Color(0xFFFFD54F).copy(alpha = 0.2f)
        MarketCategory.CURRENCY -> Color(0xFF81C784).copy(alpha = 0.2f)
        MarketCategory.CRYPTO -> Color(0xFF64B5F6).copy(alpha = 0.2f)
    }
}

private fun getCategoryIconText(id: String): String {
    return when {
        id == "USD" -> "💵"
        id == "EUR" -> "💶"
        id == "USDT" -> "💲"
        id.contains("GOLD") -> "🥇"
        id.contains("COIN") -> "🪙"
        id == "BTC" -> "₿"
        else -> "📈"
    }
}
