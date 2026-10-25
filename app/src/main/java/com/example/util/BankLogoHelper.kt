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
            name.contains("بلو") || name.contains("Blu") || name.contains("blu") -> "blubank"
            name.contains("سامان") -> "saman"
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

    @androidx.annotation.DrawableRes
    fun getLogoResourceId(bankName: String?, cardNumber: String?): Int? {
        val slug = getBankSlug(bankName, cardNumber)
        return when (slug) {
            "melli" -> com.example.R.drawable.ic_bank_melli
            "saderat" -> com.example.R.drawable.ic_bank_saderat
            "mellat" -> com.example.R.drawable.ic_bank_mellat
            "tejarat" -> com.example.R.drawable.ic_bank_tejarat
            "sepah" -> com.example.R.drawable.ic_bank_sepah
            "keshavarzi" -> com.example.R.drawable.ic_bank_keshavarzi
            "maskan" -> com.example.R.drawable.ic_bank_maskan
            "refah" -> com.example.R.drawable.ic_bank_refah
            "saman" -> com.example.R.drawable.ic_bank_saman
            "blubank" -> com.example.R.drawable.ic_bank_blubank
            "pasargad" -> com.example.R.drawable.ic_bank_pasargad
            "parsian" -> com.example.R.drawable.ic_bank_parsian
            "shahr" -> com.example.R.drawable.ic_bank_shahr
            "ayandeh" -> com.example.R.drawable.ic_bank_ayandeh
            "dey" -> com.example.R.drawable.ic_bank_dey
            "sina" -> com.example.R.drawable.ic_bank_sina
            "en" -> com.example.R.drawable.ic_bank_en
            "karafarin" -> com.example.R.drawable.ic_bank_karafarin
            "sarmayeh" -> com.example.R.drawable.ic_bank_sarmayeh
            "gardeshgari" -> com.example.R.drawable.ic_bank_gardeshgari
            "iz" -> com.example.R.drawable.ic_bank_iz
            "khavarmiyaneh" -> com.example.R.drawable.ic_bank_khavarmiyaneh
            "resalat" -> com.example.R.drawable.ic_bank_resalat
            "mehr" -> com.example.R.drawable.ic_bank_mehr
            "post" -> com.example.R.drawable.ic_bank_post
            "tt" -> com.example.R.drawable.ic_bank_tt
            "bim" -> com.example.R.drawable.ic_bank_bim
            "edbi" -> com.example.R.drawable.ic_bank_edbi
            "melal" -> com.example.R.drawable.ic_bank_melal
            "cbi" -> com.example.R.drawable.ic_bank_generic
            "noor" -> com.example.R.drawable.ic_bank_en
            "caspian" -> com.example.R.drawable.ic_bank_melal
            "generic" -> com.example.R.drawable.ic_bank_generic
            else -> com.example.R.drawable.ic_bank_generic
        }
    }

    @androidx.annotation.DrawableRes
    fun getLogoResourceId(context: Context, bankName: String?, cardNumber: String?): Int? {
        return getLogoResourceId(bankName, cardNumber)
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
