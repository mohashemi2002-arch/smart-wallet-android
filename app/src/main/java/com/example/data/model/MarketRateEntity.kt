package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_rates")
data class MarketRateEntity(
    @PrimaryKey val symbol: String,       // e.g. "USD", "EUR", "GOLD_18K", "COIN_EMAMI"
    val name: String,                    // e.g. "دلار آمریکا"
    val category: String,                // "CURRENCY", "GOLD", "CRYPTO"
    val priceToman: Long,
    val changePercent: String,
    val isPositive: Boolean,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
