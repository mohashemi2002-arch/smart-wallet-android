package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.FinanceRepository
import com.example.data.model.BankSmsSender
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSenderSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FinanceRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var senders by remember { mutableStateOf<List<BankSmsSender>>(emptyList()) }
    var showPickerBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.allBankSmsSenders.collectLatest { list ->
            senders = list
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showPickerBottomSheet = true
        } else {
            Toast.makeText(context, "دسترسی پیامک داده نشد", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت سرشماره‌های بانکی", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color(0xFFE6E1E5))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141218))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) -> {
                            showPickerBottomSheet = true
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
                        }
                    }
                },
                containerColor = Color(0xFFD0BCFF)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن سرشماره", tint = Color(0xFF381E72))
            }
        },
        containerColor = Color(0xFF141218)
    ) { innerPadding ->
        if (senders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    "هیچ سرشماره‌ای ثبت نشده است.\nاز دکمه + برای افزودن از صندوق پیامک استفاده کنید.",
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(senders) { sender ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(sender.bankName, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE6E1E5))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(sender.senderNumber, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCAC4D0))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = sender.isActive,
                                    onCheckedChange = { isActive ->
                                        coroutineScope.launch {
                                            repository.bankSmsSenderDao.update(sender.copy(isActive = isActive))
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF381E72),
                                        checkedTrackColor = Color(0xFFD0BCFF)
                                    )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = {
                                    coroutineScope.launch { repository.bankSmsSenderDao.delete(sender) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPickerBottomSheet) {
        SmsSenderPickerBottomSheet(
            onDismissRequest = { showPickerBottomSheet = false },
            onSenderSelected = { address, bankName ->
                coroutineScope.launch {
                    val newSender = BankSmsSender(bankName = bankName, senderNumber = address, isActive = true)
                    repository.bankSmsSenderDao.insert(newSender)
                    showPickerBottomSheet = false
                }
            }
        )
    }
}
