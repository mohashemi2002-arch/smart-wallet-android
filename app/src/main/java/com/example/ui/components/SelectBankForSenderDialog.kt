package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val IRANIAN_MAJOR_BANKS = listOf(
    "بلوبانک",
    "بانک ملت",
    "بانک ملی ایران",
    "بانک صادرات ایران",
    "بانک سپه",
    "بانک تجارت",
    "بانک پاسارگاد",
    "بانک سامان",
    "بانک قرض‌الحسنه مهر ایران",
    "بانک قرض‌الحسنه رسالت",
    "بانک پارسیان",
    "بانک کشاورزی",
    "بانک مسکن",
    "بانک شهر",
    "بانک آینده",
    "بانک رفاه کارگران",
    "بانک سینا",
    "بانک اقتصاد نوین",
    "بانک کارآفرین",
    "بانک سرمایه",
    "بانک گردشگری",
    "بانک ایران زمین",
    "پست بانک ایران",
    "بانک دی",
    "بانک خاورمیانه",
    "بانک توسعه تعاون",
    "بانک صنعت و معدن",
    "بانک توسعه صادرات",
    "مؤسسه اعتباری ملل",
    "مؤسسه اعتباری نور"
)

/**
 * Interactive Dialog to let the user pick which Iranian Bank corresponds to a sender number/address,
 * with real bank logo preview, live search filter, and instant confirmation.
 */
@Composable
fun SelectBankForSenderDialog(
    senderNumber: String,
    initialBankName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (selectedBankName: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBank by remember {
        mutableStateOf(
            if (initialBankName.isNotBlank() && initialBankName != "بانک مربوطه") initialBankName
            else IRANIAN_MAJOR_BANKS.first()
        )
    }

    val filteredBanks = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) IRANIAN_MAJOR_BANKS
        else IRANIAN_MAJOR_BANKS.filter { it.contains(q, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222938),
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "انتخاب بانک مربوط به سرشماره",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "سرشماره: $senderNumber",
                    fontSize = 12.sp,
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "مشخص کنید پیامک‌های این سرشماره به حساب کدام بانک متصل شوند:",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجوی نام بانک...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = Color(0xFF94A3B8))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredBanks, key = { it }) { bank ->
                        val isSelected = bank == selectedBank

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBank = bank },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF2D3748),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else Color(0xFF475569)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BankLogoImage(
                                        bankName = bank,
                                        cardNumber = null,
                                        containerSize = 32.dp,
                                        logoSize = 24.dp,
                                        containerColor = Color.White
                                    )
                                    Text(
                                        text = bank,
                                        color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "انتخاب شده",
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (filteredBanks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "بانکی با این عنوان یافت نشد",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedBank) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تایید و انتساب به $selectedBank", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color(0xFF94A3B8))
            }
        }
    )
}
