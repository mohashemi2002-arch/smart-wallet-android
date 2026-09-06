package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChequeEntity
import com.example.data.model.ChequeStatus
import com.example.data.model.ChequeType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChequesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val chequeDao = db.chequeDao()
    private val transactionDao = db.transactionDao()

    val allCheques: StateFlow<List<ChequeEntity>> = chequeDao.getAllCheques()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCheque(
        sayadNumber: String,
        type: ChequeType,
        amount: Long,
        dueDateTimestamp: Long,
        bankName: String,
        partyName: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val cheque = ChequeEntity(
                sayadNumber = sayadNumber,
                type = type,
                amount = amount,
                dueDateTimestamp = dueDateTimestamp,
                bankName = bankName,
                partyName = partyName,
                notes = notes
            )
            chequeDao.insertCheque(cheque)
        }
    }

    fun clearCheque(chequeId: Long) {
        viewModelScope.launch {
            val cheque = chequeDao.getChequeById(chequeId) ?: return@launch
            if (cheque.status == ChequeStatus.CLEARED) return@launch

            chequeDao.updateCheque(cheque.copy(status = ChequeStatus.CLEARED))

            val txType = if (cheque.type == ChequeType.ISSUED) TransactionType.EXPENSE else TransactionType.INCOME
            transactionDao.insert(
                Transaction(
                    amountToman = cheque.amount,
                    type = txType,
                    categoryName = "چک صیادی",
                    merchantName = cheque.partyName,
                    dateMillis = System.currentTimeMillis(),
                    isParsedFromSms = false
                )
            )
        }
    }

    fun bounceCheque(chequeId: Long) {
        viewModelScope.launch {
            val cheque = chequeDao.getChequeById(chequeId) ?: return@launch
            chequeDao.updateCheque(cheque.copy(status = ChequeStatus.BOUNCED))
        }
    }
}
