package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CurrencyUnit
import com.example.ui.components.PoolakiPlusBadge
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val currencyUnit by viewModel.currencyUnit.collectAsState()
    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }
    var exportDataText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات و اشتراک پولکی", fontWeight = FontWeight.Bold, color = Color(0xFFE6E1E5)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Color(0xFFE6E1E5))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1B1F))
            )
        },
        containerColor = Color(0xFF1C1B1F)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Subscription & Freemium Tier Bento Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPremium) Color(0xFF381E72) else Color(0xFF2B2930)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD0BCFF))
                                Text(
                                    text = if (isPremium) "اشتراک فعال: پولکی پلاس" else "سطح رایگان (Free Tier)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE6E1E5)
                                )
                            }
                            if (isPremium) PoolakiPlusBadge()
                        }

                        Text(
                            text = if (isPremium)
                                "امکانات پلاس شامل: خواندن هوشمند SMS با Gemini AI، مدیریت سبد طلا و ارز، خروجی اکسل و پیش‌بینی هوشمند هزینه."
                            else
                                "در سطح رایگان ثبت دستی تراکنش‌ها و گزارش‌های پایه فعال است. پس از انقضای اشتراک تمامی داده‌های شما کاملاً دست‌نخورده باقی می‌مانند (Graceful Downgrade).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )

                        Button(
                            onClick = { viewModel.togglePremium() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("toggle_subscription_button"),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            )
                        ) {
                            Text(if (isPremium) "تغییر به سطح رایگان (شبیه‌سازی)" else "فعال‌سازی پولکی پلاس (ارتقا)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Currency Switcher Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "واحد پول پیش‌فرض",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE6E1E5)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = currencyUnit == CurrencyUnit.TOMAN,
                                onClick = { viewModel.setCurrencyUnit(CurrencyUnit.TOMAN) },
                                label = { Text("تومان (Toman)") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4A4458),
                                    selectedLabelColor = Color(0xFFE8DEF8),
                                    containerColor = Color(0xFF36343B),
                                    labelColor = Color(0xFFCAC4D0)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = currencyUnit == CurrencyUnit.TOMAN,
                                    borderColor = Color(0xFF49454F)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = currencyUnit == CurrencyUnit.RIAL,
                                onClick = { viewModel.setCurrencyUnit(CurrencyUnit.RIAL) },
                                label = { Text("ریال (Rial)") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4A4458),
                                    selectedLabelColor = Color(0xFFE8DEF8),
                                    containerColor = Color(0xFF36343B),
                                    labelColor = Color(0xFFCAC4D0)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = currencyUnit == CurrencyUnit.RIAL,
                                    borderColor = Color(0xFF49454F)
                                )
                            )
                        }
                    }
                }
            }

            // 3. Export Data Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "خروجی گرفتن و پشتیبان‌گیری (CSV / Excel)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE6E1E5)
                        )

                        Text(
                            text = "دریافت خروجی استاندار اکسل از تراکنش‌ها، سبد دارایی و تعهدات.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )

                        OutlinedButton(
                            onClick = {
                                exportDataText = viewModel.generateExport()
                                showExportDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_data_button"),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0BCFF))
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFFD0BCFF))
                            Spacer(Modifier.width(8.dp))
                            Text("تولید فایل CSV/Excel گزارش مالی", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Poolaki Export", exportDataText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "متن گزارش در کلیپ‌بورد کپی شد!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text("کپی در حافظه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }
}
