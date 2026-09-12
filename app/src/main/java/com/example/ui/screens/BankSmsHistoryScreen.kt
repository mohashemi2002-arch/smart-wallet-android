package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankSmsHistoryItem
import com.example.ui.components.BankLogoImage
import com.example.ui.components.PersianFormatters
import com.example.ui.viewmodel.BankSmsHistoryViewModel
import kotlinx.coroutines.launch

import com.example.ui.components.PendingReviewBottomSheet
import com.example.data.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSmsHistoryScreen(
    viewModel: BankSmsHistoryViewModel,
    onNavigateBack: () -> Unit,
    onManualIngest: (BankSmsHistoryItem) -> Unit
) {
    val smsList by viewModel.smsHistoryList.collectAsState()
    val banks by viewModel.registeredBanks.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val selectedBank by viewModel.selectedBankFilter.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scope = rememberCoroutineScope()
    var transactionToReview by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تاریخچه پیامک‌های بانکی", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshScan() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی پیامک‌ها")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2638),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF151921)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips
            if (banks.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(banks) { bank ->
                        FilterChip(
                            selected = selectedBank == bank,
                            onClick = { viewModel.selectedBankFilter.value = bank },
                            label = { Text(bank, color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5C82E6),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (isScanning && smsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5C82E6))
                }
            } else if (smsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "هیچ پیامکی از سرشماره‌های بانکی تعریف‌شده یافت نشد.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(smsList, key = { it.smsId }) { sms ->
                        BankSmsHistoryCard(
                            item = sms,
                            onIngestClick = {
                                scope.launch {
                                    val parsedTx = viewModel.parseSmsManual(sms)
                                    if (parsedTx != null) {
                                        transactionToReview = parsedTx
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    transactionToReview?.let { tx ->
        PendingReviewBottomSheet(
            transaction = tx,
            banks = bankAccounts,
            onDismiss = { transactionToReview = null },
            onConfirm = { updatedTx ->
                viewModel.insertTransaction(updatedTx)
                transactionToReview = null
            },
            onIgnore = {
                transactionToReview = null
            }
        )
    }
}

@Composable
fun BankSmsHistoryCard(
    item: BankSmsHistoryItem,
    onIngestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252E3E))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                        bankName = item.bankName,
                        cardNumber = null,
                        containerSize = 44.dp,
                        logoSize = 36.dp,
                        containerColor = Color.Transparent
                    )
                    Column {
                        Text(
                            text = item.bankName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = item.sender,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Text(
                    text = PersianFormatters.formatDateTime(item.timestamp),
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            // Body
            Surface(
                color = Color(0xFF1E2633),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.body,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 18.sp
                )
            }

            // Footer (Status & Actions)
            if (item.isRegistered) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                    Text("ثبت شده در تراکنش‌ها", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF44336).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                        Text("ثبت نشده / جامانده", color = Color(0xFFE57373), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onIngestClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ثبت این پیامک به عنوان تراکنش", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
