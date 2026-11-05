package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.data.AppTheme
import com.example.domain.util.AuthManager
import com.example.ui.auth.CloudRecoveryDialog
import com.example.ui.components.PersianCurrencyFormatter
import com.example.ui.viewmodel.SettingsViewModel
import com.example.updater.OtaReleaseInfo
import com.example.updater.OtaUpdateDialog
import com.example.updater.OtaUpdateManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRegistration: () -> Unit = {},
    onNavigateToSmsSenders: () -> Unit = {},
    onNavigateToGroupedSms: () -> Unit = {},
    onNavigateToFilteredSms: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsViewModel: SettingsViewModel = viewModel()
    val preferences by settingsViewModel.userPreferences.collectAsState()

    val authManager = remember { AuthManager(context) }
    val otaManager = remember { OtaUpdateManager(context) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCloudRecoveryDialog by remember { mutableStateOf(false) }
    var availableOtaRelease by remember { mutableStateOf<OtaReleaseInfo?>(null) }
    var isCheckingOta by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Cloud Recovery Dialog
    if (showCloudRecoveryDialog) {
        CloudRecoveryDialog(
            authManager = authManager,
            onDismiss = { showCloudRecoveryDialog = false },
            onRecoverySuccess = { msg ->
                showCloudRecoveryDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg.ifBlank { "بازیابی اطلاعات با موفقیت انجام شد" })
                }
            }
        )
    }

    // OTA Update Dialog
    if (availableOtaRelease != null) {
        OtaUpdateDialog(
            releaseInfo = availableOtaRelease!!,
            otaManager = otaManager,
            onDismiss = { availableOtaRelease = null }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editFirstName by remember {
            mutableStateOf(preferences.userFirstName.ifEmpty { authManager.savedFirstName })
        }
        var editLastName by remember {
            mutableStateOf(preferences.userLastName.ifEmpty { authManager.savedLastName })
        }
        var editPhone by remember {
            mutableStateOf(preferences.userPhoneNumber.ifEmpty { authManager.savedPhoneNumber })
        }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "ویرایش مشخصات کاربری",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it },
                        label = { Text("نام") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { editLastName = it },
                        label = { Text("نام خانوادگی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("شماره همراه") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.updateUserProfile(
                            firstName = editFirstName.trim(),
                            lastName = editLastName.trim(),
                            phoneNumber = editPhone.trim()
                        )
                        showEditProfileDialog = false
                        Toast.makeText(context, "اطلاعات با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBarSettings(onNavigateBack = onNavigateBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card (Purple Avatar Icon)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Distinctive Purple Person Icon Box as requested
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "پروفایل کاربر",
                                    tint = Color(0xFF8B5CF6), // Purple
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            val fullName = listOf(
                                preferences.userFirstName.ifEmpty { authManager.savedFirstName },
                                preferences.userLastName.ifEmpty { authManager.savedLastName }
                            ).filter { it.isNotBlank() }.joinToString(" ")

                            val phone = preferences.userPhoneNumber.ifEmpty { authManager.savedPhoneNumber }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = fullName.ifBlank { "کاربر گرامی" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (phone.isNotBlank()) PersianCurrencyFormatter.toPersianDigits(phone) else "شماره همراه ثبت نشده",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ویرایش")
                        }
                    }
                }
            }

            // Security & Biometric Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "امنیت و ورود",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val canUseBio = remember { authManager.canUseBiometric() }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ورود با اثر انگشت",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (canUseBio) {
                                        "حسگر بیومتریک دستگاه فعال است"
                                    } else {
                                        "حسگر اثر انگشت روی این دستگاه تعریف نشده است"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (canUseBio) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFEF5350)
                                )
                            }
                            Switch(
                                checked = preferences.biometricEnabled,
                                onCheckedChange = { enabled ->
                                    settingsViewModel.updateBiometricEnabled(enabled)
                                    authManager.isBiometricEnabled = enabled
                                    if (enabled && !canUseBio) {
                                        Toast.makeText(
                                            context,
                                            "توجه: لطفاً اثر انگشت خود را در تنظیمات امنیتی گوشی ثبت کنید",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Cloud Backup & Restore Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "پشتیبان‌گیری و فضای ابری",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "ذخیره‌سازی امن و بازیابی تراکنش‌ها، حساب‌ها و تنظیمات از طریق فضای ابری.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showCloudRecoveryDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بازیابی اطلاعات")
                            }

                            OutlinedButton(
                                onClick = {
                                    authManager.triggerBackgroundSync()
                                    Toast.makeText(
                                        context,
                                        "درخواست همگام‌سازی ابری ارسال شد",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("همگام‌سازی")
                            }
                        }
                    }
                }
            }

            // Software Updates (OTA) Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "بروزرسانی نرم‌افزار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "نسخه فعلی برنامه:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = {
                                    if (!isCheckingOta) {
                                        isCheckingOta = true
                                        coroutineScope.launch {
                                            try {
                                                val release = otaManager.checkForOtaUpdate()
                                                isCheckingOta = false
                                                if (release != null) {
                                                    availableOtaRelease = release
                                                } else {
                                                    snackbarHostState.showSnackbar("برنامه شما به آخرین نسخه بروز است")
                                                }
                                            } catch (e: Exception) {
                                                isCheckingOta = false
                                                snackbarHostState.showSnackbar("خطا در بررسی بروزرسانی: ${e.localizedMessage ?: "عدم اتصال"}")
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isCheckingOta
                            ) {
                                if (isCheckingOta) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("در حال بررسی...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("بررسی بروزرسانی")
                                }
                            }
                        }
                    }
                }
            }

            // SMS Tools & Diagnostic Card (moved away from Bank SMS menu)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "ابزارهای پیامک گوشی و سرشماره‌ها",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "مشاهده تمام پیامک‌های ورودی گوشی، فیلتر سرشماره‌ها و عیب‌یابی متن خام پیامک‌ها:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 1. Grouped SMS Viewer
                        OutlinedButton(
                            onClick = onNavigateToGroupedSms,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("صندوق پیامک‌های گوشی (گروه‌بندی بر اساس فرستنده)", modifier = Modifier.weight(1f), fontSize = 12.sp)
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // 2. Filtered Raw SMS Viewer
                        OutlinedButton(
                            onClick = onNavigateToFilteredSms,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("صحت‌سنجی و بررسی متن خام پیامک‌ها", modifier = Modifier.weight(1f), fontSize = 12.sp)
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // 3. Bank Senders Manager
                        OutlinedButton(
                            onClick = onNavigateToSmsSenders,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("مدیریت و ثبت سرشماره‌های بانکی", modifier = Modifier.weight(1f), fontSize = 12.sp)
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Appearance & Theme Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "ظاهر و پوسته",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(
                                AppTheme.SYSTEM to "سیستم",
                                AppTheme.LIGHT to "روشن",
                                AppTheme.DARK to "تاریک"
                            ).forEach { (theme, label) ->
                                FilterChip(
                                    selected = preferences.appTheme == theme,
                                    onClick = { settingsViewModel.updateAppTheme(theme) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // Language Card
            item {
                val isEn = preferences.appLanguage == com.example.data.AppLanguage.EN
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isEn) "Language / زبان برنامه" else "زبان برنامه (Language)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(
                                com.example.data.AppLanguage.FA to "فارسی (Persian)",
                                com.example.data.AppLanguage.EN to "English"
                            ).forEach { (lang, label) ->
                                FilterChip(
                                    selected = preferences.appLanguage == lang,
                                    onClick = { settingsViewModel.updateAppLanguage(lang) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // About Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "کیف پول هوشمند (پولکی)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مدیریت جامع امور مالی، پیامک‌های بانکی، چک‌ها و اقساط با امنیت بالا و همگام‌سازی ابری.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "نسخه v${BuildConfig.VERSION_NAME} (کد ساخت: ${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarSettings(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "تنظیمات",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
