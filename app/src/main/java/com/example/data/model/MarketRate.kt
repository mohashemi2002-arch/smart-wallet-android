package com.example.data.model

enum class MarketCategory(val titleFa: String) {
    CURRENCY("ارزهای فیات"),
    GOLD_COIN("طلا و سکه"),
    CRYPTO("کریپتوکارنسی")
}

data class MarketRate(
    val id: String,
    val titleFa: String,
    val symbol: String,
    val priceToman: Long,
    val changePercent: Double, // positive or negative percentage, e.g., +1.4 or -0.8
    val highToman: Long,
    val lowToman: Long,
    val category: MarketCategory,
    val unit: String = "تومان",
    val lastUpdated: String
)
