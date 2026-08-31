package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtType {
    LOAN_INSTALLMENT, // قسط وام
    SAYAD_CHECK,      // چک صیادی
    DEBT_OWED,        // بدهی پرداختی
    RECEIVABLE_DUE    // طلب دریافتی
}

@Entity(tableName = "debt_loans")
data class DebtLoan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: DebtType,
    val amountToman: Long,
    val dueDateMillis: Long,
    val isPaid: Boolean = false,
    val contactName: String? = null,
    val notes: String? = null
)
