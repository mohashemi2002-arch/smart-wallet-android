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

    private val userAgent = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    suspend fun scrapeRates(): List<MarketRateEntity> = withContext(Dispatchers.IO) {
        val rates = mutableListOf<MarketRateEntity>()
        try {
            val request = Request.Builder()
                .url("https://www.navasan.net/")
                .header("User-Agent", userAgent)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val html = response.body?.string() ?: return@withContext emptyList()
            val doc = Jsoup.parse(html)
            
            // Typical Navasan table structure uses table body rows (tr)
            val rows = doc.select("tbody tr")
            
            val timestamp = System.currentTimeMillis()
            
            for (row in rows) {
                // Each row might have id or class we can use, but let's be robust
                // It usually has <td> elements
                val columns = row.select("td")
                if (columns.size >= 4) {
                    val title = columns[0].text().trim()
                    var priceText = columns[1].text().trim()
                    val changeText = columns[2].text().trim()
                    val changePercentText = columns[3].text().trim()
                    
                    val symbolCategory = determineSymbolAndCategory(title) ?: continue
                    val (symbol, category) = symbolCategory
                    
                    // Parse price - assume Rial, convert to Toman (divide by 10)
                    // If it's crypto, maybe it's already Toman. The prompt says convert if scraped in Rial.
                    // Actually, Navasan usually displays in Rial. 
                    // To be safe, let's extract digits.
                    val rawPrice = priceText.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                    
                    // Navasan shows Rial usually. Let's assume Rial and convert to Toman. 
                    // Gold globally could be in USD, but navasan shows in Rial/Toman for domestic.
                    val priceToman = if (rawPrice > 0) rawPrice / 10 else 0L

                    // Change percentage
                    val isPositive = changePercentText.contains("+") || (!changePercentText.contains("-") && changePercentText.isNotBlank())
                    val cleanPercent = if (changePercentText.isBlank() || changePercentText == "-") "۰٪" else changePercentText
                    
                    rates.add(
                        MarketRateEntity(
                            symbol = symbol,
                            name = title,
                            category = category,
                            priceToman = priceToman,
                            changePercent = cleanPercent,
                            isPositive = isPositive,
                            lastUpdatedTimestamp = timestamp
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext rates
    }

    private fun determineSymbolAndCategory(title: String): Pair<String, String>? {
        return when {
            title.contains("دلار آمریکا") || title == "دلار" -> "USD" to "CURRENCY"
            title.contains("یورو") -> "EUR" to "CURRENCY"
            title.contains("درهم امارات") || title == "درهم" -> "AED" to "CURRENCY"
            title.contains("پوند انگلیس") || title == "پوند" -> "GBP" to "CURRENCY"
            title.contains("لیر ترکیه") || title == "لیر" -> "TRY" to "CURRENCY"
            title.contains("تتر") -> "USDT" to "CRYPTO"
            title.contains("طلای ۱۸ عیار") || title.contains("طلا 18") || title == "گرم طلای 18 عیار" -> "GOLD_18K" to "GOLD"
            title.contains("طلای ۲۴ عیار") || title.contains("طلا 24") || title == "گرم طلای 24 عیار" -> "GOLD_24K" to "GOLD"
            title.contains("مثقال طلا") -> "GOLD_MITHQAL" to "GOLD"
            title.contains("سکه امامی") || title.contains("طرح جدید") -> "COIN_EMAMI" to "GOLD"
            title.contains("سکه بهار آزادی") || title.contains("طرح قدیم") -> "COIN_BAHAR" to "GOLD"
            title.contains("نیم سکه") -> "COIN_HALF" to "GOLD"
            title.contains("ربع سکه") -> "COIN_QUARTER" to "GOLD"
            title.contains("سکه گرمی") -> "COIN_GRAM" to "GOLD"
            title.contains("انس جهانی طلا") || title == "انس طلا" -> "GOLD_OUNCE" to "GOLD"
            else -> null
        }
    }
}
