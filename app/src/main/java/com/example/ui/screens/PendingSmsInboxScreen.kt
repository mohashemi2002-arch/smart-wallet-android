package com.example.ui.screens

import androidx.compose.material.icons.filled.History
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.PersianFormatters
import com.example.ui.viewmodel.PendingSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSmsInboxScreen(
    viewModel: PendingSmsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("صندوق پیامک‌های بانکی", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (pendingTransactions.isNotEmpty()) {
                            Text(
                                "(${PersianFormatters.toPersianDigits(pendingTransactions.size.toString())} پیامک بررسی‌نشده)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "تاریخچه پیامک‌های بانکی")
                    }
                    if (pendingTransactions.isNotEmpty()) {
                        TextButton(onClick = { viewModel.approveAllPending(onComplete = {}, onError = {}) }) {
                            Text("تایید همه", color = Color(0xFF5C82E6), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E2633), titleContentColor = Color.White, actionIconContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF171E2B)
    ) { paddingValues ->
        if (pendingTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("همه پیامک‌ها بررسی شدند.\nصندوق خالی است.", textAlign = TextAlign.Center, color = Color.LightGray)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C82E6))
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاهده کل پیامک‌های بانکی")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingTransactions) { tx ->
                    PendingSmsCard(
                        transaction = tx,
                        bankAccounts = bankAccounts,
                        categories = categories,
                        onApprove = { newCard, newCat -> viewModel.approveTransaction(tx, newCard, newCat) },
                        onReject = { viewModel.rejectTransaction(tx) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSmsCard(
    transaction: Transaction,
    bankAccounts: List<com.example.data.model.BankAccount>,
    categories: List<com.example.data.model.Category>,
    onApprove: (String?, String) -> Unit,
    onReject: () -> Unit
) {
    var expandedRaw by remember { mutableStateOf(false) }
    
    // States for dropdowns
    var expandedAccount by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    
    var selectedCard by remember { mutableStateOf(transaction.cardNumber) }
    var selectedCategory by remember { mutableStateOf(transaction.categoryName.ifBlank { "متفرقه" }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252E3E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Bank Name & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.ui.components.BankLogoImage(
                        bankName = transaction.merchantName,
                        cardNumber = transaction.cardNumber,
                        containerSize = 44.dp,
                        logoSize = 38.dp,
                        containerColor = Color.Transparent
                    )
                    Surface(
                        color = Color(0xFF333E52),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = transaction.merchantName.ifBlank { "تراکنش بانکی" },
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = PersianFormatters.formatDate(transaction.dateMillis),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Amount and Type
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isIncome = transaction.type == TransactionType.INCOME
                Surface(
                    color = if (isIncome) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFF44336).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isIncome) "واریز" else "برداشت/انتقال",
                        color = if (isIncome) Color(0xFF81C784) else Color(0xFFE57373),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = PersianFormatters.formatMoney(transaction.amountToman, com.example.ui.components.CurrencyUnit.TOMAN),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Dropdowns
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                // Category Picker
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی", fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Account Picker
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = it },
                    modifier = Modifier.weight(1f)
                ) {
                    val displayCard = if (selectedCard?.startsWith("ID:") == true) {
                        val id = selectedCard?.removePrefix("ID:")?.toLongOrNull()
                        val acc = bankAccounts.find { it.id == id }
                        if (acc != null) "${acc.bankName} - ${acc.cardNumber.takeLast(4)}" else selectedCard
                    } else selectedCard

                    OutlinedTextField(
                        value = displayCard ?: "انتخاب کارت",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("کارت مرتبط", fontSize = 10.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false }
                    ) {
                        bankAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.bankName} - ${acc.cardNumber.takeLast(4)}") },
                                onClick = {
                                    selectedCard = "ID:${acc.id}"
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }
            }

            // Raw SMS Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expandedRaw = !expandedRaw }
            ) {
                Icon(
                    imageVector = if (expandedRaw) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF5C82E6)
                )
                Text("مشاهده متن اصلی پیامک", color = Color(0xFF5C82E6), fontSize = 12.sp)
            }
            AnimatedVisibility(visible = expandedRaw) {
                Text(
                    text = transaction.rawSmsText ?: "",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E2633), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onApprove(selectedCard, selectedCategory) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("تایید و ثبت")
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("نادیده گرفتن")
                }
            }
        }
    }
}
