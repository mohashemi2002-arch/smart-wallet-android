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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.FinanceRepository
import com.example.data.SmsAiParser
import com.example.data.model.*
import com.example.ui.components.BankLogoImage
import com.example.ui.components.PersianFormatters
import com.example.ui.components.formatAsToman
import com.example.util.DeviceSenderSmsMessage
import com.example.util.DeviceSmsReader
import kotlinx.coroutines.launch

/**
 * Screen that lists all incoming SMS messages, groups them by sender number,
 * and provides a filter to display messages from a specific sender.
 * Uses a LazyColumn to list messages and shows the raw message content for verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingSmsGroupedScreen(
    initialSenderFilter: String? = null,
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

    var allIncomingMessages by remember { mutableStateOf<List<DeviceSenderSmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Selected filter for sender number (null means "همه فرستنده‌ها" / all senders)
    var activeSenderFilter by rememberSaveable { mutableStateOf<String?>(initialSenderFilter) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var requireBalanceOnly by rememberSaveable { mutableStateOf(false) }

    fun loadAllMessages() {
        if (!hasPermission) return
        coroutineScope.launch {
            isLoading = true
            val msgs = DeviceSmsReader.getAllIncomingMessages(context, limit = 1500)
            allIncomingMessages = msgs
            isLoading = false
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            loadAllMessages()
        }
    }

    // Grouping all messages by sender number
    val messagesBySender: Map<String, List<DeviceSenderSmsMessage>> = remember(allIncomingMessages) {
        allIncomingMessages.groupBy { it.address }
    }

    // List of sender summaries for the filter carousel
    val senderSummaries = remember(messagesBySender, bankSmsSenders) {
        messagesBySender.map { (address, list) ->
            val detectedBank = list.firstNotNullOfOrNull { it.detectedBank }
                ?: bankSmsSenders.find { DeviceSmsReader.isAddressStrictMatch(it.senderNumber, address) }?.bankName
                ?: DeviceSmsReader.detectBankName(address, "")
            SenderGroupSummary(
                address = address,
                count = list.size,
                latestTimestamp = list.maxOfOrNull { it.timestamp } ?: 0L,
                detectedBank = detectedBank
            )
        }.sortedByDescending { it.count }
    }

    // Filter messages according to activeSenderFilter, requireBalanceOnly, and searchQuery
    val displayMessages = remember(allIncomingMessages, activeSenderFilter, requireBalanceOnly, searchQuery) {
        var list = allIncomingMessages
        if (!activeSenderFilter.isNullOrBlank()) {
            val target = activeSenderFilter!!
            list = list.filter { DeviceSmsReader.isAddressStrictMatch(it.address, target) || it.address.equals(target, ignoreCase = true) }
        }
        if (requireBalanceOnly) {
            list = list.filter { it.hasBalance && !DeviceSmsReader.isOtpWithoutBalance(it.body) }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { it.body.lowercase().contains(q) || it.address.lowercase().contains(q) }
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "پیامک‌های دریافتی (دسته‌بندی بر اساس فرستنده)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFE6E1E5)
                        )
                        Text(
                            text = if (activeSenderFilter == null) "نمایش همه فرستنده‌ها (${allIncomingMessages.size} پیامک)"
                            else "فرستنده: $activeSenderFilter (${displayMessages.size} پیامک)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("incoming_sms_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color(0xFFE6E1E5)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadAllMessages() },
                        modifier = Modifier.testTag("incoming_sms_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "به‌روزرسانی",
                            tint = Color(0xFF60A5FA)
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
                .padding(horizontal = 16.dp)
                .testTag("incoming_sms_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // Permission Banner
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
                                text = "دسترسی به پیامک‌ها نیاز است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "برای خواندن، گروه‌بندی و فیلتر پیامک‌های دریافتی گوشی، دسترسی پیامک را تایید کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
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

            // Controls & Sender Grouping Carousel
            if (hasPermission) {
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
                            // Section header
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
                                        Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "فیلتر بر اساس فرستنده (گروه‌بندی شده):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }

                                if (activeSenderFilter != null) {
                                    TextButton(
                                        onClick = { activeSenderFilter = null },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("نمایش همه", fontSize = 11.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Grouped Senders Horizontal Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("sender_chips_row")
                            ) {
                                // "All" Chip
                                item {
                                    FilterChip(
                                        selected = activeSenderFilter == null,
                                        onClick = { activeSenderFilter = null },
                                        label = {
                                            Text(
                                                text = "همه (${allIncomingMessages.size})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF2563EB),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF2D3748),
                                            labelColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }

                                // Each Sender Group
                                items(senderSummaries) { summary ->
                                    val isSelected = activeSenderFilter != null &&
                                            (summary.address.equals(activeSenderFilter, ignoreCase = true) ||
                                                    DeviceSmsReader.isAddressStrictMatch(summary.address, activeSenderFilter!!))

                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            activeSenderFilter = if (isSelected) null else summary.address
                                        },
                                        leadingIcon = {
                                            if (summary.detectedBank != null) {
                                                BankLogoImage(
                                                    bankName = summary.detectedBank,
                                                    cardNumber = null,
                                                    containerSize = 20.dp,
                                                    logoSize = 16.dp
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.PhoneAndroid,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) Color.White else Color(0xFF94A3B8)
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = "${summary.detectedBank ?: summary.address} (${summary.count})",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
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

                            // Search Query in message body or sender
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("جستجو در متن پیامک‌ها یا شماره...", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "پاک کردن", tint = Color(0xFF94A3B8))
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sms_search_textfield")
                            )

                            // Quick Filter Options (Balance only toggle)
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
                                    Text(
                                        text = "فقط پیامک‌های دارای مانده/تراکنش",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = "تعداد: ${displayMessages.size}",
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
            }

            // Loading Indicator
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
                                text = "در حال بارگذاری و گروه‌بندی پیامک‌های دریافتی...",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Empty State
            if (!isLoading && hasPermission && displayMessages.isEmpty()) {
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
                                text = if (activeSenderFilter != null) {
                                    "هیچ پیامکی از فرستنده $activeSenderFilter با فیلترهای انتخابی یافت نشد."
                                } else {
                                    "صندوق پیامک‌های دستگاه خالی است یا پیامی یافت نشد."
                                },
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            if (activeSenderFilter != null || requireBalanceOnly) {
                                Button(
                                    onClick = {
                                        activeSenderFilter = null
                                        requireBalanceOnly = false
                                        searchQuery = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Text("حذف فیلترها و نمایش همه پیامک‌ها", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Messages List in LazyColumn with Raw Content for Verification
            items(displayMessages, key = { it.id }) { msg ->
                val detectedSender = remember(msg) {
                    BankSmsSender(
                        bankName = msg.detectedBank ?: DeviceSmsReader.detectBankName(msg.address, msg.body) ?: "فرستنده",
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

private data class SenderGroupSummary(
    val address: String,
    val count: Int,
    val latestTimestamp: Long,
    val detectedBank: String?
)
