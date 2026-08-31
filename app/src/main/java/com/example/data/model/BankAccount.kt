package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,          // e.g., "بانک ملی", "بانک ملت", "بلوبانک", "بانک سامان"
    val accountName: String,       // e.g., "حساب اصلی", "کارت حقوق", "حساب پس‌انداز"
    val cardNumber: String,        // e.g., "6037-9978-1234-5678" or "6037****8910"
    val accountNumber: String? = null,
    val balanceToman: Long = 0L,
    val cardColorHex: String = "#1E88E5",
    val smsShortcode: String? = null,   // e.g., "B.QMEHRIRAN", "9830009417", "2000400"
    val accountType: String = "BANK_CARD", // "BANK_CARD" or "OTHER_SOURCE"
    val readSmsEnabled: Boolean = true,
    val initialBalanceEnabled: Boolean = true
)
