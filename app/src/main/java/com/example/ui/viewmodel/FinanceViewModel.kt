package com.example.ui.viewmodel
import com.example.ui.components.PersianFormatters
import kotlinx.coroutines.flow.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Exporter
import com.example.data.FinanceRepository
import com.example.data.MarketRepository
import com.example.data.SmsAiParser
import com.example.data.model.*
import com.example.data.database.AppDatabase
import com.example.ui.components.CurrencyUnit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository.getInstance(application)
    private val database = AppDatabase.getDatabase(application)
    private val marketRepository = MarketRepository.getInstance(database.marketRateDao(), database.assetDao())

    val marketRates: StateFlow<List<MarketRate>> = marketRepository.getCachedRates().map { entities ->
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
                lastUpdated = PersianFormatters.formatDateTime(entity.lastUpdatedTimestamp)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val _isRefreshingMarket = MutableStateFlow(false)
    val isRefreshingMarket: StateFlow<Boolean> = _isRefreshingMarket.asStateFlow()
    val lastMarketUpdatedTime: StateFlow<String> = marketRates.map { rates -> rates.firstOrNull()?.lastUpdated ?: "" }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    init {
        // Initial fetch of market rates
        refreshMarketRates()
    }

    fun refreshMarketRates() {
        viewModelScope.launch {
            _isRefreshingMarket.value = true
            val result = marketRepository.refreshRates()
            _isRefreshingMarket.value = false
        }
    }

    sealed class TransactionDateFilter {
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
    }

    val pendingTransactions: StateFlow<List<Transaction>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<Asset>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parsedSmsRecords: StateFlow<List<ParsedSmsEntity>> = repository.allParsedSms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccount>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankSmsSenders: StateFlow<List<BankSmsSender>> = repository.allBankSmsSenders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Preferences / Settings State
    private val _isPremium = MutableStateFlow(true) // Smart Wallet Plus Tier
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _currencyUnit = MutableStateFlow(CurrencyUnit.TOMAN)
    val currencyUnit: StateFlow<CurrencyUnit> = _currencyUnit.asStateFlow()

    // SMS Playground State
    private val _smsInputText = MutableStateFlow("")
    val smsInputText: StateFlow<String> = _smsInputText.asStateFlow()

    private val _parsedSmsState = MutableStateFlow<ParsedSmsResult?>(null)
    val parsedSmsState: StateFlow<ParsedSmsResult?> = _parsedSmsState.asStateFlow()

    private val _isParsingSms = MutableStateFlow(false)
    val isParsingSms: StateFlow<Boolean> = _isParsingSms.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Derived Statistics
    val totalAssetsValueToman: StateFlow<Long> = assets.map { list ->
        list.sumOf { it.totalValueToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalIncomeToman: StateFlow<Long> = transactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amountToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalExpenseToman: StateFlow<Long> = transactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalBankBalances: StateFlow<Long> = bankAccounts.map { list ->
        list.sumOf { it.balanceToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalNetWorthToman: StateFlow<Long> = combine(totalAssetsValueToman, totalBankBalances) { assetsVal, bankBal ->
        assetsVal + bankBal
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setSmsInputText(text: String) {
        _smsInputText.value = text
    }

    fun parseSmsInput() {
        val text = _smsInputText.value
        if (text.isBlank()) return

        viewModelScope.launch {
            _isParsingSms.value = true
            val result = SmsAiParser.parseSms(text)
            _parsedSmsState.value = result
            _isParsingSms.value = false
        }
    }

    fun confirmParsedSmsTransaction() {
        val parsed = _parsedSmsState.value ?: return
        viewModelScope.launch {
            val transaction = Transaction(
                amountToman = parsed.amountToman,
                type = parsed.type,
                categoryName = parsed.categoryName,
                merchantName = parsed.merchantName,
                cardNumber = parsed.cardNumber,
                dateMillis = System.currentTimeMillis(),
                rawSmsText = parsed.rawText,
                isParsedFromSms = true,
                status = TransactionStatus.CONFIRMED,
                bankName = parsed.bankName,
                balanceAfterToman = parsed.balanceToman
            )
            repository.addTransaction(transaction)

            // Also persist parsed SMS entity
            val smsEntity = ParsedSmsEntity(
                senderAddress = "",
                bankName = parsed.bankName ?: parsed.merchantName.ifBlank { "بانک" },
                rawSmsText = parsed.rawText,
                amountToman = parsed.amountToman,
                type = parsed.type,
                cardNumber = parsed.cardNumber,
                balanceToman = parsed.balanceToman,
                dateMillis = System.currentTimeMillis(),
                transactionTimeText = parsed.transactionTime,
                categoryName = parsed.categoryName,
                merchantName = parsed.merchantName,
                confidence = parsed.confidence,
                status = SmsProcessingStatus.CONFIRMED
            )
            repository.saveParsedSms(smsEntity)

            _parsedSmsState.value = null
            _smsInputText.value = ""
            _statusMessage.value = "تراکنش بانکی با موفقیت ثبت شد!"
        }
    }

    fun addManualTransaction(
        amountToman: Long,
        type: TransactionType,
        categoryName: String,
        merchantName: String,
        cardNumber: String? = null
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                amountToman = amountToman,
                type = type,
                categoryName = categoryName,
                merchantName = merchantName,
                cardNumber = cardNumber,
                dateMillis = System.currentTimeMillis(),
                isParsedFromSms = false
            )
            repository.addTransaction(transaction)
            _statusMessage.value = "تراکنش دستی ثبت گردید."
        }
    }

    fun addOrUpdateTransaction(
        id: Long = 0L,
        amountToman: Long,
        type: TransactionType,
        categoryName: String,
        merchantName: String,
        sourceAccount: BankAccount? = null,
        destinationAccount: BankAccount? = null,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            if (categoryName == "جابجایی" && sourceAccount != null && destinationAccount != null) {
                repository.performAccountTransfer(
                    sourceAccountId = sourceAccount.id,
                    destinationAccountId = destinationAccount.id,
                    amountToman = amountToman,
                    dateMillis = dateMillis,
                    notes = merchantName
                )
                _statusMessage.value = "جابجایی بین‌بانکی با موفقیت ثبت شد."
            } else {
                val tx = Transaction(
                    id = id,
                    amountToman = amountToman,
                    type = type,
                    categoryName = categoryName,
                    merchantName = merchantName,
                    cardNumber = sourceAccount?.cardNumber,
                    bankName = sourceAccount?.bankName,
                    dateMillis = dateMillis,
                    isParsedFromSms = false,
                    status = TransactionStatus.CONFIRMED,
                    notes = merchantName
                )
                if (id > 0L) {
                    repository.updateTransaction(tx)
                    _statusMessage.value = "تراکنش با موفقیت ویرایش شد."
                } else {
                    repository.addTransaction(tx)
                    _statusMessage.value = "تراکنش دستی با موفقیت ثبت شد."
                }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun approvePendingTransaction(transaction: Transaction) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.approvePendingTransaction(transaction)
                _statusMessage.value = "تراکنش پیامکی تایید و در موجودی کارت اعمال شد."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error approving pending transaction", e)
                _statusMessage.value = "خطا در تایید تراکنش پیامکی"
            }
        }
    }

    fun approvePendingTransactionWithCategory(transaction: Transaction, newCategoryName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.approvePendingTransactionWithCategory(transaction, newCategoryName)
                _statusMessage.value = "تراکنش با دسته «$newCategoryName» تایید شد."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error approving pending transaction with category", e)
                _statusMessage.value = "خطا در تایید تراکنش"
            }
        }
    }

    fun approveAllPendingTransactions() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.approveAllPendingTransactions()
                _statusMessage.value = "تمام تراکنش‌های در انتظار با موفقیت تایید و ثبت شدند."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error approving all pending transactions", e)
                _statusMessage.value = "خطا در تایید دسته‌جمعی تراکنش‌ها"
            }
        }
    }

    fun rejectPendingTransaction(transaction: Transaction) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.rejectPendingTransaction(transaction)
                _statusMessage.value = "تراکنش پیامکی رد و حذف شد."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error rejecting pending transaction", e)
            }
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.clearAllTransactions()
                _statusMessage.value = "تمام تراکنش‌ها با موفقیت پاکسازی شدند."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error clearing all transactions", e)
                _statusMessage.value = "خطا در پاکسازی تراکنش‌ها"
            }
        }
    }

    fun updateTransactionCategory(transactionId: Long, newCategoryName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.updateTransactionCategory(transactionId, newCategoryName)
                _statusMessage.value = "دسته‌بندی تراکنش با موفقیت به «$newCategoryName» تغییر یافت."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error updating transaction category", e)
                _statusMessage.value = "خطا در تغییر دسته‌بندی تراکنش"
            }
        }
    }

    fun clearAllTransactionsAndResetBalances() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.clearAllTransactionsAndResetBalances()
                _statusMessage.value = "تمام تراکنش‌ها پاکسازی و موجودی حساب‌ها ریست شد."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error clearing all transactions and resetting balances", e)
                _statusMessage.value = "خطا در پاکسازی کامل تراکنش‌ها"
            }
        }
    }

    fun clearPendingTransactions() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.clearPendingTransactions()
                _statusMessage.value = "تراکنش‌های در انتظار تایید حذف شدند."
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Error clearing pending transactions", e)
            }
        }
    }

    fun addAsset(
        name: String,
        type: AssetType,
        quantity: Double,
        priceToman: Long,
        unit: String,
        purchasePriceToman: Long = priceToman
    ) {
        viewModelScope.launch {
            val asset = Asset(
                name = name,
                type = type,
                totalQuantity = quantity,
                currentUnitPriceToman = priceToman,
                purchaseUnitPriceToman = purchasePriceToman,
                unit = unit
            )
            repository.addAsset(asset)
            _statusMessage.value = "دارایی جدید اضافه شد."
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun updateCategoryBudget(category: Category, newLimitToman: Long) {
        viewModelScope.launch {
            repository.updateCategoryBudget(category.copy(budgetLimitToman = newLimitToman))
        }
    }

    fun addBankAccount(
        bankName: String,
        accountName: String,
        cardNumber: String,
        accountNumber: String?,
        balanceToman: Long,
        cardColorHex: String,
        smsShortcode: String? = null,
        accountType: String = "BANK_CARD",
        readSmsEnabled: Boolean = true,
        initialBalanceEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            val account = BankAccount(
                bankName = bankName,
                accountName = accountName,
                cardNumber = cardNumber,
                accountNumber = accountNumber,
                balanceToman = balanceToman,
                cardColorHex = cardColorHex,
                smsShortcode = smsShortcode,
                accountType = accountType,
                readSmsEnabled = readSmsEnabled,
                initialBalanceEnabled = initialBalanceEnabled
            )
            repository.addBankAccount(account)
            _statusMessage.value = "کارت/حساب بانکی «$accountName» با موفقیت افزوده شد."
        }
    }

    fun updateBankAccount(bankAccount: BankAccount) {
        viewModelScope.launch {
            repository.updateBankAccount(bankAccount)
            _statusMessage.value = "اطلاعات کارت با موفقیت بروزرسانی شد."
        }
    }

    fun deleteBankAccount(bankAccount: BankAccount) {
        viewModelScope.launch {
            repository.deleteBankAccount(bankAccount)
            _statusMessage.value = "حساب بانکی حذف گردید."
        }
    }

    fun addBankSmsSender(bankName: String, senderNumber: String) {
        viewModelScope.launch {
            val sender = BankSmsSender(
                bankName = bankName,
                senderNumber = senderNumber,
                isActive = true
            )
            repository.addBankSmsSender(sender)
            _statusMessage.value = "سرشماره اس‌ام‌اس «$senderNumber» برای $bankName ثبت شد."
        }
    }

    fun toggleBankSmsSenderActive(sender: BankSmsSender) {
        viewModelScope.launch {
            repository.updateBankSmsSender(sender.copy(isActive = !sender.isActive))
        }
    }

    fun deleteBankSmsSender(sender: BankSmsSender) {
        viewModelScope.launch {
            repository.deleteBankSmsSender(sender)
            _statusMessage.value = "سرشماره اس‌ام‌اس حذف گردید."
        }
    }

    fun scanRecentSmsFromSenders() {
        viewModelScope.launch {
            _isParsingSms.value = true
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.READ_SMS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                _statusMessage.value = "مجوز خواندن پیامک اعطا نشده است. لطفاً ابتدا دسترسی را تایید کنید."
                _isParsingSms.value = false
                return@launch
            }

            // Query strictly filtered real bank SMS from device inbox
            val deviceMessages = com.example.util.DeviceSmsReader.getAllBankSms(
                context = getApplication(),
                requireBalance = true,
                limit = 200
            ).filter {
                !com.example.util.DeviceSmsReader.isNonBankSender(it.address) &&
                com.example.util.DeviceSmsReader.isBankTransactionSms(it.address, it.body)
            }

            if (deviceMessages.isEmpty()) {
                _statusMessage.value = "هیچ پیامک تراکنش بانکی جدید و معتبری در گوشی یافت نشد."
                _isParsingSms.value = false
                return@launch
            }

            // Sort messages chronologically from furthest/oldest to newest
            val sortedMessages = deviceMessages.sortedBy { msg ->
                com.example.util.JalaliCalendarHelper.parseBankSmsDateTime(msg.body) ?: msg.timestamp
            }

            var processedCount = 0
            for (msg in sortedMessages) {
                val res = repository.processIncomingSms(
                    smsText = msg.body,
                    senderAddress = msg.address,
                    smsTimestamp = msg.timestamp
                )
                if (res.amountToman > 0) {
                    processedCount++
                }
            }

            _isParsingSms.value = false
            _statusMessage.value = "تعداد $processedCount پیامک تراکنش بانکی جدید استخراج و در صف بررسی قرار گرفت."
        }
    }

    fun togglePremium() {
        _isPremium.value = !_isPremium.value
    }

    fun setCurrencyUnit(unit: CurrencyUnit) {
        _currencyUnit.value = unit
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun generateExport(): String {
        return Exporter.generateExportCsv(
            transactions = transactions.value,
            assets = assets.value
        )
    }
}
