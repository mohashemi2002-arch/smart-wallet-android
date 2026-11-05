package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.FinanceRepository
import com.example.data.SmsAiParser
import com.example.data.model.*
import com.example.util.DeviceSenderSmsMessage
import com.example.util.DeviceSmsReader
import kotlinx.coroutines.launch

/**
 * BottomSheet displaying SMS messages received strictly from a specific Bank SMS Sender.
 * Enforces:
 * 1. Strictly from the senderNumber (no fallback to other banks).
 * 2. Highlights & requires balance ("مانده" / "موجودی") to filter out OTP / رمز دوم SMS.
 * 3. Shows full raw original SMS body without truncation.
 * 4. Interactive category dropdown menu with 1-click transaction registration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenderSmsViewerSheet(
    sender: BankSmsSender,
    onDismiss: () -> Unit,
    onTransactionSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FinanceRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    var messages by remember { mutableStateOf<List<DeviceSenderSmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var onlyWithBalance by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            coroutineScope.launch {
                isLoading = true
                val result = DeviceSmsReader.getMessagesForSender(
                    context = context,
                    senderNumber = sender.senderNumber,
                    bankName = sender.bankName,
                    requireBalance = onlyWithBalance
                )
                messages = result
                isLoading = false
            }
        }
    }

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

    LaunchedEffect(sender.senderNumber, hasPermission, onlyWithBalance) {
        if (hasPermission) {
            isLoading = true
            val result = DeviceSmsReader.getMessagesForSender(
                context = context,
                senderNumber = sender.senderNumber,
                bankName = sender.bankName,
                requireBalance = onlyWithBalance
            )
            messages = result
            isLoading = false
        } else {
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E212B),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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
                        containerSize = 42.dp,
                        logoSize = 34.dp,
                        containerColor = Color(0xFF2C3242)
                    )
                    Column {
                        Text(
                            text = "پیامک‌های ${sender.bankName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "سرشماره اختصاصی: ${sender.senderNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF60A5FA),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
                }
            }

            HorizontalDivider(color = Color(0xFF2E3446))

            // Filter Chips: Require Balance vs All Messages from this Sender
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = onlyWithBalance,
                    onClick = { onlyWithBalance = true },
                    label = { Text("فقط تراکنش‌های دارای مانده", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        if (onlyWithBalance) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF262E3D),
                        labelColor = Color(0xFF94A3B8)
                    )
                )

                FilterChip(
                    selected = !onlyWithBalance,
                    onClick = { onlyWithBalance = false },
                    label = { Text("همه پیامک‌ها (شامل رمز دوم)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF475569),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF262E3D),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }

            if (!hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF282E3E)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF5C82E6), modifier = Modifier.size(48.dp))
                            Text(
                                text = "دسترسی خواندن پیامک‌ها نیاز است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "برای استخراج پیامک‌های بانکی از گوشی و نمایش آنها، لطفاً دسترسی پیامک را تایید کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.READ_SMS) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
                            ) {
                                Text("اعطای دسترسی به پیامک‌ها", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF5C82E6))
                        Spacer(Modifier.height(12.dp))
                        Text("در حال جستجوی پیامک‌های سرشماره ${sender.senderNumber}...", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            } else if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text(
                            text = if (onlyWithBalance) {
                                "هیچ پیامک تراکنشی (دارای مانده) از سرشماره ${sender.senderNumber} یافت نشد."
                            } else {
                                "هیچ پیامکی از سرشماره ${sender.senderNumber} در صندوق پیامک‌های گوشی وجود ندارد."
                            },
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (onlyWithBalance) {
                            TextButton(onClick = { onlyWithBalance = false }) {
                                Text("مشاهده همه پیامک‌ها (شامل پیامک‌های بدون مانده و رمز دوم)", fontSize = 12.sp, color = Color(0xFF60A5FA))
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "تعداد ${messages.size} پیامک دریافت شد از سرشماره ${sender.senderNumber}:",
                    color = Color(0xFFB0B7C3),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        SenderSmsItemCard(
                            message = msg,
                            sender = sender,
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
                                            it.bankName == sender.bankName
                                        }
                                        if (matchedCard != null) {
                                            repository.bankAccountDao.update(
                                                matchedCard.copy(balanceToman = tx.balanceAfterToman)
                                            )
                                        }
                                    }
                                    Toast.makeText(context, "تراکنش با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                                    onTransactionSaved()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying an individual SMS message from a specific bank sender.
 * Displays:
 * 1. Full raw original SMS body (متن اصلی پیامک) without truncation.
 * 2. Extracted balance badge (مانده حساب) if available.
 * 3. Amount & Type (واریز / برداشت).
 * 4. Interactive category selection dropdown (منوی کشویی دسته‌بندی).
 * 5. One-click registration button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenderSmsItemCard(
    message: DeviceSenderSmsMessage,
    sender: BankSmsSender,
    categoryNames: List<String>,
    bankAccounts: List<BankAccount>,
    isAlreadyRegistered: Boolean,
    onRegister: (Transaction) -> Unit
) {
    var isRegisteredLocal by remember { mutableStateOf(isAlreadyRegistered) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    // Parse the SMS text
    val parsed = remember(message.body) {
        SmsAiParser.parseSmsWithRegex(message.body, sender.senderNumber)
    }

    var selectedType by remember { mutableStateOf(parsed.type) }
    var selectedCategory by remember { mutableStateOf(parsed.categoryName.ifBlank { "متفرقه" }) }
    var selectedMerchant by remember { mutableStateOf(parsed.merchantName.ifBlank { sender.bankName }) }
    var amountValue by remember { mutableStateOf(parsed.amountToman) }

    val isExpense = selectedType == TransactionType.EXPENSE
    val typeColor = if (isExpense) Color(0xFFEF5350) else Color(0xFF4CAF50)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282E3F)),
        border = BorderStroke(
            1.dp,
            if (isRegisteredLocal) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFF384055)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Date & Status & Sender Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = PersianFormatters.formatDateTime(message.timestamp),
                        fontSize = 11.sp,
                        color = Color(0xFFA0A7B5)
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1E2633)
                    ) {
                        Text(
                            text = "سرشماره: ${message.address}",
                            fontSize = 10.sp,
                            color = Color(0xFF93C5FD),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (isRegisteredLocal) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(14.dp))
                            Text("ثبت شده", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFA726).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color(0xFFFFA726), modifier = Modifier.size(13.dp))
                            Text("در انتظار تایید", color = Color(0xFFFFA726), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Amount & Type Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مبلغ تراکنش:", fontSize = 11.sp, color = Color(0xFFA0A7B5))
                    Text(
                        text = if (amountValue > 0) "${amountValue.formatAsToman()} تومان" else "نامشخص",
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
                            selectedContainerColor = Color(0xFFEF5350).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFFF8A80)
                        )
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("واریز", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFFA5D6A7)
                        )
                    )
                }
            }

            // Balance Box (مانده حساب)
            if (parsed.balanceToman != null && parsed.balanceToman > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F3A3A),
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
            } else if (message.hasBalance) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F3A3A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(15.dp))
                        Text("پیامک دارای مانده است", fontSize = 11.sp, color = Color(0xFFCCFBF1))
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
                        focusedBorderColor = Color(0xFF5C82E6),
                        unfocusedBorderColor = Color(0xFF4A5568),
                        focusedLabelColor = Color(0xFF5C82E6),
                        unfocusedLabelColor = Color(0xFFA0A7B5)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false },
                    modifier = Modifier.background(Color(0xFF282E3F))
                ) {
                    categoryNames.forEach { catName ->
                        DropdownMenuItem(
                            text = { Text(catName, color = Color.White, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF5C82E6), modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                selectedCategory = catName
                                expandedCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            // Full Raw Original SMS Text Box (متن اصلی پیامک بدون خلاصه سازی)
            Surface(
                color = Color(0xFF182030),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2D3B52)),
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
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "متن اصلی پیامک دریافتی:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD)
                            )
                        }

                        Text(
                            text = if (message.hasBalance) "تراکنش بانکی ✓" else "پیامک بانکی",
                            fontSize = 10.sp,
                            color = if (message.hasBalance) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    SelectionContainer {
                        Text(
                            text = message.body,
                            color = Color(0xFFF1F5F9),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Register Transaction Button
            if (!isRegisteredLocal) {
                Button(
                    onClick = {
                        if (amountValue <= 0) return@Button
                        val tx = Transaction(
                            amountToman = amountValue,
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
                            senderNumber = sender.senderNumber
                        )
                        onRegister(tx)
                        isRegisteredLocal = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C82E6),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ثبت این پیامک به عنوان تراکنش", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
