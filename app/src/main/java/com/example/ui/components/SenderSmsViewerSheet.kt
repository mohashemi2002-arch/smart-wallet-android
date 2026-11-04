package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.FinanceRepository
import com.example.data.SmsAiParser
import com.example.data.model.*
import com.example.util.DeviceSenderSmsMessage
import com.example.util.DeviceSmsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * BottomSheet displaying all SMS messages received from a specific Bank SMS Sender.
 * Allows users to view the parsed transaction, select a category from an interactive
 * dropdown menu (منوی کشویی دسته‌بندی), and register it as an official transaction with 1 click.
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

    var messages by remember { mutableStateOf<List<DeviceSenderSmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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

    LaunchedEffect(sender.senderNumber) {
        isLoading = true
        val result = DeviceSmsReader.getMessagesForSender(context, sender.senderNumber)
        messages = result
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E212B),
        modifier = Modifier.fillMaxHeight(0.9f)
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
                        containerSize = 40.dp,
                        logoSize = 32.dp,
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
                            text = "سرشماره: ${sender.senderNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
                }
            }

            HorizontalDivider(color = Color(0xFF2E3446))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF5C82E6))
                        Spacer(Modifier.height(12.dp))
                        Text("در حال واکشی پیامک‌های این سرشماره...", color = Color.LightGray, fontSize = 13.sp)
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text(
                            text = "هیچ پیامکی از سرشماره ${sender.senderNumber} در صندوق یافت نشد.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = "تعداد ${messages.size} پیامک دریافت شد. می‌توانید دسته‌بندی را با منوی کشویی تغییر داده و ثبت کنید:",
                    color = Color(0xFFB0B7C3),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 10.dp)
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
                                    // Update bank card balance if available
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SenderSmsItemCard(
    message: DeviceSenderSmsMessage,
    sender: BankSmsSender,
    categoryNames: List<String>,
    bankAccounts: List<BankAccount>,
    isAlreadyRegistered: Boolean,
    onRegister: (Transaction) -> Unit
) {
    var isRegisteredLocal by remember { mutableStateOf(isAlreadyRegistered) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var expandedDetails by remember { mutableStateOf(false) }

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
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRegisteredLocal) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFF384055)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Date & Registration Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = PersianFormatters.formatDateTime(message.timestamp),
                    fontSize = 11.sp,
                    color = Color(0xFFA0A7B5)
                )

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

            // Raw SMS text accordion
            Surface(
                color = Color(0xFF1B202C),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedDetails = !expandedDetails }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (expandedDetails) "متن کامل پیامک:" else message.body.take(65) + if (message.body.length > 65) "..." else "",
                            color = Color(0xFFD1D5DB),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = if (expandedDetails) 10 else 1
                        )
                        Icon(
                            imageVector = if (expandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Register Transaction Button
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
