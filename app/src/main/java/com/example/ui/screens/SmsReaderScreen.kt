package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceRepository
import com.example.data.model.BankAccount
import com.example.data.model.BankSmsSender
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

private val ColorIncomeGreen = Color(0xFF10B981)
private val ColorExpenseRed = Color(0xFFEF4444)
private val ColorIncomeSoft = Color(0xFFE6F7F0)
private val ColorExpenseSoft = Color(0xFFFEECEC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReaderScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToFilteredSms: (String?) -> Unit = {},
    onNavigateToGroupedSms: (String?) -> Unit = {},
    onNavigateToBankLinker: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { FinanceRepository.getInstance(context) }

    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val bankSmsSenders by viewModel.bankSmsSenders.collectAsState()
    val isParsing by viewModel.isParsingSms.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()

    val dbCategories by repository.allCategories.collectAsState(initial = emptyList())
    val confirmedTransactions by repository.confirmedTransactions.collectAsState(initial = emptyList())

    val categoryNames = remember(dbCategories) {
        val names = dbCategories.map { it.name }.filter { it.isNotBlank() }
        if (names.isNotEmpty()) names else listOf(
            "خوراکی و سوپرمارکت", "حمل و نقل", "مسکن و اجاره", "قبوض و خدمات",
            "سلامت و درمان", "پوشاک و خرید", "تفریح و سرگرمی", "درآمد و حقوق", "متفرقه"
        )
    }

    var showClearAllDialog by remember { mutableStateOf(false) }
    var showClearPendingDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddSenderDialog by remember { mutableStateOf(false) }
    var showSmsPickerBottomSheet by remember { mutableStateOf(false) }
    var editingSenderForBank by remember { mutableStateOf<BankSmsSender?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.READ_SMS] == true
        if (granted) {
            viewModel.scanRecentSmsFromSenders()
        } else {
            Toast.makeText(context, "دسترسی پیامک برای خوانش پیامک‌های بانکی الزامی است", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog: Clear All Transactions Confirmation
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = ColorExpenseRed)
                    Text("پاکسازی تمام تراکنش‌ها", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "با تایید این بخش، تمام تراکنش‌های ثبت‌شده و پیامک‌های قبلی پاکسازی می‌شوند تا بتوانید از ابتدا، پیامک‌های واقعی بانکی را اسکن و دوباره دسته‌بندی و تایید کنید.\n\nآیا از پاکسازی کامل اطمینان دارید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllTransactions()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorExpenseRed)
                ) {
                    Text("بله، پاکسازی کامل")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearAllDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Dialog: Clear Pending Transactions Confirmation
    if (showClearPendingDialog) {
        AlertDialog(
            onDismissRequest = { showClearPendingDialog = false },
            title = { Text("حذف موارد در انتظار", fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف تمام تراکنش‌های در انتظار تایید اطمینان دارید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearPendingTransactions()
                        showClearPendingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorExpenseRed)
                ) {
                    Text("حذف موارد")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearPendingDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "پیامک‌های بانکی و تایید تراکنش‌ها",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "اسکن، تایید و دسته‌بندی تراکنش‌های بانکی",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    // Clear All Transactions Button
                    IconButton(
                        onClick = { showClearAllDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "پاکسازی تمام تراکنش‌ها",
                            tint = ColorExpenseRed
                        )
                    }
                    // Bank Linker
                    IconButton(onClick = { onNavigateToBankLinker() }) {
                        Icon(
                            imageVector = Icons.Default.AddLink,
                            contentDescription = "اتصال سرشماره‌ها به بانک‌ها",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Bank SMS Smart Scanner Hero Card
            item {
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
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ColorIncomeGreen,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Text(
                                text = "فیلتر هوشمند و استخراج پیامک‌های بانکی",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "تنها پیامک‌های دارای مانده‌حساب و کلمات واریز/برداشت از بانک‌های معتبر شناسایی می‌شوند. پیامک‌های شخصی، تبلیغات، اپراتورها (ایرانسل/همراه اول) و رمز پویا به طور خودکار حذف می‌گردند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val hasRead = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.READ_SMS
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                    if (hasRead) {
                                        viewModel.scanRecentSmsFromSenders()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.READ_SMS,
                                                android.Manifest.permission.RECEIVE_SMS
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_bank_sms_button"),
                                enabled = !isParsing,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (isParsing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("در حال اسکن...")
                                } else {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("اسکن پیامک‌های بانکی جدید", fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { showClearAllDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ColorExpenseRed.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = ColorExpenseRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("پاکسازی کل", color = ColorExpenseRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Pending Approval & Categorization Workbench
            item {
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
                            text = "تراکنش‌های در انتظار بررسی و تایید",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (pendingTransactions.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${PersianFormatters.toPersianDigits(pendingTransactions.size.toString())} مورد",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = if (pendingTransactions.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (pendingTransactions.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { viewModel.approveAllPendingTransactions() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = ColorIncomeGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("تایید همه", color = ColorIncomeGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            TextButton(
                                onClick = { showClearPendingDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("حذف همه", color = ColorExpenseRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            if (pendingTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ColorIncomeGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "هیچ تراکنشی در انتظار تایید نیست",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "برای دریافت و دسته‌بندی پیامک‌های جدید، دکمه «اسکن پیامک‌های بانکی جدید» را بزنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = pendingTransactions,
                    key = { it.id }
                ) { pending ->
                    PendingTransactionReviewCard(
                        transaction = pending,
                        availableCategories = categoryNames,
                        onApprove = { chosenCategory ->
                            viewModel.approvePendingTransactionWithCategory(pending, chosenCategory)
                        },
                        onReject = {
                            viewModel.rejectPendingTransaction(pending)
                        }
                    )
                }
            }

            // 3. Bank Cards & Accounts Management Carousel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
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
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "کارت‌ها و حساب‌های بانکی شما",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            FilledTonalButton(
                                onClick = { showAddAccountDialog = true },
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("افزودن کارت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (bankAccounts.isEmpty()) {
                            Text(
                                text = "هنوز حسابی اضافه نکرده‌اید. با ثبت اولین کارت یا اسکن پیامک‌ها، کارت‌ها به طور خودکار ثبت می‌شوند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(bankAccounts) { account ->
                                    BankCardItem(
                                        account = account,
                                        currencyUnit = currencyUnit,
                                        onDelete = { viewModel.deleteBankAccount(account) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Active Bank SMS Senders Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "سرشماره‌های بانکی فعال",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            FilledTonalButton(
                                onClick = { showSmsPickerBottomSheet = true },
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("افزودن سرشماره", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (bankSmsSenders.isEmpty()) {
                            Text(
                                text = "سرشماره‌ای ثبت نشده است. می‌توانید با دکمه بالا سرشماره‌های بانک‌های خود را معرفی نمایید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            bankSmsSenders.forEach { sender ->
                                BankSmsSenderRow(
                                    sender = sender,
                                    onToggleActive = { viewModel.toggleBankSmsSenderActive(sender) },
                                    onDelete = { viewModel.deleteBankSmsSender(sender) },
                                    onEditBank = { editingSenderForBank = sender }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add New Bank Account
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

    // BottomSheet: Select Bank Sender from Device Inbox
    if (showSmsPickerBottomSheet) {
        SmsSenderPickerSheet(
            onDismiss = { showSmsPickerBottomSheet = false },
            onSenderSelected = { /* handled below */ },
            onSenderAndBankSelected = { address, detectedBank ->
                viewModel.addBankSmsSender(detectedBank, address)
                showSmsPickerBottomSheet = false
            }
        )
    }



    // Dialog: Edit Sender Bank Mapping
    editingSenderForBank?.let { sender ->
        SelectBankForSenderDialog(
            senderNumber = sender.senderNumber,
            initialBankName = sender.bankName,
            onDismiss = { editingSenderForBank = null },
            onConfirm = { chosenBankName ->
                coroutineScope.launch {
                    repository.bankSmsSenderDao.update(
                        sender.copy(bankName = chosenBankName)
                    )
                    Toast.makeText(
                        context,
                        "سرشماره ${sender.senderNumber} به $chosenBankName متصل شد",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                editingSenderForBank = null
            }
        )
    }
}

/**
 * Card for reviewing and categorizing a single pending bank transaction.
 */
@Composable
private fun PendingTransactionReviewCard(
    transaction: Transaction,
    availableCategories: List<String>,
    onApprove: (String) -> Unit,
    onReject: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME
    var selectedCategory by remember(transaction.id, transaction.categoryName) {
        mutableStateOf(transaction.categoryName.ifBlank { "خوراکی و سوپرمارکت" })
    }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var isExpandedSms by remember { mutableStateOf(false) }

    val (catColor, catIcon) = CategoryIconMap.getIconForCategory(selectedCategory)
    val bankTitle = transaction.bankName?.ifBlank { "بانک" } ?: "بانک"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Bank info & Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BankLogoImage(
                        bankName = bankTitle,
                        cardNumber = transaction.cardNumber,
                        containerSize = 36.dp,
                        logoSize = 28.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Column {
                        Text(
                            text = bankTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!transaction.cardNumber.isNullOrBlank()) {
                            Text(
                                text = transaction.cardNumber,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Type Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isIncome) ColorIncomeSoft else ColorExpenseSoft
                ) {
                    Text(
                        text = if (isIncome) "واریز" else "برداشت / خرید",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) ColorIncomeGreen else ColorExpenseRed
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Amount and Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "مبلغ تراکنش:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (isIncome) "+" else "-"}${transaction.amountToman.formatAsToman()} تومان",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isIncome) ColorIncomeGreen else ColorExpenseRed
                        )
                    )
                }

                if (transaction.balanceAfterToman != null && transaction.balanceAfterToman > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "مانده بعد از تراکنش:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${transaction.balanceAfterToman.formatAsToman()} تومان",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Category Selection Row (قابلیت دسته‌بندی)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "دسته‌بندی تراکنش (قابل تغییر):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Category Carousel / Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCategories) { catName ->
                        val isSelected = catName == selectedCategory
                        val (cColor, cIcon) = CategoryIconMap.getIconForCategory(catName)

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = catName },
                            label = { Text(catName, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else cColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Raw SMS text accordion
            if (!transaction.rawSmsText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpandedSms = !isExpandedSms }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "متن پیامک بانکی",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = if (isExpandedSms) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isExpandedSms) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = transaction.rawSmsText ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Actions: Confirm vs Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onApprove(selectedCategory) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorIncomeGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("تایید و ثبت در حساب", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorExpenseRed),
                    border = BorderStroke(1.dp, ColorExpenseRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("رد / حذف")
                }
            }
        }
    }
}

@Composable
fun BankSmsSenderRow(
    sender: BankSmsSender,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onEditBank: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEditBank() }
            ) {
                BankLogoImage(
                    bankName = sender.bankName,
                    cardNumber = null,
                    containerSize = 36.dp,
                    logoSize = 28.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                )

                Column {
                    Text(
                        text = sender.bankName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = sender.senderNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = sender.isActive,
                    onCheckedChange = { onToggleActive() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف سرشماره",
                        tint = ColorExpenseRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
