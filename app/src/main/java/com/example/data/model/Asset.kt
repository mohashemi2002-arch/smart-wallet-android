package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetType {
    GOLD,        // e.g. 18k Gold Grams
    CURRENCY,    // e.g. USD, EUR, GBP
    GOLD_COIN,   // e.g. Emami, Bahar Azadi, Nim Azadi, Rbe Azadi
    CRYPTO       // e.g. Bitcoin, Tether, Ethereum
}

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,             // e.g. "طلا ۱۸ عیار", "دلار آمریکا", "سکه امامی", "تتر"
    val type: AssetType,
    val quantity: Double,         // e.g. 12.5 (grams or coins)
    val currentPriceToman: Long,  // Unit price in Toman
    val unit: String,             // e.g. "گرم", "دلار", "عدد", "USDT"
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalValueToman: Long
        get() = (quantity * currentPriceToman).toLong()
}
