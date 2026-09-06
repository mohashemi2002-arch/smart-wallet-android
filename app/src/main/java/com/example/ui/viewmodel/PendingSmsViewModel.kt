package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FinanceRepository
import com.example.data.model.BankAccount
import com.example.data.model.Category
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingSmsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository.getInstance(application)

    val pendingTransactions: StateFlow<List<Transaction>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccount>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun approveTransaction(transaction: Transaction, newCardNumber: String?, newCategory: String) {
        viewModelScope.launch {
            val updated = transaction.copy(
                cardNumber = newCardNumber ?: transaction.cardNumber,
                categoryName = newCategory
                // Do NOT set status here, let repository handle it to prevent double confirmation issues if we add a check
            )
            repository.approvePendingTransaction(updated)
        }
    }

    fun rejectTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun approveAll(pendingList: List<Transaction>) {
        viewModelScope.launch {
            pendingList.forEach { tx ->
                repository.approvePendingTransaction(tx)
            }
        }
    }
}
