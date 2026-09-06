package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.util.SmsInboxReader
import com.example.util.SmsSenderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.util.BankBinHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSenderPickerBottomSheet(
    onDismissRequest: () -> Unit,
    onSenderSelected: (String, String) -> Unit
) {
    val context = LocalContext.current
    var senders by remember { mutableStateOf<List<SmsSenderItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSenderAddress by remember { mutableStateOf<String?>(null) }

    val banks = BankBinHelper.getAllUniqueBanks()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val items = SmsInboxReader.getUniqueSenders(context)
            senders = items
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF141218),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                if (selectedSenderAddress == null) "انتخاب سرشماره از پیامک‌ها" else "این سرشماره متعلق به کدام بانک است؟",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFE6E1E5),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (selectedSenderAddress == null) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجوی شماره یا متن...") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color(0xFF49454F),
                        focusedTextColor = Color(0xFFE6E1E5),
                        unfocusedTextColor = Color(0xFFE6E1E5)
                    ),
                    singleLine = true
                )

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFD0BCFF), modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
                } else {
                    val filteredSenders = senders.filter {
                        it.address.contains(searchQuery) || it.latestSnippet.contains(searchQuery)
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filteredSenders) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedSenderAddress = item.address },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.address, style = MaterialTheme.typography.titleMedium, color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                                        Text("${item.count} پیامک", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCAC4D0))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        item.latestSnippet,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFE6E1E5),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(32.dp)) }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(banks) { bank ->
                        Text(
                            text = bank,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSenderSelected(selectedSenderAddress!!, bank) }
                                .padding(vertical = 16.dp),
                            color = Color(0xFFE6E1E5),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(color = Color(0xFF49454F))
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedSenderAddress = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF49454F))
                        ) {
                            Text("بازگشت به لیست پیامک‌ها", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
