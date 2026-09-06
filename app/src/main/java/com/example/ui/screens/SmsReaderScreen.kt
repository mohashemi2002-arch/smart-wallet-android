package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccount
import com.example.data.model.BankSmsSender
import com.example.data.model.TransactionType
import com.example.ui.components.AddBankAccountDialog
import com.example.ui.components.BankCardItem
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PersianFormatters
import com.example.ui.components.SmartWalletPlusBadge
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReaderScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val smsInputText by viewModel.smsInputText.collectAsState()
    val parsedResult by viewModel.parsedSmsState.collectAsState()
    val isParsing by viewModel.isParsingSms.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val bankSmsSenders by viewModel.bankSmsSenders.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddSenderDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.scanRecentSmsFromSenders()
        }
    }

    val sampleSmsList = remember {
        listOf(
            "برداشت مبلغ 1,250,000 ریال از حساب 6037****8910 - خرید داروخانه دکتر رضایی - بانک ملی",
            "خرید اسنپ مبلغ 85,000 تومان - کارت 6104****4321 - بانک ملت",
            "واریز مبلغ 150,000,000 ریال به حساب 6221****1111 - واریز حقوق - بانک پارسیان",
            "برداشت مبلغ 3,200,000 ریال بابت خرید دیجی‌کالا - بلوبانک",
            "برداشت 4,500,000 ریال از حساب 5022****9988 - هایپرمارکت افق - بانک پاسارگاد"
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پردازش اس‌ام‌اس و حساب‌های بانکی", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Color(0xFFE6E1E5))
                    }
                },
                actions = {
                    if (isPremium) SmartWalletPlusBadge(Modifier.padding(end = 8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1B1F))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // 1. Android Background Listener Status Bento Card
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFB1E5D3),
                                modifier = Modifier.size(12.dp)
                            ) {}
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "شنود خودکار پیامک‌های بانکی فعال است",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE6E1E5)
                                )
                                Text(
                                    text = "پیامک‌های دریافتی از سرشماره‌های معرفی‌شده با BroadcastReceiver استخراج و با عبارات کلیدی (آفلاین) پردازش شده و جهت تایید در اینجا نمایش داده می‌شوند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCAC4D0)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val hasReadSms = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.READ_SMS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                val hasReceiveSms = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.RECEIVE_SMS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (hasReadSms && hasReceiveSms) {
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
                                .fillMaxWidth()
                                .testTag("scan_recent_sms_button"),
                            enabled = !isParsing,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            )
                        ) {
                            if (isParsing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF381E72), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("در حال استعلام پیامک‌های بانکی...")
                            } else {
                                Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF381E72))
                                Spacer(Modifier.width(8.dp))
                                Text("خوانش اس‌ام‌اس‌های جدید از سرشماره‌های فعال", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1.5 Pending Transactions Section
            if (pendingTransactions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F1D38)), // A subtle alert color
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "تراکنش‌های نیازمند تایید (${pendingTransactions.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFD8E4)
                            )
                            
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(pendingTransactions) { pending ->
                                    val isExpense = pending.type == TransactionType.EXPENSE
                                    Card(
                                        modifier = Modifier.width(300.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A4458)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = if (isExpense) "برداشت" else "واریز", color = if (isExpense) Color(0xFFF2B8B5) else Color(0xFFB1E5D3), fontWeight = FontWeight.Bold)
                                                Text(text = PersianFormatters.formatMoney(pending.amountToman, currencyUnit), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text(text = "فروشگاه: ${pending.merchantName}", color = Color(0xFFCAC4D0), fontSize = 12.sp)
                                            Text(text = "کارت: ${pending.cardNumber ?: "نامشخص"}", color = Color(0xFFCAC4D0), fontSize = 12.sp)
                                            
                                            Spacer(Modifier.height(12.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { viewModel.approvePendingTransaction(pending) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB1E5D3), contentColor = Color(0xFF0F5223))
                                                ) {
                                                    Text("تایید")
                                                }
                                                OutlinedButton(
                                                    onClick = { viewModel.rejectPendingTransaction(pending) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF2B8B5))
                                                ) {
                                                    Text("رد")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Bank Cards & Accounts Management Section
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
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFFD0BCFF))
                                Text(
                                    text = "کارت‌ها و حساب‌های بانکی شما",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE6E1E5)
                                )
                            }

                            FilledTonalButton(
                                onClick = { showAddAccountDialog = true },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("add_bank_card_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("افزودن کارت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (bankAccounts.isEmpty()) {
                            Text(
                                text = "هنوز هیچ کارت یا حساب بانکی ثبت نشده است.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCAC4D0)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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

            // 3. Bank SMS Sender Numbers Configuration Section
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
                                Icon(Icons.Default.PermPhoneMsg, contentDescription = null, tint = Color(0xFFD0BCFF))
                                Text(
                                    text = "سرشماره‌های اس‌ام‌اس بانکی",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE6E1E5)
                                )
                            }

                            OutlinedButton(
                                onClick = { showAddSenderDialog = true },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("add_sms_sender_button"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("سرشماره جدید", fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = "پیامک‌های دریافتی فقط از این سرشماره‌ها آنالیز و استخراج می‌شوند:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )

                        bankSmsSenders.forEach { sender ->
                            BankSmsSenderRow(
                                sender = sender,
                                onToggleActive = { viewModel.toggleBankSmsSenderActive(sender) },
                                onDelete = { viewModel.deleteBankSmsSender(sender) }
                            )
                        }
                    }
                }
            }

            // 4. Interactive SMS Test Playground Section
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "تست و بررسی دستی متن پیامک بانکی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE6E1E5)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sampleSmsList) { sample ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.setSmsInputText(sample)
                                        viewModel.parseSmsInput()
                                    },
                                    label = {
                                        Text(
                                            text = sample.take(28) + "...",
                                            fontSize = 11.sp,
                                            color = Color(0xFFD0BCFF)
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF36343B)),
                                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = Color(0xFF49454F)),
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = smsInputText,
                            onValueChange = { viewModel.setSmsInputText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("sms_input_field"),
                            placeholder = { Text("متن پیامک بانکی را اینجا وارد نمایید...", color = Color(0xFF938F99)) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF36343B),
                                unfocusedContainerColor = Color(0xFF36343B),
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedTextColor = Color(0xFFE6E1E5),
                                unfocusedTextColor = Color(0xFFE6E1E5)
                            )
                        )

                        Button(
                            onClick = { viewModel.parseSmsInput() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parse_sms_button"),
                            enabled = smsInputText.isNotBlank() && !isParsing,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            )
                        ) {
                            if (isParsing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF381E72), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("در حال آنالیز Gemini AI...", color = Color(0xFF381E72))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF381E72))
                                Spacer(Modifier.width(8.dp))
                                Text("آنالیز هوشمند متن", fontWeight = FontWeight.Bold, color = Color(0xFF381E72))
                            }
                        }
                    }
                }
            }

            // 5. Parsed Output Card
            item {
                AnimatedVisibility(
                    visible = parsedResult != null,
                    enter = fadeIn() + expandVertically()
                ) {
                    parsedResult?.let { result ->
                        val isExpense = result.type == TransactionType.EXPENSE
                        val typeColor = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parsed_sms_result_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
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
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text(text = "نتیجه استخراج شده", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (result.parsedByAi) Color(0xFF004D40) else Color(0xFFE65100)
                                    ) {
                                        Text(
                                            text = if (result.parsedByAi) "Gemini AI" else "Local Regex",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("مبلغ:", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = PersianFormatters.formatMoney(result.amountToman, currencyUnit),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = typeColor
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("نوع:", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = if (isExpense) "برداشت (هزینه)" else "واریز (درآمد)",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = typeColor
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("فروشگاه / طرف حساب:", style = MaterialTheme.typography.bodySmall)
                                        Text(result.merchantName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("دسته‌بندی:", style = MaterialTheme.typography.bodySmall)
                                        Text(result.categoryName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                Button(
                                    onClick = { viewModel.confirmParsedSmsTransaction() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("confirm_sms_transaction_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = typeColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("تایید و ثبت در تراکنش‌ها و بروزرسانی کارت")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add New Bank Card / Account
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

    // Dialog: Add New SMS Sender Number
    if (showAddSenderDialog) {
        AddSmsSenderDialog(
            onDismiss = { showAddSenderDialog = false },
            onConfirm = { bankName, senderNumber ->
                viewModel.addBankSmsSender(bankName, senderNumber)
                showAddSenderDialog = false
            }
        )
    }
}

@Composable
fun BankSmsSenderRow(
    sender: BankSmsSender,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF36343B)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFD0BCFF).copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                    }
                }

                Column {
                    Text(sender.bankName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE6E1E5))
                    Text("سرشماره: ${sender.senderNumber}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCAC4D0))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = sender.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF381E72), checkedTrackColor = Color(0xFFD0BCFF))
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFF2B8B5), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}



@Composable
fun AddSmsSenderDialog(
    onDismiss: () -> Unit,
    onConfirm: (bankName: String, senderNumber: String) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var senderNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعریف سرشماره اس‌ام‌اس بانکی", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
        containerColor = Color(0xFF2B2930),
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("نام بانک مربوطه (مثلاً بانک کشاورزی)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = senderNumber,
                    onValueChange = { senderNumber = it },
                    label = { Text("سرشماره اس‌ام‌اس (مثلاً 200070 یا +9820004000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bankName.isNotBlank() && senderNumber.isNotBlank()) {
                        onConfirm(bankName, senderNumber)
                    }
                },
                enabled = bankName.isNotBlank() && senderNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72))
            ) {
                Text("ثبت سرشماره", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = Color(0xFFCAC4D0)) }
        }
    )
}
