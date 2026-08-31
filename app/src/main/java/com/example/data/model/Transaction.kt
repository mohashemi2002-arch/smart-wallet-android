package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountToman: Long,
    val type: TransactionType,
    val categoryName: String,
    val merchantName: String,
    val cardNumber: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val rawSmsText: String? = null,
    val isParsedFromSms: Boolean = false,
    val isApproved: Boolean = true,
    val notes: String? = null
)
