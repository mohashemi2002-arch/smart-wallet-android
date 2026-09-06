package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.util.JalaliCalendarHelper
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class CurrencyUnit {
    TOMAN, // تومان
    RIAL   // ریال
}

object PersianFormatters {

    private val latinToPersianDigits = mapOf(
        '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
        '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
    )

    fun toPersianDigits(input: String): String {
        return input.map { latinToPersianDigits[it] ?: it }.joinToString("")
    }

    fun formatMoney(amountInToman: Long, unit: CurrencyUnit = CurrencyUnit.TOMAN): String {
        val displayAmount = if (unit == CurrencyUnit.RIAL) amountInToman * 10 else amountInToman
        val formatter = NumberFormat.getInstance(Locale.US)
        val formattedNumber = formatter.format(displayAmount)
        val persianNumber = toPersianDigits(formattedNumber)
        val suffix = if (unit == CurrencyUnit.RIAL) "ریال" else "تومان"
        return "$persianNumber $suffix"
    }

    fun formatDouble(value: Double): String {
        val formatted = String.format(Locale.US, "%.2f", value)
        return toPersianDigits(formatted)
    }

    fun formatDate(millis: Long): String {
        val d = JalaliCalendarHelper.getJalaliDateFromMillis(millis)
        return toPersianDigits("${d.dayOfWeekName}، ${d.day} ${d.monthName} ${d.year}")
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.US)
        return toPersianDigits(sdf.format(Date(millis)))
    }
}

@Composable
fun PoolakiPlusBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFF2B8B5),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "PLUS",
                tint = Color(0xFF601410),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "PLUS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = Color(0xFF601410)
            )
        }
    }
}
