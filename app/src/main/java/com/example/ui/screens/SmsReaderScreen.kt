package com.example.ui.screens

import com.example.ui.components.formatAsToman

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
import com.example.ui.components.BankLogoImage
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PersianFormatters
import com.example.ui.components.SenderSmsItemCard
import com.example.ui.components.SmartWalletPlusBadge
import com.example.ui.components.SmsSenderPickerSheet
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.DeviceSenderSmsMessage
import com.example.util.DeviceSmsReader
import com.example.data.FinanceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReaderScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onNavigateToFilteredSms: (String?) -> Unit = {},
    onNavigateToGroupedSms: (String?) -> Unit = {}
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
    var showSmsPickerBottomSheet by remember { mutableStateOf(false) }
    var selectedSenderForSmsView by remember { mutableStateOf<BankSmsSender?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { FinanceRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val dbCategories by repository.allCategories.collectAsState(initial = emptyList())
    val registeredTransactions by repository.allTransactions.collectAsState(initial = emptyList())

    val categoryNames = remember(dbCategories) {
        val names = dbCategories.map { it.name }.filter { it.isNotBlank() }
        if (names.isNotEmpty()) names else listOf(
            "خوراکی و سوپرمارکت", "حمل و نقل", "خرید و پوشاک", "پزشکی و سلامت",
            "قبوض و ساختمان", "تفریح و سرگرمی", "درآمد و حقوق", "متفرقه"
        )
    }

    var hasReadSmsPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var deviceBankMessages by remember { mutableStateOf<List<DeviceSenderSmsMessage>>(emptyList()) }
    var isLoadingDeviceSms by remember { mutableStateOf(false) }
    var selectedBankFilter by remember { mutableStateOf("همه") }

    LaunchedEffect(hasReadSmsPermission) {
        if (hasReadSmsPermission) {
            isLoadingDeviceSms = true
            val msgs = DeviceSmsReader.getAllBankSms(context, limit = 100)
            deviceBankMessages = msgs
            isLoadingDeviceSms = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.READ_SMS] == true
        hasReadSmsPermission = granted
        val allGranted = permissions.values.all { it }
        if (allGranted || granted) {
            viewModel.scanRecentSmsFromSenders()
            coroutineScope.launch {
                isLoadingDeviceSms = true
                val msgs = DeviceSmsReader.getAllBankSms(context, limit = 100)
                deviceBankMessages = msgs
                isLoadingDeviceSms = false
            }
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
                    IconButton(onClick = { onNavigateToGroupedSms(null) }) {
                        Icon(
                            Icons.Default.ListAlt,
                            contentDescription = "لیست پیامک‌ها و گروه‌بندی بر اساس فرستنده",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(onClick = { onNavigateToFilteredSms(null) }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "فیلتر پیامک‌ها بر اساس سرشماره",
                            tint = Color(0xFF60A5FA)
                        )
                    }
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

            // 1.2 Device Bank Transaction SMS Inbox Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF232A38)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384357)),
                    shape = RoundedCornerShape(28.dp)
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
                                Icon(Icons.Default.Inbox, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
                                Column {
                                    Text(
                                        text = "صندوق پیامک‌های بانکی گوشی",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (hasReadSmsPermission) "تعداد ${deviceBankMessages.size} پیامک تراکنش شناسایی شد" else "نیازمند اعطای دسترسی پیامک",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    if (hasReadSmsPermission) {
                                        coroutineScope.launch {
                                            isLoadingDeviceSms = true
                                            val msgs = DeviceSmsReader.getAllBankSms(context, limit = 100)
                                            deviceBankMessages = msgs
                                            isLoadingDeviceSms = false
                                        }
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.READ_SMS,
                                                android.Manifest.permission.RECEIVE_SMS
                                            )
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("به‌روزرسانی", fontSize = 12.sp)
                            }
                        }

                        // Dedicated buttons for SMS screens
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToGroupedSms(null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_grouped_sms_screen_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("لیست پیامک‌ها (گروه‌بندی)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onNavigateToFilteredSms(null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_filtered_sms_screen_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("صحت‌سنجی متن خام", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (!hasReadSmsPermission) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        "برای خواندن و استخراج پیامک‌های بانکی از گوشی، لطفاً دسترسی پیامک را تایید کنید:",
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Button(
                                        onClick = {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    android.Manifest.permission.READ_SMS,
                                                    android.Manifest.permission.RECEIVE_SMS
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                                    ) {
                                        Text("اعطای دسترسی به پیامک‌ها", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else if (isLoadingDeviceSms) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF60A5FA), strokeWidth = 2.dp)
                                    Text("در حال جستجو و خواندن پیامک‌های بانکی از گوشی...", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        } else if (deviceBankMessages.isEmpty()) {
                            Text(
                                text = "هیچ پیامک بانکی در صندوق پیامک‌های گوشی یافت نشد. دکمه «به‌روزرسانی» یا «خوانش اس‌ام‌اس‌های جدید» را بزنید.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            )
                        } else {
                            val availableBanks = remember(deviceBankMessages) {
                                listOf("همه") + deviceBankMessages.mapNotNull { it.detectedBank }.distinct()
                            }

                            if (availableBanks.size > 2) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(availableBanks) { bName ->
                                        FilterChip(
                                            selected = selectedBankFilter == bName,
                                            onClick = { selectedBankFilter = bName },
                                            label = { Text(bName, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF3B82F6),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            val filteredMessages = remember(deviceBankMessages, selectedBankFilter) {
                                if (selectedBankFilter == "همه") deviceBankMessages
                                else deviceBankMessages.filter { it.detectedBank == selectedBankFilter }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                filteredMessages.take(25).forEach { msg ->
                                    val detectedSender = BankSmsSender(
                                        bankName = msg.detectedBank ?: "بانک",
                                        senderNumber = msg.address,
                                        isActive = true
                                    )
                                    SenderSmsItemCard(
                                        message = msg,
                                        sender = detectedSender,
                                        categoryNames = categoryNames,
                                        bankAccounts = bankAccounts,
                                        isAlreadyRegistered = registeredTransactions.any { tx ->
                                            tx.dateMillis == msg.timestamp ||
                                            (!tx.rawSmsText.isNullOrBlank() && (tx.rawSmsText == msg.body || msg.body.contains(tx.rawSmsText!!)))
                                        },
                                        onRegister = { tx ->
                                            coroutineScope.launch {
                                                repository.transactionDao.insert(tx)
                                                if (tx.balanceAfterToman != null && tx.balanceAfterToman > 0) {
                                                    val matchedCard = bankAccounts.find {
                                                        (tx.cardNumber != null && it.cardNumber.endsWith(tx.cardNumber)) ||
                                                        it.bankName == detectedSender.bankName
                                                    }
                                                    if (matchedCard != null) {
                                                        repository.bankAccountDao.update(
                                                            matchedCard.copy(balanceToman = tx.balanceAfterToman)
                                                        )
                                                    }
                                                }
                                                android.widget.Toast.makeText(context, "تراکنش با موفقیت ثبت شد", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
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
                                                Text(text = pending.amountToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان", color = Color.White, fontWeight = FontWeight.Bold)
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

                        // Prominent drawer button: Pick from SMS inbox
                        Button(
                            onClick = { showSmsPickerBottomSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_sms_sender_drawer_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5C82E6),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("📩 باز کردن صندوق پیامک‌ها و انتخاب سرشماره (کشویی)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                onDelete = { viewModel.deleteBankSmsSender(sender) },
                                onViewSms = { selectedSenderForSmsView = sender },
                                onFilterScreen = { onNavigateToFilteredSms(sender.senderNumber) }
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
                                            text = result.amountToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان",
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

    // Drawer: Select Bank Sender from Device SMS Inbox
    if (showSmsPickerBottomSheet) {
        SmsSenderPickerSheet(
            onDismiss = { showSmsPickerBottomSheet = false },
            onSenderSelected = { address ->
                // Handled in onSenderAndBankSelected
            },
            onSenderAndBankSelected = { address, detectedBank ->
                viewModel.addBankSmsSender(detectedBank, address)
                showSmsPickerBottomSheet = false
            }
        )
    }

    // Dialog: Add New SMS Sender Number
    if (showAddSenderDialog) {
        AddSmsSenderDialog(
            onDismiss = { showAddSenderDialog = false },
            onOpenPicker = {
                showAddSenderDialog = false
                showSmsPickerBottomSheet = true
            },
            onConfirm = { bankName, senderNumber ->
                viewModel.addBankSmsSender(bankName, senderNumber)
                showAddSenderDialog = false
            }
        )
    }

    selectedSenderForSmsView?.let { sender ->
        com.example.ui.components.SenderSmsViewerSheet(
            sender = sender,
            onDismiss = { selectedSenderForSmsView = null }
        )
    }
}

@Composable
fun BankSmsSenderRow(
    sender: BankSmsSender,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onViewSms: () -> Unit = {},
    onFilterScreen: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewSms() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF36343B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BankLogoImage(
                        bankName = sender.bankName,
                        cardNumber = null,
                        containerSize = 36.dp,
                        logoSize = 28.dp,
                        containerColor = Color(0xFF2B2930)
                    )

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

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewSms,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF435574),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("مشاهده کشویی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onFilterScreen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("صحت‌سنجی متن خام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddSmsSenderDialog(
    onDismiss: () -> Unit,
    onOpenPicker: () -> Unit = {},
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onOpenPicker,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C82E6),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("📩 انتخاب از صندوق پیامک‌ها (کشویی)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF49454F))
                    Text(" یا وارد کردن دستی ", color = Color(0xFFCAC4D0), fontSize = 11.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF49454F))
                }

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
