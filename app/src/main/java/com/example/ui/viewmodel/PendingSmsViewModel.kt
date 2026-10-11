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
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val updated = transaction.copy(
                    cardNumber = newCardNumber ?: transaction.cardNumber,
                    categoryName = newCategory
                )
                repository.approvePendingTransaction(updated)
            } catch (e: Exception) {
                android.util.Log.e("PendingSmsVM", "Error approving transaction", e)
            }
        }
    }

    fun rejectTransaction(transaction: Transaction) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.deleteTransaction(transaction)
            } catch (e: Exception) {
                android.util.Log.e("PendingSmsVM", "Error rejecting transaction", e)
            }
        }
    }

    fun approveAllPending(onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val pendingList = pendingTransactions.value
                pendingList.forEach { transaction ->
                    repository.approvePendingTransaction(transaction)
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onError(e.localizedMessage ?: "خطا در تایید دسته جمعی") }
            }
        }
    }

    fun approveAll(pendingList: List<Transaction>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            pendingList.forEach { tx ->
                repository.approvePendingTransaction(tx)
            }
        }
    }
}
