package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.graphics.Color

object BankLogoHelper {

    fun getBankSlug(bankName: String?, cardNumber: String?): String {
        val detected = BankBinHelper.getBankInfoFromCardNumber(cardNumber ?: "")
        val name = "${detected?.name ?: ""} ${bankName ?: ""}"
        
        return when {
            name.contains("ملی") -> "melli"
            name.contains("صادرات") -> "saderat"
            name.contains("ملت") -> "mellat"
            name.contains("تجارت") -> "tejarat"
            name.contains("سپه") || name.contains("انصار") || name.contains("کوثر") || name.contains("قوامین") || name.contains("حکمت") -> "sepah"
            name.contains("کشاورزی") -> "keshavarzi"
            name.contains("مسکن") -> "maskan"
            name.contains("رفاه") -> "refah"
            name.contains("سامان") -> "saman"
            name.contains("بلو") || name.contains("Blu") || name.contains("blu") -> "blubank"
            name.contains("پاسارگاد") -> "pasargad"
            name.contains("پارسیان") -> "parsian"
            name.contains("شهر") -> "shahr"
            name.contains("آینده") -> "ayandeh"
            name.contains("دی") -> "dey"
            name.contains("سینا") -> "sina"
            name.contains("اقتصاد نوین") -> "en"
            name.contains("کارآفرین") -> "karafarin"
            name.contains("سرمایه") -> "sarmayeh"
            name.contains("گردشگری") -> "gardeshgari"
            name.contains("ایران زمین") -> "iz"
            name.contains("خاورمیانه") -> "khavarmiyaneh"
            name.contains("رسالت") -> "resalat"
            name.contains("مهر") -> "mehr"
            name.contains("پست") -> "post"
            name.contains("توسعه تعاون") -> "tt"
            name.contains("صنعت و معدن") -> "bim"
            name.contains("توسعه صادرات") -> "edbi"
            name.contains("مرکزی") -> "cbi"
            name.contains("ملل") -> "melal"
            name.contains("نور") -> "noor"
            name.contains("کاسپین") -> "caspian"
            else -> "generic"
        }
    }

    @SuppressLint("DiscouragedApi")
    fun getLogoResourceId(context: Context, bankName: String?, cardNumber: String?): Int? {
        val slug = getBankSlug(bankName, cardNumber)
        val resourceId = context.resources.getIdentifier(
            "ic_bank_$slug",
            "drawable",
            context.packageName
        )
        return if (resourceId != 0) resourceId else null
    }

    fun getBrandColor(bankName: String?, cardNumber: String?): Color {
        val bankInfo = BankBinHelper.getBankInfoFromCardNumber(cardNumber ?: "")
        val colorHex = bankInfo?.colorHex ?: "#1E88E5"
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF1E88E5)
        }
    }
}
