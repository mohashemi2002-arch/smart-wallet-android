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
    val purchaseUnitPriceToman: Long = 0L,
    val unit: String = "",
    val notes: String? = null,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    val totalValueToman: Long
        get() = (totalQuantity * currentUnitPriceToman).toLong()

    val totalInvestedToman: Long
        get() = (totalQuantity * purchaseUnitPriceToman).toLong()

    val unrealizedPnlToman: Long
        get() = totalValueToman - totalInvestedToman

    val unrealizedPnlPercent: Double
        get() = if (totalInvestedToman > 0) ((unrealizedPnlToman.toDouble() / totalInvestedToman.toDouble()) * 100.0) else 0.0
}
