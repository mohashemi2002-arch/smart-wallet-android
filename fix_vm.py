import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

target = '''    data class DateFilter(val startMillis: Long, val endMillis: Long, val displayTitle: String)

    private val _selectedDateFilter = MutableStateFlow<DateFilter?>(null)
    val selectedDateFilter: StateFlow<DateFilter?> = _selectedDateFilter.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _selectedDateFilter.flatMapLatest { filter ->
        if (filter == null) {
            repository.confirmedTransactions
        } else {
            repository.getConfirmedTransactionsBetween(filter.startMillis, filter.endMillis)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateDateFilter(startMillis: Long, endMillis: Long, displayTitle: String) {
        _selectedDateFilter.value = DateFilter(startMillis, endMillis, displayTitle)
    }

    fun clearDateFilter() {
        _selectedDateFilter.value = null
    }'''

replacement = '''    sealed class DateFilter {
        data class SpecificDay(val jYear: Int, val jMonth: Int, val jDay: Int, val startMillis: Long, val endMillis: Long) : DateFilter()
        data class FullMonth(val jYear: Int, val jMonth: Int, val startMillis: Long, val endMillis: Long) : DateFilter()
    }

    private val _selectedDateFilter = MutableStateFlow<DateFilter?>(null)
    val selectedDateFilter: StateFlow<DateFilter?> = _selectedDateFilter.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _selectedDateFilter.flatMapLatest { filter ->
        if (filter == null) {
            repository.confirmedTransactions
        } else {
            val startMillis = when (filter) {
                is DateFilter.SpecificDay -> filter.startMillis
                is DateFilter.FullMonth -> filter.startMillis
            }
            val endMillis = when (filter) {
                is DateFilter.SpecificDay -> filter.endMillis
                is DateFilter.FullMonth -> filter.endMillis
            }
            repository.getConfirmedTransactionsInRange(startMillis, endMillis)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDateFilter(filter: DateFilter?) {
        _selectedDateFilter.value = filter
    }'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)

