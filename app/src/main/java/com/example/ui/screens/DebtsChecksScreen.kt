package com.example.ui.screens

import com.example.ui.components.formatAsToman

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.*
import com.example.ui.viewmodel.ChequesViewModel
import com.example.ui.viewmodel.LoansViewModel
import com.example.ui.components.PersianFormatters
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsChecksScreen(
    onBack: () -> Unit,
    loansViewModel: LoansViewModel = viewModel(),
    chequesViewModel: ChequesViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var showAddChequeDialog by remember { mutableStateOf(false) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    
    val tabs = listOf("اقساط و وام", "چک‌های صیادی")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تعهدات مالی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (selectedTabIndex == 0) showAddLoanDialog = true else showAddChequeDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> LoansTab(loansViewModel)
                1 -> ChequesTab(chequesViewModel)
            }
        }
        
        if (showAddLoanDialog) {
            com.example.ui.components.AddLoanBottomSheet(
                onDismiss = { showAddLoanDialog = false },
                onConfirm = { title, total, count, amount ->
                    loansViewModel.addLoan(title, total, count, amount, System.currentTimeMillis() + 86400000L * 30L)
                    showAddLoanDialog = false
                    android.widget.Toast.makeText(ctx, "وام با موفقیت ثبت شد", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAddChequeDialog) {
            com.example.ui.components.AddChequeBottomSheet(
                onDismiss = { showAddChequeDialog = false },
                onConfirm = { sayad, type, amount, bank, party ->
                    chequesViewModel.addCheque(sayad, type, amount, System.currentTimeMillis() + 86400000L * 7L, bank, party, null)
                    showAddChequeDialog = false
                    android.widget.Toast.makeText(ctx, "چک با موفقیت ثبت شد", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun LoansTab(viewModel: LoansViewModel) {
    val loans by viewModel.loans.collectAsState()
    val installments by viewModel.pendingInstallments.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("اقساط نزدیک", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(installments.take(5)) { inst ->
            val ctx = androidx.compose.ui.platform.LocalContext.current
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("قسط شماره ${PersianFormatters.toPersianDigits(inst.installmentNumber.toString())}")
                        Text(
                            text = "سررسید: ${PersianFormatters.formatDate(inst.dueDateTimestamp)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${inst.amount.formatAsToman() + " تومان"}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val ctx = ctx
                        Button(onClick = { 
                            viewModel.payInstallment(inst.id) 
                            android.widget.Toast.makeText(ctx, "قسط پرداخت شد", android.widget.Toast.LENGTH_SHORT).show()
                        }, shape = RoundedCornerShape(8.dp)) {
                            Text("پرداخت")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("وام‌های فعال", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(loans) { loan ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(loan.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("کل مبلغ: ${loan.totalAmount.formatAsToman() + " تومان"}")
                    Text("مبلغ هر قسط: ${loan.installmentAmount.formatAsToman() + " تومان"}")
                    Text("تعداد اقساط: ${PersianFormatters.toPersianDigits(loan.installmentCount.toString())}")
                }
            }
        }
        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

@Composable
fun ChequesTab(viewModel: ChequesViewModel) {
    val cheques by viewModel.allCheques.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cheques) { cheque ->
            val ctx = androidx.compose.ui.platform.LocalContext.current
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (cheque.type == ChequeType.ISSUED) "چک صادره" else "چک دریافتی",
                            fontWeight = FontWeight.Bold,
                            color = if (cheque.type == ChequeType.ISSUED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        StatusBadge(cheque.status)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("شماره صیاد: ${PersianFormatters.toPersianDigits(cheque.sayadNumber)}")
                    Text("بانک: ${cheque.bankName} - در وجه: ${cheque.partyName}")
                    Text(
                        text = "مبلغ: ${cheque.amount.formatAsToman() + " تومان"}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("سررسید: ${PersianFormatters.formatDate(cheque.dueDateTimestamp)}")
                    
                    if (cheque.status == ChequeStatus.PENDING) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            val ctx = ctx
                            OutlinedButton(onClick = { 
                                viewModel.bounceCheque(cheque.id)
                                android.widget.Toast.makeText(ctx, "چک برگشت خورد", android.widget.Toast.LENGTH_SHORT).show()
                             }) {
                                Text("برگشت زدن")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { 
                                viewModel.clearCheque(cheque.id)
                                android.widget.Toast.makeText(ctx, "چک پاس شد", android.widget.Toast.LENGTH_SHORT).show()
                             }) {
                                Text("پاس شدن")
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

@Composable
fun StatusBadge(status: ChequeStatus) {
    val (color, text) = when (status) {
        ChequeStatus.PENDING -> Color(0xFFFFA000) to "در انتظار"
        ChequeStatus.CLEARED -> Color(0xFF388E3C) to "پاس شده"
        ChequeStatus.BOUNCED -> Color(0xFFD32F2F) to "برگشتی"
        ChequeStatus.TRANSFERRED -> Color(0xFF1976D2) to "خرج شده"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
