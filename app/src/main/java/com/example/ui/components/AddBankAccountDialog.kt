package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

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
    val detectedBank = remember(cardNumber) {
        detectIranianBank(cardNumber)
    }

    val bankName = detectedBank.first
    val detectedColor = detectedBank.second

    LaunchedEffect(detectedColor) {
        selectedColorHex = detectedColor
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF222834),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("کارت بانکی", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Option 2: Other Source
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { accountType = "OTHER_SOURCE" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (accountType == "OTHER_SOURCE") Color(0xFF3B6FE8) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("منبع دیگر", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (accountType == "BANK_CARD") {
                    // Card Number Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "شماره کارت ۱۶ رقمی", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it.take(19) },
                            placeholder = { Text("۶۰۳۷ ۹۹۷۲ ۰۵۶۵ ۲۵۳۸", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_number_input"),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = smsShortcode,
                            onValueChange = { smsShortcode = it },
                            label = { Text("سرشماره پیامکی (مثلاً B.QMEHRIRAN)") },
                            modifier = Modifier.weight(1f),
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

                        IconButton(
                            onClick = { showSmsPickerModal = true },
                            modifier = Modifier.background(Color(0xFF1A212D), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = "انتخاب سرشماره", tint = Color(0xFF90CAF9))
                        }
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

                // Confirm / Submit Button
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
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_bank_account_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C82E6),
                        contentColor = Color.White
                    )
                ) {
                    Text("ثبت  >", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // Modal: Select Bank Sender from Device SMS (Screenshot 7)
    if (showSmsPickerModal) {
        Dialog(onDismissRequest = { showSmsPickerModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF222834)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "انتخاب سرشماره پیامکی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        IconButton(onClick = { showSmsPickerModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
                        }
                    }

                    Text(
                        text = "شماره‌ای رو انتخاب کن که ازش پیامک «واریز» و «برداشت» دریافت می‌کنی",
                        fontSize = 12.sp,
                        color = Color(0xFF8A9AAD)
                    )

                    val sampleSenders = listOf(
                        Pair("B.QMEHRIRAN", "بانک قرض‌الحسنه مهر ایران - واریز/برداشت"),
                        Pair("9830009417", "بانک ملی ایران - سامانه پیامکی ورود و تراکنش"),
                        Pair("Bank Melli", "بانک ملی ایران - بام"),
                        Pair("20008511", "بانک سامان / بلوبانک"),
                        Pair("30009411", "بانک ملت - تراکنش‌ها"),
                        Pair("200082", "بانک پاسارگاد")
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(sampleSenders) { (code, name) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        smsShortcode = code
                                        showSmsPickerModal = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A212D)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF3B6FE8).copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF90CAF9))
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(code, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(name, fontSize = 11.sp, color = Color(0xFF8A9AAD))
                                    }
                                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF8A9AAD), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun detectIranianBank(cardNumber: String): Pair<String, String> {
    val clean = cardNumber.replace("-", "").replace(" ", "")
    if (clean.length < 4) return Pair("", "#1E88E5")

    val prefix = clean.take(4)
    return when (prefix) {
        "6037" -> Pair("بانک ملی ایران", "#1E88E5")
        "6104" -> Pair("بانک ملت", "#D32F2F")
        "6221" -> Pair("بانک پارسیان", "#7B1FA2")
        "6219" -> Pair("بلوبانک (سامان)", "#0288D1")
        "5022" -> Pair("بانک پاسارگاد", "#F57C00")
        "6274" -> Pair("بانک کشاورزی", "#388E3C")
        "5892" -> Pair("بانک سپه", "#3F51B5")
        "6039" -> Pair("بانک رفاه کارگران", "#00897B")
        "6273" -> Pair("بانک تجارت", "#1565C0")
        "6393" -> Pair("بانک سینا", "#E65100")
        "6362" -> Pair("بانک آینده", "#8D6E63")
        "5041" -> Pair("بانک رسالت", "#00ACC1")
        else -> Pair("بانک متصل", "#1E88E5")
    }
}
