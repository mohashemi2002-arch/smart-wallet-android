package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReportFilterState(
    val dateRangeType: DateRangeType = DateRangeType.THIS_MONTH,
    val selectedTypes: Set<TransactionType> = setOf(TransactionType.INCOME, TransactionType.EXPENSE),
    val minAmount: Long? = null,
    val maxAmount: Long? = null
)

enum class DateRangeType {
    TODAY, THIS_WEEK, THIS_MONTH, LAST_3_MONTHS, ALL_TIME
}

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()

    private val _filterState = MutableStateFlow(ReportFilterState())
    val filterState: StateFlow<ReportFilterState> = _filterState

    private val allTransactions = transactionDao.getConfirmedTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(allTransactions, _filterState) { transactions, filters ->
        val now = System.currentTimeMillis()
        val currentJalali = com.example.util.JalaliCalendarHelper.getJalaliDateFromMillis(now)
        val startTime = when (filters.dateRangeType) {
            DateRangeType.TODAY -> {
                com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliDay(currentJalali.year, currentJalali.month, currentJalali.day).first
            }
            DateRangeType.THIS_WEEK -> {
                // Saturday is the first day of the Iranian week (شنبه)
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = now
                val dow = cal.get(java.util.Calendar.DAY_OF_WEEK) // Sunday=1, ..., Saturday=7
                val daysSinceSaturday = dow % 7
                val saturdayCal = java.util.Calendar.getInstance()
                saturdayCal.timeInMillis = now - (daysSinceSaturday * 24L * 60 * 60 * 1000)
                val satJalali = com.example.util.JalaliCalendarHelper.getJalaliDateFromMillis(saturdayCal.timeInMillis)
                com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliDay(satJalali.year, satJalali.month, satJalali.day).first
            }
            DateRangeType.THIS_MONTH -> {
                // Start of current Shamsi month (یکم ماه جاری)
                com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliMonth(currentJalali.year, currentJalali.month).first
            }
            DateRangeType.LAST_3_MONTHS -> {
                var startMonth = currentJalali.month - 2
                var startYear = currentJalali.year
                if (startMonth <= 0) {
                    startMonth += 12
                    startYear--
                }
                com.example.util.JalaliCalendarHelper.getStartAndEndOfJalaliMonth(startYear, startMonth).first
            }
            DateRangeType.ALL_TIME -> 0L
        }

        transactions.filter { tx ->
            val dateMatch = tx.dateMillis >= startTime
            val typeMatch = filters.selectedTypes.contains(tx.type)
            val minMatch = filters.minAmount == null || tx.amountToman >= filters.minAmount
            val maxMatch = filters.maxAmount == null || tx.amountToman <= filters.maxAmount
            dateMatch && typeMatch && minMatch && maxMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalInflow = combine(filteredTransactions, _filterState) { txList, _ ->
        txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalOutflow = combine(filteredTransactions, _filterState) { txList, _ ->
        txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val netBalance = combine(totalInflow, totalOutflow) { inflow, outflow ->
        inflow - outflow
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun updateDateRange(type: DateRangeType) {
        _filterState.value = _filterState.value.copy(dateRangeType = type)
    }

    fun toggleTypeFilter(type: TransactionType) {
        val current = _filterState.value.selectedTypes.toMutableSet()
        if (current.contains(type)) current.remove(type) else current.add(type)
        _filterState.value = _filterState.value.copy(selectedTypes = current)
    }

    fun setAmountRange(min: Long?, max: Long?) {
        _filterState.value = _filterState.value.copy(minAmount = min, maxAmount = max)
    }

    fun updateTransactionCategory(transactionId: Long, newCategoryName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val tx = transactionDao.getTransactionById(transactionId) ?: return@launch
            val categoryDao = db.categoryDao()
            val existing = categoryDao.getCategoryByName(newCategoryName)
            if (existing == null) {
                categoryDao.insert(com.example.data.model.Category(name = newCategoryName, colorHex = "#95A5A6"))
            }
            transactionDao.update(tx.copy(categoryName = newCategoryName))
        }
    }
}
