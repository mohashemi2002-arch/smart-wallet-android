package com.example.network

import com.example.data.model.MarketRateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class NavasanScraper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun scrapeRates(): List<MarketRateEntity> = withContext(Dispatchers.IO) {
        val rates = mutableListOf<MarketRateEntity>()
        try {
            val request = Request.Builder()
                .url("https://www.tgju.org/")
                .header("User-Agent", userAgent)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val html = response.body?.string() ?: return@withContext emptyList()
            val doc = Jsoup.parse(html)
            
            val rows = doc.select("tr[data-market-row]")
            val timestamp = System.currentTimeMillis()
            
            val addedSymbols = mutableSetOf<String>()

            for (row in rows) {
                val rowId = row.attr("data-market-row")
                val priceAttr = row.attr("data-price")
                if (priceAttr.isEmpty()) continue
                
                val symbolCategory = determineSymbolAndCategory(rowId) ?: continue
                val (symbol, category) = symbolCategory
                
                if (addedSymbols.contains(symbol)) continue
                
                val title = row.selectFirst("th")?.text()?.trim() ?: ""
                
                val rawPriceRial = priceAttr.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                val priceToman = if (rawPriceRial > 0) rawPriceRial / 10 else 0L
                
                var changePercentText = "۰٪"
                var isPositive = true
                
                val spans = row.select("td span")
                for (span in spans) {
                    val text = span.text()
                    if (text.contains("%")) {
                        val match = Regex("""\((.*?%)\)""").find(text)
                        if (match != null) {
                            changePercentText = match.groupValues[1].replace("-", "").replace("+", "")
                        }
                        isPositive = !span.hasClass("low")
                        break
                    }
                }

                rates.add(
                    MarketRateEntity(
                        symbol = symbol,
                        name = title,
                        category = category,
                        priceToman = priceToman,
                        changePercent = changePercentText,
                        isPositive = isPositive,
                        lastUpdatedTimestamp = timestamp
                    )
                )
                addedSymbols.add(symbol)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext rates
    }

    private fun determineSymbolAndCategory(rowId: String): Pair<String, String>? {
        return when (rowId) {
            "price_dollar_rl" -> "USD" to "CURRENCY"
            "price_eur" -> "EUR" to "CURRENCY"
            "price_aed" -> "AED" to "CURRENCY"
            "price_gbp" -> "GBP" to "CURRENCY"
            "price_try" -> "TRY" to "CURRENCY"
            "crypto-tether-irr", "crypto-tether" -> "USDT" to "CRYPTO"
            "geram18" -> "GOLD_18K" to "GOLD"
            "geram24" -> "GOLD_24K" to "GOLD"
            "mesghal" -> "GOLD_MITHQAL" to "GOLD"
            "sekee" -> "COIN_EMAMI" to "GOLD"
            "sekeb" -> "COIN_BAHAR" to "GOLD"
            "nim" -> "COIN_HALF" to "GOLD"
            "rob" -> "COIN_QUARTER" to "GOLD"
            "gerami" -> "COIN_GRAM" to "GOLD"
            "ons" -> "GOLD_OUNCE" to "GOLD"
            else -> null
        }
    }
}
