package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.BankSmsHistoryItem
import com.example.data.model.BankAccount
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.SmsAiParser
import com.example.domain.util.BankSmsHistoryScanner
import com.example.data.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BankSmsHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val scanner = BankSmsHistoryScanner(application, database)
    private val parser = SmsAiParser
    private val repository = FinanceRepository.getInstance(application)

    val bankAccounts: StateFlow<List<BankAccount>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _allSms = MutableStateFlow<List<BankSmsHistoryItem>>(emptyList())
    
    val selectedBankFilter = MutableStateFlow<String?>("همه")

    val registeredBanks: StateFlow<List<String>> = combine(_allSms) { (smsList) ->
        val banks = smsList.map { it.bankName }.distinct().sorted()
        listOf("همه") + banks
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf("همه"))

    val smsHistoryList: StateFlow<List<BankSmsHistoryItem>> = combine(_allSms, selectedBankFilter) { smsList, filter ->
        if (filter == "همه" || filter == null) {
            smsList
        } else {
            smsList.filter { it.bankName == filter }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        refreshScan()
    }

    fun refreshScan() {
        viewModelScope.launch {
            _isScanning.value = true
            val results = scanner.scanInboxForBankSms()
            _allSms.value = results
            _isScanning.value = false
        }
    }

    suspend fun parseSmsManual(sms: BankSmsHistoryItem): Transaction? {
        // SmsAiParser.parseSms is suspend function
        val result = parser.parseSms(sms.body, senderAddress = sms.sender)
        return if (result.amountToman > 0) {
            Transaction(
                amountToman = result.amountToman,
                type = result.type,
                categoryName = result.categoryName.ifBlank { "متفرقه" },
                merchantName = result.merchantName.ifBlank { sms.bankName },
                cardNumber = result.cardNumber,
                dateMillis = sms.timestamp,
                rawSmsText = sms.body,
                isParsedFromSms = true,
                status = TransactionStatus.PENDING_APPROVAL
            )
        } else {
            // Even if parsing fails, we return a draft so the user can fill it
            Transaction(
                amountToman = 0L,
                type = com.example.data.model.TransactionType.EXPENSE,
                categoryName = "متفرقه",
                merchantName = sms.bankName,
                dateMillis = sms.timestamp,
                rawSmsText = sms.body,
                isParsedFromSms = true,
                status = TransactionStatus.PENDING_APPROVAL
            )
        }
    }
    
    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
            refreshScan() // Re-scan to update the "registered" flag
        }
    }
}
