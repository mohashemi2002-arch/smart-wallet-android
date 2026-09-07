package com.example.ui.components

import com.example.ui.components.formatAsToman

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccount

@Composable
fun BankCardItem(
    account: BankAccount,
    currencyUnit: CurrencyUnit,
    onDelete: (() -> Unit)? = null
) {
    val parseColor = try {
        Color(android.graphics.Color.parseColor(account.cardColorHex))
    } catch (e: Exception) {
        Color(0xFF1E88E5)
    }

    Card(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = parseColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.example.ui.components.BankLogoImage(
                            bankName = account.bankName,
                            cardNumber = account.cardNumber,
                            size = 42.dp,
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                        Column {
                            Text(
                                text = account.bankName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = account.accountName,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف کارت",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = account.cardNumber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Column {
                    Text(text = "موجودی فعلی:", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        text = account.balanceToman.formatAsToman() + if (currencyUnit == com.example.ui.components.CurrencyUnit.RIAL) " ریال" else " تومان",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
