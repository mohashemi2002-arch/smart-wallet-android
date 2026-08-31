package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_sms_senders")
data class BankSmsSender(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,          // e.g., "بانک ملی", "بانک ملت", "بلوبانک", "بانک سامان"
    val senderNumber: String,      // e.g., "2000400", "30009411", "989000", "+9820004000"
    val isActive: Boolean = true
)
