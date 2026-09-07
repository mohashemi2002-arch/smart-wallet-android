package com.example.data.model

data class BankSmsHistoryItem(
    val smsId: Long,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val bankName: String,
    val isRegistered: Boolean
)
