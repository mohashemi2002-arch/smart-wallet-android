package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BankAccount
import com.example.data.model.MarketRate
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.AddBankAccountDialog
import com.example.ui.components.BankLogoImage
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.EditAccountBottomSheet
import com.example.ui.components.PersianCurrencyFormatter
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.BankLogoHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel = viewModel(),
    onNavigateToSms: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToPortfolio: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToMarket: () -> Unit = {},
    onNavigateToRegistration: () -> Unit = {}
) {
    val accounts by viewModel.bankAccounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val totalBalance by viewModel.totalNetWorthToman.collectAsState()
    val monthlyIncome by viewModel.totalIncomeToman.collectAsState()
    val monthlyExpense by viewModel.totalExpenseToman.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val marketRates by viewModel.marketRates.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()

    var accountToEdit by remember { mutableStateOf<BankAccount?>(null) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    if (accountToEdit != null) {
        EditAccountBottomSheet(
            account = accountToEdit!!,
            onDismiss = { accountToEdit = null },
            onUpdate = { updatedAcc ->
                viewModel.updateBankAccount(updatedAcc)
                accountToEdit = null
            },
            onDelete = { accToDelete ->
                viewModel.deleteBankAccount(accToDelete)
                accountToEdit = null
            }
        )
    }

    if (showAddAccountDialog) {
        AddBankAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { bankName, accountName, cardNumber, accountNumber, balanceToman, colorHex, smsShortcode, accountType, readSmsEnabled, initialBalanceEnabled ->
                viewModel.addBankAccount(
                    bankName = bankName,
                    accountName = accountName,
                    cardNumber = cardNumber,
                    accountNumber = accountNumber,
                    balanceToman = balanceToman,
                    cardColorHex = colorHex,
                    smsShortcode = smsShortcode,
                    accountType = accountType,
                    readSmsEnabled = readSmsEnabled,
                    initialBalanceEnabled = initialBalanceEnabled
                )
                showAddAccountDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopBarDashboard(
                currencyUnit = currencyUnit,
                onCurrencyChange = { viewModel.setCurrencyUnit(it) }
            )
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
                        text = "موجودی کل دارایی",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Text(
                        text = formatWithUnit(totalBalance.valueOrZero(), currencyUnit),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "درآمد ماهانه: ${formatWithUnit(monthlyIncome.valueOrZero(), currencyUnit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "هزینه ماهانه: ${formatWithUnit(monthlyExpense.valueOrZero(), currencyUnit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "درآمد",
                    value = formatWithUnit(monthlyIncome.valueOrZero(), currencyUnit),
                    subtitle = "+۱۲٪",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "هزینه",
                    value = formatWithUnit(monthlyExpense.valueOrZero(), currencyUnit),
                    subtitle = "-۵٪",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            BankAccountsSection(
                accounts = accounts,
                currencyUnit = currencyUnit,
                onAccountClick = { accountToEdit = it },
                onAddAccount = { showAddAccountDialog = true }
            )
        }

        if (pendingTransactions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToSms),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3F1D38)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE25574).copy(alpha = 0.6f))
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color(0xFFF2B8B5),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Sms,
                                        contentDescription = null,
                                        tint = Color(0xFF601410),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "پیامک‌های تراکنش نیازمند تایید",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD8E4),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${pendingTransactions.size} پیامک بانکی جدید نیازمند بررسی و تایید هستند",
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFFFD8E4)
                        )
                    }
                }
            }
        }

        item {
            QuickActionsSection(
                onNavigateToSms = onNavigateToSms,
                onNavigateToBudget = onNavigateToBudget,
                onNavigateToPortfolio = onNavigateToPortfolio,
                onNavigateToDebts = onNavigateToDebts
            )
        }

        item {
            MarketOverviewSection(
                rates = marketRates,
                currencyUnit = currencyUnit,
                onClick = onNavigateToMarket
            )
        }

        item {
            RecentTransactionsSection(
                transactions = transactions,
                currencyUnit = currencyUnit
            )
        }

        item {
            PendingSmsSection(onNavigateToSms = onNavigateToSms)
        }
    }
}

@Composable
private fun TopBarDashboard(
    currencyUnit: CurrencyUnit,
    onCurrencyChange: (CurrencyUnit) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "سلام، کاربر گرامی",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "خلاصه وضعیت مالی",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = currencyUnit == CurrencyUnit.TOMAN,
                onClick = { onCurrencyChange(CurrencyUnit.TOMAN) },
                label = { Text("تومان", style = MaterialTheme.typography.labelSmall) },
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = currencyUnit == CurrencyUnit.RIAL,
                onClick = { onCurrencyChange(CurrencyUnit.RIAL) },
                label = { Text("ریال", style = MaterialTheme.typography.labelSmall) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = tint.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun BankAccountsSection(
    accounts: List<BankAccount>,
    currencyUnit: CurrencyUnit,
    onAccountClick: (BankAccount) -> Unit,
    onAddAccount: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "حساب‌ها و کارت‌های بانکی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onAddAccount,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("افزودن کارت", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = onAddAccount,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("هنوز کارتی ثبت نشده، برای افزودن کلیک کنید +")
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(accounts) { account ->
                        val brandColor = BankLogoHelper.getBrandColor(account.bankName, account.cardNumber)
                        Card(
                            modifier = Modifier
                                .width(270.dp)
                                .clickable { onAccountClick(account) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                brandColor.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Right Side (in RTL start): Account details & balance
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = account.bankName.ifBlank { account.accountName },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (account.accountName.isNotBlank() && account.accountName != account.bankName) {
                                        Text(
                                            text = account.accountName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    val accNum = account.accountNumber?.trim()
                                    val displayAccText = if (!accNum.isNullOrBlank()) {
                                        "شماره حساب: ${PersianCurrencyFormatter.toPersianDigits(accNum)}"
                                    } else {
                                        "شماره حساب: ثبت نشده"
                                    }
                                    Text(
                                        text = displayAccText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "مانده حساب:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatWithUnit(account.balanceToman, currencyUnit),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Left Side (in RTL end): Bank Logo
                                BankLogoImage(
                                    bankName = account.bankName,
                                    cardNumber = account.cardNumber,
                                    containerSize = 52.dp,
                                    logoSize = 42.dp,
                                    containerColor = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToSms: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToDebts: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            Triple("پیامک بانکی", Icons.Default.Message, onNavigateToSms),
            Triple("بودجه", Icons.Default.Savings, onNavigateToBudget),
            Triple("دارایی", Icons.Default.AccountBalanceWallet, onNavigateToPortfolio),
            Triple("سررسید", Icons.Default.CalendarMonth, onNavigateToDebts)
        ).forEach { (label, icon, onClick) ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketOverviewSection(
    rates: List<MarketRate>,
    currencyUnit: CurrencyUnit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "نرخ‌های بازار",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(قیمت لحظه‌ای)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "مشاهده بازار آنلاین",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            val displayRates = if (rates.isNotEmpty()) {
                // Prioritize key items: US Dollar, Gold 18k, Coin, or first 3
                val usd = rates.firstOrNull { it.symbol.contains("USD", ignoreCase = true) || it.titleFa.contains("دلار") }
                val gold = rates.firstOrNull { it.id.contains("gold", ignoreCase = true) || it.titleFa.contains("طلا") }
                val coin = rates.firstOrNull { it.id.contains("coin", ignoreCase = true) || it.titleFa.contains("سکه") }
                val selected = listOfNotNull(usd, gold, coin).distinct()
                if (selected.size >= 3) selected.take(3) else rates.take(3)
            } else emptyList()

            if (displayRates.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    displayRates.forEach { rate ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = rate.titleFa,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatWithUnit(rate.priceToman, currencyUnit),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val isPositive = rate.changePercent >= 0
                                val changeText = "${if (isPositive) "+" else ""}${rate.changePercent}%"
                                Text(
                                    text = PersianCurrencyFormatter.toPersianDigits(changeText),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "در حال بارگذاری نرخ‌های آنلاین...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<Transaction>,
    currencyUnit: CurrencyUnit
) {
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
            Text(
                text = "تراکنش‌های اخیر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (transactions.isEmpty()) {
                Text(
                    text = "تراکنشی برای این دوره ثبت نشده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                transactions.take(5).forEach { transaction ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.merchantName.ifBlank { transaction.categoryName },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatWithUnit(transaction.amountToman, currencyUnit),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingSmsSection(onNavigateToSms: () -> Unit) {
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
            Text(
                text = "پیامک‌های در انتظار",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "پیامک‌های بانکی دریافتی را بررسی کنید",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = onNavigateToSms,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("مشاهده")
                }
            }
        }
    }
}

private fun formatMoney(value: Long, usePersianDigits: Boolean = true): String {
    val formatter = java.text.DecimalFormat("#,###")
    val formatted = formatter.format(value).replace(",", "،")
    return if (usePersianDigits) PersianCurrencyFormatter.toPersianDigits(formatted) else formatted
}

private fun formatWithUnit(amountToman: Long, unit: CurrencyUnit): String {
    val amount = if (unit == CurrencyUnit.RIAL) amountToman * 10L else amountToman
    val suffix = if (unit == CurrencyUnit.RIAL) "ریال" else "تومان"
    return "${formatMoney(amount)} $suffix"
}

private fun Long.valueOrZero(): Long = this

