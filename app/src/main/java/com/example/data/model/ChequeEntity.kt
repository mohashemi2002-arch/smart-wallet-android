package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChequeType { ISSUED, RECEIVED }
enum class ChequeStatus { PENDING, CLEARED, BOUNCED, TRANSFERRED }

@Entity(tableName = "cheques")
data class ChequeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sayadNumber: String, // 16 digits
    val type: ChequeType,
    val amount: Long,
    val dueDateTimestamp: Long,
    val bankName: String,
    val partyName: String,
    val status: ChequeStatus = ChequeStatus.PENDING,
    val linkedAccountId: Long? = null,
    val notes: String? = null
)
