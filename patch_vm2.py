with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('import com.example.data.model.MarketRate\n', 'import com.example.data.model.MarketRateEntity\n')
content = content.replace('val marketRates: StateFlow<List<MarketRate>> = marketRepository.rates', 
                          'val marketRates: StateFlow<List<MarketRateEntity>> = marketRepository.getCachedRates().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())')
content = content.replace('val isRefreshingMarket: StateFlow<Boolean> = marketRepository.isRefreshing', 
                          'private val _isRefreshingMarket = MutableStateFlow(false)\n    val isRefreshingMarket: StateFlow<Boolean> = _isRefreshingMarket.asStateFlow()')
content = content.replace('val lastMarketUpdatedTime: StateFlow<String> = marketRepository.lastUpdatedTime', 
                          'val lastMarketUpdatedTime: StateFlow<String> = marketRates.map { rates -> if (rates.isEmpty()) "" else PersianFormatters.formatTimestampToPersian(rates.maxOfOrNull { it.lastUpdatedTimestamp } ?: 0) }.stateIn(viewModelScope, SharingStarted.Lazily, "")')

content = content.replace('marketRepository.fetchLiveRates()', 
"""_isRefreshingMarket.value = true
            val result = marketRepository.refreshRates()
            _isRefreshingMarket.value = false""")

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
