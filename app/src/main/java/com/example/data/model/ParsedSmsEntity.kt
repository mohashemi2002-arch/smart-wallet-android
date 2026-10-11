package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SmsProcessingStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    IGNORED
}

@Entity(
    tableName = "parsed_sms_records",
    indices = [
        Index(value = ["rawSmsText"]),
        Index(value = ["status"]),
        Index(value = ["dateMillis"])
    ]
)
data class ParsedSmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsId: Long? = null,
    val senderAddress: String,
    val bankName: String,
    val rawSmsText: String,
    val amountToman: Long,
    val type: TransactionType,
    val cardNumber: String? = null,
    val balanceToman: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val transactionTimeText: String? = null,
    val categoryName: String = "متفرقه",
    val merchantName: String = "",
    val confidence: Float = 0.9f,
    val status: SmsProcessingStatus = SmsProcessingStatus.PENDING,
    val linkedTransactionId: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
