import org.jsoup.Jsoup

fun main() {
    try {
        val doc = Jsoup.connect("https://www.tgju.org/")
            .userAgent("Mozilla/5.0")
            .timeout(10000)
            .get()
        
        val rows = doc.select("tr[data-market-row]")
        for (row in rows) {
            val marketRow = row.attr("data-market-row")
            val priceAttr = row.attr("data-price")
            if (priceAttr.isEmpty()) continue
            
            val priceRial = priceAttr.replace(",", "").toLongOrNull() ?: 0L
            val priceToman = priceRial / 10
            
            val spans = row.select("td span")
            var changePercent = "۰٪"
            var isPositive = true
            
            for (span in spans) {
                val text = span.text()
                if (text.contains("%")) {
                    val match = Regex("""\((.*?%)\)""").find(text)
                    if (match != null) {
                        changePercent = match.groupValues[1]
                    }
                    isPositive = !span.hasClass("low")
                    break
                }
            }
            
            val th = row.selectFirst("th")?.text()?.trim() ?: ""
            println("$marketRow -> $th | $priceToman Toman | Change: $changePercent | Pos: $isPositive")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
