package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Exporter
import com.example.data.FinanceRepository
import com.example.data.MarketRepository
import com.example.data.SmsAiParser
import com.example.data.model.*
import com.example.ui.components.CurrencyUnit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository.getInstance(application)
    private val marketRepository = MarketRepository.getInstance()

    val marketRates: StateFlow<List<MarketRate>> = marketRepository.rates
    val isRefreshingMarket: StateFlow<Boolean> = marketRepository.isRefreshing
    val lastMarketUpdatedTime: StateFlow<String> = marketRepository.lastUpdatedTime

    init {
        // Initial fetch of market rates
        refreshMarketRates()
    }

    fun refreshMarketRates() {
        viewModelScope.launch {
            marketRepository.fetchLiveRates()
        }
    }

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .map { list -> list.filter { it.isApproved } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .map { list -> list.filter { !it.isApproved } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<Asset>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debtLoans: StateFlow<List<DebtLoan>> = repository.allDebtLoans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccount>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankSmsSenders: StateFlow<List<BankSmsSender>> = repository.allBankSmsSenders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Preferences / Settings State
    private val _isPremium = MutableStateFlow(true) // Poolaki Plus Tier
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

    val totalNetWorthToman: StateFlow<Long> = combine(totalAssetsValueToman, totalIncomeToman, totalExpenseToman) { assetsVal, income, expense ->
        assetsVal + income - expense
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
                isParsedFromSms = true
            )
            repository.addTransaction(transaction)
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

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun approvePendingTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.approvePendingTransaction(transaction)
            _statusMessage.value = "تراکنش پیامکی تایید و در موجودی کارت اعمال شد."
        }
    }

    fun rejectPendingTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _statusMessage.value = "تراکنش پیامکی رد و حذف شد."
        }
    }

    fun addAsset(name: String, type: AssetType, quantity: Double, priceToman: Long, unit: String) {
        viewModelScope.launch {
            val asset = Asset(
                name = name,
                type = type,
                quantity = quantity,
                currentPriceToman = priceToman,
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

    fun addDebtLoan(title: String, type: DebtType, amountToman: Long, dueDateMillis: Long, contactName: String?) {
        viewModelScope.launch {
            val debt = DebtLoan(
                title = title,
                type = type,
                amountToman = amountToman,
                dueDateMillis = dueDateMillis,
                isPaid = false,
                contactName = contactName
            )
            repository.addDebtLoan(debt)
            _statusMessage.value = "آیتم تعهد/چک اضافه گردید."
        }
    }

    fun toggleDebtPaid(debtLoan: DebtLoan) {
        viewModelScope.launch {
            repository.updateDebtLoan(debtLoan.copy(isPaid = !debtLoan.isPaid))
        }
    }

    fun deleteDebtLoan(debtLoan: DebtLoan) {
        viewModelScope.launch {
            repository.deleteDebtLoan(debtLoan)
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
            val activeSendersList = bankSmsSenders.value.filter { it.isActive }
            if (activeSendersList.isEmpty()) {
                _statusMessage.value = "هیچ سرشماره اس‌ام‌اس فعال وجود ندارد."
                _isParsingSms.value = false
                return@launch
            }

            // Simulate/Fetch recent bank SMS received from active head numbers
            val sampleBankSmsToProcess = listOf(
                "برداشت مبلغ 1,850,000 ریال از حساب 6037****8910 - خرید هایپرمارکت میقات - بانک ملی",
                "واریز 4,200,000 ریال به حساب 6104****4321 - کارت به کارت از محمد - بانک ملت",
                "برداشت 750,000 ریال از حساب 6219****5544 بابت تاکسی آنلاین - بلوبانک"
            )

            var processedCount = 0
            for (smsText in sampleBankSmsToProcess) {
                repository.processIncomingSms(smsText)
                processedCount++
            }

            _isParsingSms.value = false
            _statusMessage.value = "تعداد $processedCount پیامک جدید از سرشماره‌های فعال استخراج و ثبت شد!"
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
            assets = assets.value,
            debtLoans = debtLoans.value
        )
    }
}
