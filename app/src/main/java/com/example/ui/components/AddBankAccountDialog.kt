package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.util.BankBinHelper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AddBankAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        bankName: String,
        accountName: String,
        cardNumber: String,
        accountNumber: String?,
        balanceToman: Long,
        colorHex: String,
        smsShortcode: String?,
        accountType: String,
        readSmsEnabled: Boolean,
        initialBalanceEnabled: Boolean
    ) -> Unit
) {
    var accountType by remember { mutableStateOf("BANK_CARD") } // "BANK_CARD" or "OTHER_SOURCE"
    var cardNumber by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var readSmsEnabled by remember { mutableStateOf(true) }
    var initialBalanceEnabled by remember { mutableStateOf(true) }
    var balanceTextRial by remember { mutableStateOf("10000000") }
    var smsShortcode by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#1E88E5") }

    var showSmsPickerModal by remember { mutableStateOf(false) }

    // Auto Bank Name & Color Detection based on Iranian Bank Card 6-digit prefix
    val detectedBank: Pair<String, String> = remember(cardNumber) {
        val info = BankBinHelper.getBankInfoFromCardNumber(cardNumber)
        if (info != null) Pair(info.name, info.colorHex) else Pair("", "#1E88E5")
    }

    val bankName = detectedBank.first
    val detectedColor = detectedBank.second

    LaunchedEffect(detectedColor) {
        selectedColorHex = detectedColor
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .navigationBarsPadding()
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF222834),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Title and Close Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "افزودن حساب‌کتاب",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = Color.White
                            )
                        }
                    }

                    // Account Type Segment Switch
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "نوع حساب‌کتاب",
                            fontSize = 12.sp,
                            color = Color(0xFFB0BEC5)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181D26), RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option 1: Bank Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { accountType = "BANK_CARD" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (accountType == "BANK_CARD") Color(0xFF3B6FE8) else Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(
                                        "کارت بانکی",
                                        color = if (accountType == "BANK_CARD") Color.White else Color(0xFF8A9AAD),
                                        fontWeight = if (accountType == "BANK_CARD") FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            // Option 2: Cash / Wallet
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { accountType = "OTHER_SOURCE" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (accountType == "OTHER_SOURCE") Color(0xFF3B6FE8) else Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(
                                        "کیف پول / نقد",
                                        color = if (accountType == "OTHER_SOURCE") Color.White else Color(0xFF8A9AAD),
                                        fontWeight = if (accountType == "OTHER_SOURCE") FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Card Number Field
                    if (accountType == "BANK_CARD") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it.filter { c -> c.isDigit() }.take(16) },
                                label = { Text("شماره کارت (۱۶ رقم)") },
                                placeholder = { Text("مثال: 60379988...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_number_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF1A212D),
                                    unfocusedContainerColor = Color(0xFF1A212D),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3B6FE8),
                                    unfocusedBorderColor = Color(0xFF2E3848)
                                )
                            )

                            // Detected Bank Name Badge (Matching Screenshot 8)
                            if (bankName.isNotEmpty() && bankName != "بانک ناشناخته") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(android.graphics.Color.parseColor(selectedColorHex)).copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🏦", fontSize = 10.sp)
                                        }
                                    }
                                    Text(
                                        text = bankName,
                                        color = Color(0xFF90CAF9),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Card Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "نام کارت (اختیاری)", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                        OutlinedTextField(
                            value = accountName,
                            onValueChange = { accountName = it },
                            placeholder = { Text("کارت حقوق، حساب پس‌انداز و...", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A212D),
                                unfocusedContainerColor = Color(0xFF1A212D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B6FE8),
                                unfocusedBorderColor = Color(0xFF2E3848)
                            )
                        )
                        Text(
                            text = "اگر برای کارتت اسم بذاری، این نام رو به جای شماره کارت نمایش می‌دیم",
                            fontSize = 11.sp,
                            color = Color(0xFF8A9AAD)
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2E3848))

                    // Toggle 1: Read Bank SMS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("خواندن پیامک‌های بانک", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("دخل و خرج مستقیم از پیامک خوانده میشه", fontSize = 11.sp, color = Color(0xFF8A9AAD))
                        }
                        Switch(
                            checked = readSmsEnabled,
                            onCheckedChange = { readSmsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B6FE8)
                            )
                        )
                    }

                    if (readSmsEnabled) {
                        // SMS Shortcode Binding
                        OutlinedTextField(
                            value = smsShortcode,
                            onValueChange = { smsShortcode = it },
                            label = { Text("سرشماره پیامک این بانک") },
                            placeholder = { Text("مثال: 9830009417") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A212D),
                                unfocusedContainerColor = Color(0xFF1A212D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B6FE8),
                                unfocusedBorderColor = Color(0xFF2E3848)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showSmsPickerModal = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📩 انتخاب سرشماره از بین پیامک‌های گوشی")
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2E3848))

                    // Toggle 2: Initial Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("موجودی اولیه کارت", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Switch(
                            checked = initialBalanceEnabled,
                            onCheckedChange = { initialBalanceEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B6FE8)
                            )
                        )
                    }

                    if (initialBalanceEnabled) {
                        OutlinedTextField(
                            value = balanceTextRial,
                            onValueChange = { balanceTextRial = it.filter { c -> c.isDigit() } },
                            trailingIcon = { Text("ریال", color = Color(0xFF8A9AAD), modifier = Modifier.padding(end = 12.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A212D),
                                unfocusedContainerColor = Color(0xFF1A212D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B6FE8),
                                unfocusedBorderColor = Color(0xFF2E3848)
                            )
                        )
                    }
                } // End of Scrollable Column

                // Fixed Action Footer
                Surface(
                    tonalElevation = 3.dp,
                    color = Color(0xFF181D26),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3848))
                        ) {
                            Text("انصراف")
                        }
                        Button(
                            onClick = {
                                val finalBankName = if (bankName.isNotBlank() && bankName != "بانک ناشناخته") bankName else "بانک تخصصی"
                                val finalAccName = accountName.ifBlank { if (cardNumber.length >= 4) "کارت ${cardNumber.takeLast(4)}" else "حساب اصلی" }
                                val rialAmount = balanceTextRial.toLongOrNull() ?: 0L
                                val tomanAmount = if (initialBalanceEnabled) rialAmount / 10L else 0L

                                onConfirm(
                                    finalBankName,
                                    finalAccName,
                                    cardNumber.ifBlank { "6037-XXXX-XXXX-1111" },
                                    null,
                                    tomanAmount,
                                    selectedColorHex,
                                    smsShortcode.ifBlank { null },
                                    accountType,
                                    readSmsEnabled,
                                    initialBalanceEnabled
                                )
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("submit_bank_account_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5C82E6),
                                contentColor = Color.White
                            )
                        ) {
                            Text("ثبت و ذخیره کارت", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            } // End of outer Column
        } // End of Surface
    } // End of Dialog

    // Modal: Select Bank Sender from Device SMS (Screenshot 7)
    if (showSmsPickerModal) {
        SmsSenderPickerSheet(
            onDismiss = { showSmsPickerModal = false },
            onSenderSelected = { selectedAddress ->
                smsShortcode = selectedAddress
                showSmsPickerModal = false
            }
        )
    }
}
