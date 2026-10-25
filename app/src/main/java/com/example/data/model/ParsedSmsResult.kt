package com.example.data.model

data class ParsedSmsResult(
    val amountToman: Long,
    val type: TransactionType,
    val merchantName: String,
    val categoryName: String,
    val cardNumber: String? = null,
    val accountNumber: String? = null,
    val bankName: String? = null,
    val rawText: String,
    val balanceToman: Long? = null,
    val transactionTime: String? = null,
    val confidence: Float = 0.9f,
    val parsedByAi: Boolean = true
)
