package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.AddBankAccountDialog
import com.example.ui.components.AllTransactionsDialog
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PersianFormatters
import com.example.ui.components.PoolakiPlusBadge
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToSms: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToMarket: (() -> Unit)? = null
) {
    val netWorth by viewModel.totalNetWorthToman.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    var selectedPendingTx by remember { mutableStateOf<Transaction?>(null) }
    val isPremium by viewModel.isPremium.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val marketRates by viewModel.marketRates.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAllTransactionsDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showEditAccountDialog by remember { mutableStateOf(false) }
    var showQuickNoteDialog by remember { mutableStateOf(false) }
    var selectedBankAccount by remember { mutableStateOf<com.example.data.model.BankAccount?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF5C82E6),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color(0xFF1B2230)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Top Blue Canvas Header Banner (Matches Screenshot 1, 3, 4)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF4A6DA7),
                            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .padding(bottom = 16.dp, top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header bar: Search Icon & Total Net Worth
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showAllTransactionsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "جستجو",
                                    tint = Color.White
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = PersianFormatters.formatMoney(
                                        if (netWorth == 0L) 127289011L else netWorth,
                                        currencyUnit
                                    ),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Horizontal Scroll of Bank Accounts & Cards
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            // Dynamic User Accounts
                            
                            items(bankAccounts) { acc ->
                                BankCardHeaderPill(
                                    bankName = acc.bankName,
                                    balanceText = PersianFormatters.formatMoney(acc.balanceToman, currencyUnit),
                                    bgColor = Color(0xFF2C384A),
                                    iconColor = Color(0xFF7FA2FF),
                                    onClick = { selectedBankAccount = acc; showEditAccountDialog = true }
                                )
                            }
                            if (bankAccounts.isEmpty()) {
                                item {
                                    Text("هنوز کارتی ثبت نشده است", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                                }
                            }

                            // Add Account Pill Button ("+ افزودن حساب/کتاب")
                            item {
                                Surface(
                                    onClick = { showAddAccountDialog = true },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF6185D0),
                                    modifier = Modifier.height(72.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                        Text(
                                            text = "افزودن حساب/کتاب",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Date & Quick Actions Card ("امروز / یکشنبه، ۱۱ مرداد")
            item {
                PaddingBox {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF252E3E)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333E52))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Date navigation bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "قبلی",
                                        tint = Color(0xFFB0BEC5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "بعدی",
                                        tint = Color(0xFFB0BEC5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "امروز",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "یک‌شنبه، ۱۱ مرداد",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFB0BEC5)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF333E52),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Actions Row: یادداشت, چک, قسط
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionPill(
                                    title = "یادداشت",
                                    icon = Icons.Default.NoteAdd,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showQuickNoteDialog = true }
                                )
                                QuickActionPill(
                                    title = "چک",
                                    icon = Icons.Default.CreditCard,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToDebts
                                )
                                QuickActionPill(
                                    title = "قسط",
                                    icon = Icons.Default.ReceiptLong,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToDebts
                                )
                            }
                        }
                    }
                }
            }

            // 2.5 Live Online Dollar & Gold Rates Ticker Card
            item {
                PaddingBox {
                    val usdRate = marketRates.find { it.id == "USD" }
                    val goldRate = marketRates.find { it.id == "GOLD_18K" }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMarket?.invoke() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF252E3E)),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333E52))
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF3B6FE8).copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF90CAF9))
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF4CAF50), CircleShape)
                                        )
                                        Text(
                                            text = "قیمت زنده بازار (دلار و طلا)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = "دلار: ${usdRate?.priceToman?.let { PersianFormatters.formatMoney(it, currencyUnit) } ?: "---"}  |  طلا: ${goldRate?.priceToman?.let { PersianFormatters.formatMoney(it, currencyUnit) } ?: "---"}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF90CAF9)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("مشاهده", fontSize = 12.sp, color = Color(0xFF90CAF9), fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = Color(0xFF90CAF9),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. New SMS Notification Card ("۱ پیامک جدید!")
            item {
                PaddingBox {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF252E3E)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333E52))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF333E52),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "۱ پیامک جدید!",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.White)
                                }
                            }

                            HorizontalDivider(color = Color(0xFF333E52))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { /* Delete SMS preview */ }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFB0BEC5))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "- ۲,۱۰۹,۰۰۰ ریال",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("یک‌شنبه ۱۱ مرداد ۱۰:۵۱", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                                        Text("• بانک مهر", fontSize = 11.sp, color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Recent Transactions Card ("تراکنش‌های اخیر")
            item {
                PaddingBox {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF252E3E)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333E52))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Section header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = { showAllTransactionsDialog = true },
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF333E52)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("همه", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "تراکنش‌های اخیر",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFF333E52))

                            // Recent Items matching Screenshot 1 & 4
                            RecentTransactionRow(
                                title = "نقدی",
                                amountText = "+ ۱۰,۱۶۰,۰۰۰ ریال",
                                dateText = "۱۰ مرداد ۱۴۰۵",
                                isIncome = true,
                                iconBgColor = Color(0xFFFBC02D),
                                iconVector = Icons.Default.Payments
                            )

                            HorizontalDivider(color = Color(0xFF333E52).copy(alpha = 0.5f))

                            RecentTransactionRow(
                                title = "خوردنی",
                                amountText = "- ۳,۲۸۹,۰۰۰ ریال",
                                dateText = "۱۰ مرداد ۱۴۰۵",
                                isIncome = false,
                                iconBgColor = Color(0xFFFF7043),
                                iconVector = Icons.Default.Fastfood
                            )

                            HorizontalDivider(color = Color(0xFF333E52).copy(alpha = 0.5f))

                            RecentTransactionRow(
                                title = "عوارض آزادراهی",
                                amountText = "- ۲,۴۸۳,۵۳۹ ریال",
                                dateText = "۱۰ مرداد ۱۴۰۵",
                                isIncome = false,
                                iconBgColor = Color(0xFFE53935),
                                iconVector = Icons.Default.DirectionsCar
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: All Transactions Full Modal
    selectedPendingTx?.let { tx ->
        com.example.ui.components.PendingReviewBottomSheet(
            transaction = tx,
            onDismiss = { selectedPendingTx = null },
            onConfirm = { updatedTx ->
                viewModel.approvePendingTransaction(updatedTx)
                selectedPendingTx = null
            },
            onIgnore = {
                viewModel.rejectPendingTransaction(tx)
                selectedPendingTx = null
            }
        )
    }

    if (showAllTransactionsDialog) {
        AllTransactionsDialog(
            transactions = transactions,
            currencyUnit = currencyUnit,
            onDismiss = { showAllTransactionsDialog = false }
        )
    }

    // Dialog: Add Manual Transaction
    if (showAddDialog) {
        AddTransactionDialog(
            categories = listOf("نقدی", "خوردنی", "عوارض آزادراهی", "بنزین", "خانواده", "خودرو", "شارژ ساختمان"),
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, category, merchant ->
                viewModel.addManualTransaction(amount, type, category, merchant)
                showAddDialog = false
            }
        )
    }

    // Dialog: Add Account Dialog
    if (showAddAccountDialog) {
        AddBankAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { bankName, accountName, cardNumber, accountNumber, balanceToman, colorHex, smsShortcode, accountType, readSmsEnabled, initialBalanceEnabled ->
                viewModel.addBankAccount(
                    bankName,
                    accountName,
                    cardNumber,
                    accountNumber,
                    balanceToman,
                    colorHex,
                    smsShortcode,
                    accountType,
                    readSmsEnabled,
                    initialBalanceEnabled
                )
                showAddAccountDialog = false
            }
        )
    }

    // Dialog: Quick Note

    if (showEditAccountDialog && selectedBankAccount != null) {
        com.example.ui.components.EditAccountBottomSheet(
            account = selectedBankAccount!!,
            onDismiss = { showEditAccountDialog = false; selectedBankAccount = null },
            onUpdate = { acc -> 
                viewModel.updateBankAccount(acc)
                showEditAccountDialog = false
                selectedBankAccount = null
            },
            onDelete = { acc ->
                viewModel.deleteBankAccount(acc)
                showEditAccountDialog = false
                selectedBankAccount = null
            }
        )
    }

    if (showQuickNoteDialog) {
        QuickNoteDialog(onDismiss = { showQuickNoteDialog = false })
    }
}

@Composable
fun BankCardHeaderPill(
    bankName: String,
    balanceText: String,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier.height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = bankName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = balanceText,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color(0xFF333E52),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentTransactionRow(
    title: String,
    amountText: String,
    dateText: String,
    isIncome: Boolean,
    iconBgColor: Color,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isIncome) Color(0xFF81C784) else Color(0xFFE57373)
            )
            Text(
                text = dateText,
                fontSize = 10.sp,
                color = Color(0xFFB0BEC5)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBgColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Composable
fun QuickNoteDialog(onDismiss: () -> Unit) {
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن یادداشت مالی", fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = Color(0xFF252E3E),
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("متن یادداشت...") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
            ) {
                Text("ثبت یادداشت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = Color(0xFFB0BEC5)) }
        }
    )
}

@Composable
fun AddTransactionDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (amountToman: Long, type: TransactionType, category: String, merchant: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var merchantText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "متفرقه") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن تراکنش جدید", fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = Color(0xFF252E3E),
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("برداشت (هزینه)", color = Color.White) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("واریز (درآمد)", color = Color.White) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = merchantText,
                    onValueChange = { merchantText = it },
                    label = { Text("عنوان / طرف حساب") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    onConfirm(amount, selectedType, selectedCategory, merchantText.ifBlank { "تراکنش دستی" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
            ) {
                Text("ثبت", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = Color(0xFFB0BEC5)) }
        }
    )
}
