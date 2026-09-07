package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.BankLogoHelper

@Composable
fun BankLogoImage(
    bankName: String?,
    cardNumber: String? = null,
    modifier: Modifier = Modifier,
    containerSize: Dp = 56.dp,
    logoSize: Dp = 46.dp,
    containerColor: Color = Color.White
) {
    val context = LocalContext.current
    val resourceId = BankLogoHelper.getLogoResourceId(context, bankName, cardNumber)
    
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != null) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = bankName ?: "لوگوی بانک",
                modifier = Modifier
                    .size(logoSize)
                    .padding(4.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        } else {
            // Monogram fallback
            val brandColor = BankLogoHelper.getBrandColor(bankName, cardNumber)
            val initial = (bankName ?: "🏦").trim().take(2)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brandColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontSize = (containerSize.value * 0.35f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
