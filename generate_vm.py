import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

target = '''    sealed class DateFilter {
        data class SpecificDay(val jYear: Int, val jMonth: Int, val jDay: Int, val startMillis: Long, val endMillis: Long) : DateFilter()
        data class FullMonth(val jYear: Int, val jMonth: Int, val startMillis: Long, val endMillis: Long) : DateFilter()
    }

    private val _selectedDateFilter = MutableStateFlow<DateFilter?>(null)
    val selectedDateFilter: StateFlow<DateFilter?> = _selectedDateFilter.asStateFlow()

    val allTransactions: StateFlow<List<Transaction>> = repository.confirmedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

replacement = '''    sealed class TransactionDateFilter {
        data class MonthFilter(val jYear: Int, val jMonth: Int) : TransactionDateFilter()
        data class DayFilter(val jYear: Int, val jMonth: Int, val jDay: Int) : TransactionDateFilter()
        object AllTime : TransactionDateFilter()
    }

    private val _activeDateFilter = MutableStateFlow<TransactionDateFilter>(TransactionDateFilter.AllTime)
    val activeDateFilter: StateFlow<TransactionDateFilter> = _activeDateFilter.asStateFlow()

    val allTransactions: StateFlow<List<Transaction>> = repository.confirmedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _activeDateFilter.flatMapLatest { filter ->
        when (filter) {
            is TransactionDateFilter.AllTime -> repository.confirmedTransactions
            is TransactionDateFilter.MonthFilter -> {
                val (startMillis, endMillis) = com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliMonth(filter.jYear, filter.jMonth)
                repository.getConfirmedTransactionsInRange(startMillis, endMillis)
            }
            is TransactionDateFilter.DayFilter -> {
                val (startMillis, endMillis) = com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliDay(filter.jYear, filter.jMonth, filter.jDay)
                repository.getConfirmedTransactionsInRange(startMillis, endMillis)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Initialize with current month
    init {
        val currentJalali = com.example.util.JalaliCalendarHelper.getJalaliDateFromMillis(System.currentTimeMillis())
        _activeDateFilter.value = TransactionDateFilter.MonthFilter(currentJalali.year, currentJalali.month)
    }

    fun setFilter(filter: TransactionDateFilter) {
        _activeDateFilter.value = filter
    }

    fun nextPeriod() {
        when (val filter = _activeDateFilter.value) {
            is TransactionDateFilter.MonthFilter -> {
                var m = filter.jMonth + 1
                var y = filter.jYear
                if (m > 12) { m = 1; y++ }
                _activeDateFilter.value = TransactionDateFilter.MonthFilter(y, m)
            }
            is TransactionDateFilter.DayFilter -> {
                val (start, _) = com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliDay(filter.jYear, filter.jMonth, filter.jDay)
                val n = start + 86400000L
                val jd = com.example.util.JalaliCalendarHelper.getJalaliDateFromMillis(n)
                _activeDateFilter.value = TransactionDateFilter.DayFilter(jd.year, jd.month, jd.day)
            }
            TransactionDateFilter.AllTime -> {}
        }
    }

    fun previousPeriod() {
        when (val filter = _activeDateFilter.value) {
            is TransactionDateFilter.MonthFilter -> {
                var m = filter.jMonth - 1
                var y = filter.jYear
                if (m < 1) { m = 12; y-- }
                _activeDateFilter.value = TransactionDateFilter.MonthFilter(y, m)
            }
            is TransactionDateFilter.DayFilter -> {
                val (start, _) = com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliDay(filter.jYear, filter.jMonth, filter.jDay)
                val p = start - 86400000L
                val jd = com.example.util.JalaliCalendarHelper.getJalaliDateFromMillis(p)
                _activeDateFilter.value = TransactionDateFilter.DayFilter(jd.year, jd.month, jd.day)
            }
            TransactionDateFilter.AllTime -> {}
        }
    }'''

if target in content:
    content = content.replace(target, replacement)
else:
    print("TARGET NOT FOUND!")

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
