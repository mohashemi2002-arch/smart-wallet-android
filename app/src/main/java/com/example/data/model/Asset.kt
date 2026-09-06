package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetType {
    GOLD_18K, COIN_EMAMI, COIN_HALF, COIN_QUARTER, 
    CURRENCY_USD, CURRENCY_EUR, 
    CRYPTO_USDT, CRYPTO_BTC,
    CUSTOM
}

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: AssetType,
    val name: String,
    val totalQuantity: Double,
    val currentUnitPriceToman: Long,
    val unit: String = "", // Add unit for backward compatibility if needed, or remove
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    val totalValueToman: Long
        get() = (totalQuantity * currentUnitPriceToman).toLong()
}
