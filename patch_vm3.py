with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

target1 = "import com.example.data.model.MarketRateEntity\n"
replacement1 = "import com.example.data.model.MarketRateEntity\nimport com.example.data.model.MarketRate\nimport com.example.data.model.MarketCategory\n"

target2 = "val marketRates: StateFlow<List<MarketRateEntity>> = marketRepository.getCachedRates().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())"
replacement2 = """val marketRates: StateFlow<List<MarketRate>> = marketRepository.getCachedRates().map { entities ->
        entities.map { entity ->
            val cat = when (entity.category) {
                "CURRENCY" -> MarketCategory.CURRENCY
                "GOLD" -> MarketCategory.GOLD_COIN
                "CRYPTO" -> MarketCategory.CRYPTO
                else -> MarketCategory.CURRENCY
            }
            val unitStr = if (entity.category == "GOLD" && entity.symbol.contains("18K")) "تومان / گرم" else "تومان"
            val changeD = entity.changePercent.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
            val finalChange = if (entity.isPositive) changeD else -changeD
            
            MarketRate(
                id = entity.symbol,
                titleFa = entity.name,
                symbol = entity.symbol,
                priceToman = entity.priceToman,
                changePercent = finalChange,
                highToman = 0L,
                lowToman = 0L,
                category = cat,
                unit = unitStr,
                lastUpdated = PersianFormatters.formatTimestampToPersian(entity.lastUpdatedTimestamp)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
