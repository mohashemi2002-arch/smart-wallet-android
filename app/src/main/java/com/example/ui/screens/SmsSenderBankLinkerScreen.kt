package com.example.ui.screens

import android.Manifest
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
import androidx.core.content.ContextCompat
import com.example.data.FinanceRepository
import com.example.data.model.BankSmsSender
import com.example.ui.components.BankLogoImage
import com.example.ui.components.PersianCurrencyFormatter
import com.example.ui.components.SelectBankForSenderDialog
import com.example.util.DeviceSenderSmsMessage
import com.example.util.DeviceSmsReader
import com.example.util.SmsSenderPreview
import kotlinx.coroutines.launch

/**
 * Dedicated Screen: Reads the device SMS inbox, groups messages by sender,
 * and enables the user to link any sender name/number directly to a specific Bank entity in the Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSenderBankLinkerScreen(
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
    var rawSendersList by remember { mutableStateOf<List<SmsSenderPreview>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Dialog state for linking a sender to a bank entity
    var linkingSenderAddress by remember { mutableStateOf<String?>(null) }
    var linkingSenderCurrentBank by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("همه") } // "همه", "متصل شده", "متصل نشده"

    fun loadSenders() {
        if (!hasPermission) return
        coroutineScope.launch {
            isLoading = true
            val senders = DeviceSmsReader.getUniqueSenders(context)
            rawSendersList = senders
            isLoading = false
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            loadSenders()
        }
    }

    // Combine device sender preview with Room database associations
    val enrichedSenderGroups = remember(rawSendersList, bankSmsSenders) {
        val senders = rawSendersList ?: emptyList()
        senders.map { preview ->
            val persisted = bankSmsSenders.find {
                DeviceSmsReader.isAddressStrictMatch(it.senderNumber, preview.address)
            }
            SenderLinkItem(
                address = preview.address,
                totalCount = preview.totalCount,
                lastMessage = preview.lastMessage,
                timestamp = preview.timestamp,
                persistedBank = persisted?.bankName,
                detectedBank = persisted?.bankName ?: preview.detectedBank ?: DeviceSmsReader.detectBankName(preview.address, preview.lastMessage),
                isLinked = persisted != null
            )
        }.sortedWith(compareByDescending<SenderLinkItem> { it.isLinked }.thenByDescending { it.totalCount })
    }

    val filteredList = remember(enrichedSenderGroups, searchQuery, selectedFilterCategory) {
        var list = enrichedSenderGroups
        if (selectedFilterCategory == "متصل شده") {
            list = list.filter { it.isLinked }
        } else if (selectedFilterCategory == "متصل نشده") {
            list = list.filter { !it.isLinked }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.address.lowercase().contains(q) ||
                (it.detectedBank != null && it.detectedBank.lowercase().contains(q)) ||
                it.lastMessage.lowercase().contains(q)
            }
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "اتصال سرشماره‌ها به بانک‌ها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFFE6E1E5)
                        )
                        Text(
                            text = "گروه‌بندی پیامک‌ها و ثبت انتساب در پایگاه داده",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sender_linker_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color(0xFFE6E1E5)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loadSenders() }) {
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // Permission request if not granted
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
                                text = "برای خواندن و گروه‌بندی پیامک‌ها بر اساس فرستنده و اتصال آن‌ها به بانک، مجوز را تایید کنید.",
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

            // Summary KPI & Search Card
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "اتصال فرستنده‌ها به بانک‌های سیستم",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF1E3A8A)
                                ) {
                                    Text(
                                        text = "${enrichedSenderGroups.count { it.isLinked }} از ${enrichedSenderGroups.size} متصل",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF93C5FD),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "روی هر گروه فرستنده بزنید تا نام یا سرشماره آن را به یک بانک مشخص در پایگاه داده متصل کنید.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 18.sp
                            )

                            // Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("جستجوی سرشماره یا نام بانک...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = Color(0xFF94A3B8))
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
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Filter Chips (All, Linked, Unlinked)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("همه", "متصل شده", "متصل نشده").forEach { cat ->
                                    val isSelected = selectedFilterCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilterCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp) },
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
                    }
                }
            }

            // Loading state
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
                            Text("در حال خواندن و گروه‌بندی پیامک‌های صندوق...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Empty state
            if (!isLoading && hasPermission && filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                            Text("فرستنده‌ای مطابق با جستجوی شما یافت نشد.", color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                }
            }

            // List of Sender Groups
            items(filteredList, key = { it.address }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            linkingSenderAddress = item.address
                            linkingSenderCurrentBank = item.persistedBank ?: item.detectedBank ?: ""
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2534)),
                    border = BorderStroke(
                        1.dp,
                        if (item.isLinked) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF334155)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    bankName = item.detectedBank,
                                    cardNumber = null,
                                    containerSize = 40.dp,
                                    logoSize = 30.dp,
                                    containerColor = Color(0xFF2B3447)
                                )

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.address,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )

                                        if (item.isLinked) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(11.dp))
                                                    Text("متصل به دیتابیس", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = if (item.isLinked) "بانک منتسب: ${item.persistedBank}"
                                               else if (item.detectedBank != null) "بانک پیشنهادی: ${item.detectedBank}"
                                               else "بدون انتساب به بانک",
                                        fontSize = 12.sp,
                                        color = if (item.isLinked) Color(0xFF93C5FD) else Color(0xFF94A3B8),
                                        fontWeight = if (item.isLinked) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF2B3447)
                            ) {
                                Text(
                                    text = PersianCurrencyFormatter.toPersianDigits("${item.totalCount} پیام"),
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Last Message Snippet
                        if (item.lastMessage.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF141A25),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.lastMessage,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(10.dp),
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        // Interactive Link Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "برای تغییر یا تعیین بانک لمس کنید",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            Button(
                                onClick = {
                                    linkingSenderAddress = item.address
                                    linkingSenderCurrentBank = item.persistedBank ?: item.detectedBank ?: ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (item.isLinked) Color(0xFF2563EB) else Color(0xFF0D9488),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isLinked) Icons.Default.Edit else Icons.Default.AddLink,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (item.isLinked) "تغییر بانک" else "اتصال به بانک",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Select and Persist Bank Entity to Room DB
    linkingSenderAddress?.let { address ->
        SelectBankForSenderDialog(
            senderNumber = address,
            initialBankName = linkingSenderCurrentBank,
            onDismiss = { linkingSenderAddress = null },
            onConfirm = { chosenBankName ->
                coroutineScope.launch {
                    val existing = bankSmsSenders.find { DeviceSmsReader.isAddressStrictMatch(it.senderNumber, address) }
                    if (existing != null) {
                        repository.bankSmsSenderDao.update(
                            existing.copy(bankName = chosenBankName)
                        )
                    } else {
                        repository.bankSmsSenderDao.insert(
                            BankSmsSender(
                                bankName = chosenBankName,
                                senderNumber = address,
                                isActive = true
                            )
                        )
                    }
                    Toast.makeText(
                        context,
                        "سرشماره $address با موفقیت به $chosenBankName متصل شد و در دیتابیس ثبت گردید",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                linkingSenderAddress = null
            }
        )
    }
}

private data class SenderLinkItem(
    val address: String,
    val totalCount: Int,
    val lastMessage: String,
    val timestamp: Long,
    val persistedBank: String?,
    val detectedBank: String?,
    val isLinked: Boolean
)
