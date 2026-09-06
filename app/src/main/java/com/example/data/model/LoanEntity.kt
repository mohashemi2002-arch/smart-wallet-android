package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalAmount: Long,
    val installmentCount: Int,
    val installmentAmount: Long,
    val sourceAccountId: Long? = null,
    val firstDueDateTimestamp: Long,
    val isCompleted: Boolean = false
)
