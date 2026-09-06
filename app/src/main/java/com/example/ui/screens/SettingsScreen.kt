package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AutoLockTimeout
import com.example.data.CurrencyMode
import com.example.ui.components.SmartWalletPlusBadge
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSmsSenders: () -> Unit = {},
    financeViewModel: FinanceViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPremium by financeViewModel.isPremium.collectAsState()
    val preferences by settingsViewModel.userPreferences.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var exportDataText by remember { mutableStateOf("") }
    
    var showWipeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات سیستم", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = Color(0xFFE6E1E5))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141218))
            )
        },
        containerColor = Color(0xFF141218)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Subscription Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("وضعیت اشتراک", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE6E1E5))
                            if (isPremium) {
                                Spacer(Modifier.width(8.dp))
                                SmartWalletPlusBadge()
                            }
                        }
                        Button(
                            onClick = { financeViewModel.togglePremium() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                        ) {
                            Text(if (isPremium) "تغییر به سطح رایگان" else "ارتقا به پلاس", color = Color(0xFF381E72))
                        }
                    }
                }
            }


            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToSmsSenders() },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("سرشماره‌های پیامک بانکی", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE6E1E5))
                            Text("مدیریت سرشماره‌های بانکی و خواندن از پیامک‌ها", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCAC4D0))
                        }
                        Icon(Icons.Filled.Settings, contentDescription = "تنظیمات سرشماره‌ها", tint = Color(0xFFCAC4D0))
                    }
                }
            }
            // Security & Login
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("امنیت و ورود", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("ورود با اثر انگشت (Biometric)", color = Color.LightGray)
                            Switch(
                                checked = preferences.biometricEnabled,
                                onCheckedChange = { settingsViewModel.updateBiometricEnabled(it) }
                            )
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("مخفی کردن موجودی در شروع (Privacy)", color = Color.LightGray)
                            Switch(
                                checked = preferences.privacyModeEnabled,
                                onCheckedChange = { settingsViewModel.updatePrivacyModeEnabled(it) }
                            )
                        }

                        Text("زمان قفل خودکار", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = preferences.autoLockTimeout == AutoLockTimeout.IMMEDIATE,
                                onClick = { settingsViewModel.updateAutoLockTimeout(AutoLockTimeout.IMMEDIATE) },
                                label = { Text("فوری") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(4.dp))
                            FilterChip(
                                selected = preferences.autoLockTimeout == AutoLockTimeout.MIN_1,
                                onClick = { settingsViewModel.updateAutoLockTimeout(AutoLockTimeout.MIN_1) },
                                label = { Text("۱ دقیقه") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(4.dp))
                            FilterChip(
                                selected = preferences.autoLockTimeout == AutoLockTimeout.MIN_5,
                                onClick = { settingsViewModel.updateAutoLockTimeout(AutoLockTimeout.MIN_5) },
                                label = { Text("۵ دقیقه") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Display & Currency
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("واحد پول و نمایش", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = preferences.currencyMode == CurrencyMode.TOMAN,
                                onClick = { settingsViewModel.updateCurrencyMode(CurrencyMode.TOMAN) },
                                label = { Text("تومان") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = preferences.currencyMode == CurrencyMode.RIAL,
                                onClick = { settingsViewModel.updateCurrencyMode(CurrencyMode.RIAL) },
                                label = { Text("ریال") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Backup
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("پشتیبان‌گیری", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        OutlinedButton(
                            onClick = {
                                exportDataText = financeViewModel.generateExport()
                                showExportDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("تولید فایل CSV/Excel")
                        }
                        
                        val excelPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                Toast.makeText(context, "در حال پردازش و ورود اطلاعات اکسل...", Toast.LENGTH_LONG).show()
                                settingsViewModel.importSmartWalletExcel(it, context) { resultMsg ->
                                    Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { excelPickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7FA2FF))
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("ورود اطلاعات از اکسل اسمارت ولت (Import)", fontWeight = FontWeight.Bold)
                                Text("خواندن فایل‌های پشتیبان اکسل با پسوند xlsx", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // About & Reset
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("درباره و بازنشانی", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Button(
                            onClick = { showWipeDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("حذف کامل داده‌ها (Wipe Data)", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("گزارش خروجی مالی CSV") },
            text = {
                OutlinedTextField(
                    value = exportDataText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Smart Wallet Export", exportDataText))
                    Toast.makeText(context, "متن گزارش در کلیپ‌بورد کپی شد!", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) { Text("کپی در حافظه") }
            },
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("بستن") } }
        )
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("هشدار بسیار مهم", color = Color.Red) },
            text = { Text("آیا مطمئن هستید که می‌خواهید تمامی داده‌ها، تراکنش‌ها، وام‌ها، و چک‌های خود را برای همیشه حذف کنید؟ این عمل غیرقابل بازگشت است.") },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.wipeEntireDatabase {
                            Toast.makeText(context, "داده‌ها با موفقیت حذف شدند.", Toast.LENGTH_SHORT).show()
                            showWipeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("بله، پاک شود", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showWipeDialog = false }) { Text("انصراف") } }
        )
    }
}
