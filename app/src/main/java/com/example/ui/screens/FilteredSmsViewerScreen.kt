package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.FinanceRepository
import com.example.data.SmsAiParser
import com.example.data.model.*
import com.example.ui.components.BankLogoImage
import com.example.ui.components.PersianFormatters
import com.example.ui.components.SmsSenderPickerSheet
import com.example.ui.components.formatAsToman
import com.example.util.DeviceSenderSmsMessage
import com.example.util.DeviceSmsReader
import kotlinx.coroutines.launch

/**
 * Screen that filters incoming SMS messages based on a selected sender number.
 * Displays the complete raw message content prominently for verification,
 * along with parsed transaction fields (amount, balance, card),
 * interactive category dropdown, and one-click transaction registration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredSmsViewerScreen(
    initialSenderNumber: String? = null,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val repository = remember { FinanceRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    val bankSmsSenders by repository.allBankSmsSenders.collectAsState(initial = emptyList())
    val bankAccounts by repository.allBankAccounts.collectAsState(initial = emptyList())
    val dbCategories by repository.allCategories.collectAsState(initial = emptyList())
    val registeredTransactions by repository.allTransactions.collectAsState(initial = emptyList())

    val categoryNames = remember(dbCategories) {
        val names = dbCategories.map { it.name }.filter { it.isNotBlank() }
        if (names.isNotEmpty()) names else listOf(
            "خوراکی و سوپرمارکت",
            "حمل و نقل",
            "خرید و پوشاک",
            "پزشکی و سلامت",
            "قبوض و ساختمان",
            "تفریح و سرگرمی",
            "درآمد و حقوق",
            "متفرقه"
        )
    }

    // Selected Sender Number & Filter Controls
    var selectedSenderNumber by remember {
        mutableStateOf(initialSenderNumber ?: bankSmsSenders.firstOrNull()?.senderNumber ?: "")
    }

    LaunchedEffect(bankSmsSenders) {
        if (selectedSenderNumber.isBlank() && bankSmsSenders.isNotEmpty()) {
            selectedSenderNumber = bankSmsSenders.first().senderNumber
        }
    }

    var customSenderInput by remember { mutableStateOf(selectedSenderNumber) }
    var requireBalanceOnly by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showSmsPickerBottomSheet by remember { mutableStateOf(false) }

    var messages by remember { mutableStateOf<List<DeviceSenderSmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshMessages() {
        if (!hasPermission || selectedSenderNumber.isBlank()) return
        coroutineScope.launch {
            isLoading = true
            val msgs = DeviceSmsReader.getMessagesForSender(
                context = context,
                senderNumber = selectedSenderNumber,
                bankName = bankSmsSenders.find { DeviceSmsReader.isAddressStrictMatch(it.senderNumber, selectedSenderNumber) }?.bankName,
                requireBalance = requireBalanceOnly,
                limit = 200
            )
            messages = msgs
            isLoading = false
        }
    }

    LaunchedEffect(selectedSenderNumber, requireBalanceOnly, hasPermission) {
        refreshMessages()
    }

    // Filter messages by search query if any
    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            val q = searchQuery.trim().lowercase()
            messages.filter { it.body.lowercase().contains(q) || it.address.lowercase().contains(q) }
        }
    }

    val currentBankName = remember(selectedSenderNumber, bankSmsSenders) {
        val match = bankSmsSenders.find { DeviceSmsReader.isAddressStrictMatch(it.senderNumber, selectedSenderNumber) }
        match?.bankName ?: DeviceSmsReader.detectBankName(selectedSenderNumber, "") ?: "بانک مربوطه"
    }

    if (showSmsPickerBottomSheet) {
        SmsSenderPickerSheet(
            onDismiss = { showSmsPickerBottomSheet = false },
            onSenderSelected = { address ->
                selectedSenderNumber = address
                customSenderInput = address
                showSmsPickerBottomSheet = false
            },
            onSenderAndBankSelected = { address, detectedBank ->
                selectedSenderNumber = address
                customSenderInput = address
                coroutineScope.launch {
                    val exists = bankSmsSenders.any { DeviceSmsReader.isAddressStrictMatch(it.senderNumber, address) }
                    if (!exists) {
                        repository.bankSmsSenderDao.insert(
                            BankSmsSender(bankName = detectedBank, senderNumber = address, isActive = true)
                        )
                    }
                }
                showSmsPickerBottomSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "فیلتر پیامک‌ها بر اساس سرشماره",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFFE6E1E5)
                        )
                        Text(
                            text = "صحت‌سنجی متن اصلی پیامک و ثبت تراکنش",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color(0xFFE6E1E5)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshMessages() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "به‌روزرسانی",
                            tint = Color(0xFF60A5FA)
                        )
                    }
                    IconButton(onClick = { showSmsPickerBottomSheet = true }) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = "انتخاب سرشماره از صندوق پیامک‌ها",
                            tint = Color(0xFFD0BCFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E212B))
            )
        },
        containerColor = Color(0xFF141822)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // 1. Sender Selector Carousel & Search Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222938)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "انتخاب سرشماره فرستنده:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )

                            TextButton(
                                onClick = { showSmsPickerBottomSheet = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF60A5FA))
                                Spacer(Modifier.width(4.dp))
                                Text("صندوق پیامک‌ها", fontSize = 12.sp, color = Color(0xFF60A5FA))
                            }
                        }

                        // Horizontal Bank Sender Chips
                        if (bankSmsSenders.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(bankSmsSenders) { senderItem ->
                                    val isSelected = DeviceSmsReader.isAddressStrictMatch(senderItem.senderNumber, selectedSenderNumber)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedSenderNumber = senderItem.senderNumber
                                            customSenderInput = senderItem.senderNumber
                                        },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                BankLogoImage(
                                                    bankName = senderItem.bankName,
                                                    cardNumber = null,
                                                    containerSize = 20.dp,
                                                    logoSize = 16.dp
                                                )
                                                Text(
                                                    text = "${senderItem.bankName} (${senderItem.senderNumber})",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF2563EB),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF2D3748),
                                            labelColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }
                            }
                        }

                        // Custom Sender Number Input Field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customSenderInput,
                                onValueChange = {
                                    customSenderInput = it
                                },
                                label = { Text("سرشماره هدف (مثلاً 9830009417 یا BLUBANK)", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (customSenderInput.isNotBlank()) {
                                        IconButton(onClick = {
                                            selectedSenderNumber = customSenderInput.trim()
                                        }) {
                                            Icon(Icons.Default.Check, contentDescription = "اعمال", tint = Color(0xFF60A5FA))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    selectedSenderNumber = customSenderInput.trim()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Text("فیلتر", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Options & Balance Filter Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Switch(
                                    checked = requireBalanceOnly,
                                    onCheckedChange = { requireBalanceOnly = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF10B981)
                                    )
                                )
                                Column {
                                    Text(
                                        text = "فقط تراکنش‌های دارای مانده",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "فیلتر و حذف پیامک‌های رمز دوم و رمز پویا",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "تعداد: ${filteredMessages.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Permission Request Banner if not granted
            if (!hasPermission) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282E3E)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(44.dp))
                            Text(
                                text = "دسترسی به پیامک‌ها مورد نیاز است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "برای خواندن و فیلتر کردن پیامک‌های سرشماره $selectedSenderNumber، لطفاً دسترسی پیامک را تایید کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.READ_SMS) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("اعطای دسترسی به پیامک‌ها", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Loading State
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
                            Text(
                                text = "در حال استخراج پیامک‌های سرشماره $selectedSenderNumber...",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 4. Empty State
            if (!isLoading && hasPermission && filteredMessages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text(
                                text = if (requireBalanceOnly) {
                                    "هیچ پیامک تراکنشی (دارای مانده) از سرشماره $selectedSenderNumber یافت نشد."
                                } else {
                                    "هیچ پیامکی از سرشماره $selectedSenderNumber در صندوق پیامک‌های دستگاه یافت نشد."
                                },
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (requireBalanceOnly) {
                                OutlinedButton(
                                    onClick = { requireBalanceOnly = false },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60A5FA))
                                ) {
                                    Text("نمایش همه پیامک‌ها (شامل رمز دوم و بدون مانده)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Filtered Message Cards
            items(filteredMessages, key = { it.id }) { msg ->
                val detectedSender = remember(msg, currentBankName) {
                    BankSmsSender(
                        bankName = msg.detectedBank ?: currentBankName,
                        senderNumber = msg.address,
                        isActive = true
                    )
                }

                RawVerifiedSmsCard(
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
                            Toast.makeText(context, "تراکنش با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

/**
 * High-craftsmanship card displaying the RAW message content prominently for verification,
 * along with extracted financial fields, balance, interactive category dropdown, and registration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawVerifiedSmsCard(
    message: DeviceSenderSmsMessage,
    sender: BankSmsSender,
    categoryNames: List<String>,
    bankAccounts: List<BankAccount>,
    isAlreadyRegistered: Boolean,
    onRegister: (Transaction) -> Unit
) {
    val context = LocalContext.current
    var isRegisteredLocal by remember { mutableStateOf(isAlreadyRegistered) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    // Parse the SMS text using the regex engine
    val parsed = remember(message.body) {
        SmsAiParser.parseSmsWithRegex(message.body, sender.senderNumber)
    }

    var selectedType by remember { mutableStateOf(parsed.type) }
    var selectedCategory by remember { mutableStateOf(parsed.categoryName.ifBlank { "متفرقه" }) }
    var selectedMerchant by remember { mutableStateOf(parsed.merchantName.ifBlank { sender.bankName }) }
    var amountValue by remember { mutableStateOf(parsed.amountToman) }

    val isExpense = selectedType == TransactionType.EXPENSE
    val typeColor = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2534)),
        border = BorderStroke(
            1.dp,
            if (isRegisteredLocal) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF334155)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Bank Logo, Sender Address & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BankLogoImage(
                        bankName = sender.bankName,
                        cardNumber = null,
                        containerSize = 34.dp,
                        logoSize = 26.dp,
                        containerColor = Color(0xFF2B3447)
                    )
                    Column {
                        Text(
                            text = sender.bankName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "سرشماره: ${message.address}",
                            fontSize = 11.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = PersianFormatters.formatDateTime(message.timestamp),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    if (isRegisteredLocal) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(13.dp))
                                Text("ثبت شده", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "بررسی نشده",
                                color = Color(0xFFFBBF24),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // PRIMARY INSPECTION PANEL: Raw message content explicitly displayed for verification
            Surface(
                color = Color(0xFF131924),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF26334A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "متن اصلی پیامک (جهت صحت‌سنجی):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SMS Content", message.body)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "متن پیامک کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "کپی متن",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    SelectionContainer {
                        Text(
                            text = message.body,
                            color = Color(0xFFF8FAFC),
                            fontSize = 13.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Financial Indicators (Parsed Amount, Type, and Balance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مبلغ تراکنش:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = if (amountValue > 0) "${amountValue.formatAsToman()} تومان" else "مبلغ نامشخص",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }

                // Type Toggle Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("برداشت", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFFCA5A5)
                        )
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("واریز", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF6EE7B7)
                        )
                    )
                }
            }

            // Balance Highlight Box (مانده حساب)
            if (parsed.balanceToman != null && parsed.balanceToman > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0D3331),
                    border = BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(16.dp))
                            Text("مانده حساب در پیامک:", fontSize = 12.sp, color = Color(0xFFCCFBF1), fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "${parsed.balanceToman.formatAsToman()} تومان",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2DD4BF)
                        )
                    }
                }
            }

            // Category Selection Dropdown Menu (منوی کشویی دسته‌بندی)
            ExposedDropdownMenuBox(
                expanded = expandedCategoryDropdown,
                onExpandedChange = { expandedCategoryDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("دسته‌بندی (انتخاب از منوی کشویی)", fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF3B82F6),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false },
                    modifier = Modifier.background(Color(0xFF222B3D))
                ) {
                    categoryNames.forEach { catName ->
                        DropdownMenuItem(
                            text = { Text(catName, color = Color.White, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                selectedCategory = catName
                                expandedCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            // One-Click Registration Button
            if (!isRegisteredLocal) {
                Button(
                    onClick = {
                        val tx = Transaction(
                            amountToman = if (amountValue > 0) amountValue else 1000L,
                            type = selectedType,
                            categoryName = selectedCategory.ifBlank { "متفرقه" },
                            merchantName = selectedMerchant.ifBlank { sender.bankName },
                            cardNumber = parsed.cardNumber,
                            dateMillis = message.timestamp,
                            rawSmsText = message.body,
                            isParsedFromSms = true,
                            status = TransactionStatus.CONFIRMED,
                            bankName = sender.bankName,
                            balanceAfterToman = parsed.balanceToman,
                            senderNumber = message.address
                        )
                        onRegister(tx)
                        isRegisteredLocal = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ثبت این پیامک به عنوان تراکنش", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
