with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

target = 'val lastMarketUpdatedTime: StateFlow<String> = marketRates.map { rates -> if (rates.isEmpty()) "" else PersianFormatters.formatDateTime(rates.map { it.lastUpdatedTimestamp }.maxOrNull() ?: 0) }.stateIn(viewModelScope, SharingStarted.Lazily, "")'
replacement = 'val lastMarketUpdatedTime: StateFlow<String> = marketRates.map { rates -> rates.firstOrNull()?.lastUpdated ?: "" }.stateIn(viewModelScope, SharingStarted.Lazily, "")'

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
