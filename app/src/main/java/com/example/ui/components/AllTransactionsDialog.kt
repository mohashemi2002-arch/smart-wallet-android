package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Transaction
import com.example.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsDialog(
    transactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("مرداد ۱۴۰۵") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E2638)),
            color = Color(0xFF1E2638)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Toggle search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = Color(0xFFE6E1E5)
                        )
                    }

                    Text(
                        text = "همه‌ی تراکنش‌ها",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE6E1E5)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = Color(0xFFE6E1E5)
                        )
                    }
                }

                // Month Selector Pill Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF2C374D),
                        modifier = Modifier.clip(RoundedCornerShape(50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "قبلی",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedMonth,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "بعدی",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Main Grouped List Container Box
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF273246)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38496B))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Day 1: دیروز ، ۱۰ مرداد ۱۴۰۵
                        item {
                            DayTransactionGroup(
                                dateText = "دیروز ، ۱۰ مرداد ۱۴۰۵",
                                totalSumText = "+ ۲,۶۸۷,۴۶۱ ریال",
                                isPositive = true,
                                items = listOf(
                                    TransactionRowData("نقدی", "+ ۱۰,۱۶۰,۰۰۰ ریال", true, Color(0xFFFBC02D), Icons.Default.Payments),
                                    TransactionRowData("خوردنی", "- ۳,۲۸۹,۰۰۰ ریال", false, Color(0xFFFF7043), Icons.Default.Fastfood),
                                    TransactionRowData("عوارض آزادراهی", "- ۲,۴۸۳,۵۳۹ ریال", false, Color(0xFFE53935), Icons.Default.DirectionsCar),
                                    TransactionRowData("بنزین", "- ۷۵۰,۰۰۰ ریال", false, Color(0xFFE53935), Icons.Default.LocalGasStation),
                                    TransactionRowData("بنزین", "- ۹۵۰,۰۰۰ ریال", false, Color(0xFFE53935), Icons.Default.LocalGasStation)
                                )
                            )
                        }

                        // Day 2: پنج‌شنبه ، ۸ مرداد ۱۴۰۵
                        item {
                            DayTransactionGroup(
                                dateText = "پنج‌شنبه ، ۸ مرداد ۱۴۰۵",
                                totalSumText = "- ۱۱۸,۵۰۰,۰۰۰ ریال",
                                isPositive = false,
                                items = listOf(
                                    TransactionRowData("جابجایی\nاز 💳 به حساب 🟢", "۱۰,۱۶۰,۰۰۰ ریال", null, Color(0xFF42A5F5), Icons.Default.SwapHoriz),
                                    TransactionRowData("خانواده", "- ۶,۵۰۰,۰۰۰ ریال", false, Color(0xFFFF9800), Icons.Default.People),
                                    TransactionRowData("خانواده", "- ۸۰۰,۰۰۰ ریال", false, Color(0xFFFF9800), Icons.Default.People),
                                    TransactionRowData("خانواده", "- ۸۰۰,۰۰۰ ریال", false, Color(0xFFFF9800), Icons.Default.People),
                                    TransactionRowData("خانواده", "- ۷۶,۰۰۰,۰۰۰ ریال", false, Color(0xFFFF9800), Icons.Default.People),
                                    TransactionRowData("خودرو", "- ۲,۴۰۰,۰۰۰ ریال", false, Color(0xFF26A69A), Icons.Default.DirectionsCar)
                                )
                            )
                        }

                        // Day 3: چهارشنبه ، ۷ مرداد ۱۴۰۵
                        item {
                            DayTransactionGroup(
                                dateText = "چهارشنبه ، ۷ مرداد ۱۴۰۵",
                                totalSumText = "- ۸,۷۹۸,۰۰۰ ریال",
                                isPositive = false,
                                items = listOf(
                                    TransactionRowData("خانواده", "- ۱,۹۵۰,۰۰۰ ریال", false, Color(0xFFFF9800), Icons.Default.People),
                                    TransactionRowData("خوردنی", "- ۵,۰۵۰,۰۰۰ ریال", false, Color(0xFFFF7043), Icons.Default.Fastfood),
                                    TransactionRowData("خوردنی", "- ۸۰۰,۰۰۰ ریال", false, Color(0xFFFF7043), Icons.Default.Fastfood),
                                    TransactionRowData("بنزین", "- ۹۹۸,۰۰۰ ریال", false, Color(0xFFE53935), Icons.Default.LocalGasStation)
                                )
                            )
                        }

                        // Day 4: سه‌شنبه ، ۶ مرداد ۱۴۰۵
                        item {
                            DayTransactionGroup(
                                dateText = "سه‌شنبه ، ۶ مرداد ۱۴۰۵",
                                totalSumText = "- ۳,۰۰۰,۴۵۰ ریال",
                                isPositive = false,
                                items = listOf(
                                    TransactionRowData("شارژ ساختمان\nخونه", "- ۳,۰۰۰,۴۵۰ ریال", false, Color(0xFF5C6BC0), Icons.Default.HomeWork),
                                    TransactionRowData("جابجایی\nاز 🟢 به حساب 🟢", "۲۴۷,۵۳۰,۰۰۰ ریال", null, Color(0xFF42A5F5), Icons.Default.SwapHoriz)
                                )
                            )
                        }

                        // Day 5: دوشنبه ، ۵ مرداد ۱۴۰۵
                        item {
                            DayTransactionGroup(
                                dateText = "دوشنبه ، ۵ مرداد ۱۴۰۵",
                                totalSumText = "+ ۲۵۴,۹۰۰,۰۰۰ ریال",
                                isPositive = true,
                                items = listOf(
                                    TransactionRowData("6037 - 9915 - 4703 - 2338\nموجودی اولیه", "+ ۲۴۷,۶۰۰,۰۰۰ ریال", true, Color(0xFF1E88E5), Icons.Default.CreditCard),
                                    TransactionRowData("6063 - 7312 - 1460 - 0612\nموجودی اولیه", "+ ۷,۳۰۰,۰۰۰ ریال", true, Color(0xFF1E88E5), Icons.Default.CreditCard)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TransactionRowData(
    val title: String,
    val amountText: String,
    val isIncome: Boolean?, // null for transfer
    val iconBgColor: Color,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun DayTransactionGroup(
    dateText: String,
    totalSumText: String,
    isPositive: Boolean,
    items: List<TransactionRowData>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFCAC4D0)
            )

            Text(
                text = totalSumText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Color(0xFF81C784) else Color(0xFFE57373)
            )
        }

        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Amount & Date
                Column {
                    Text(
                        text = item.amountText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (item.isIncome) {
                            true -> Color(0xFF81C784)
                            false -> Color(0xFFE57373)
                            null -> Color(0xFFE6E1E5)
                        }
                    )
                }

                // Right: Icon & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFE6E1E5)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = item.iconBgColor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.iconVector,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF38496B).copy(alpha = 0.5f), thickness = 0.8.dp)
        }
    }
}
