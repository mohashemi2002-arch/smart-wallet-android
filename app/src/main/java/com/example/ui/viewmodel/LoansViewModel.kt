package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.InstallmentEntity
import com.example.data.model.LoanEntity
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoansViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val loanDao = db.loanDao()
    private val transactionDao = db.transactionDao()

    val loans: StateFlow<List<LoanEntity>> = loanDao.getAllLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingInstallments: StateFlow<List<InstallmentEntity>> = loanDao.getAllPendingInstallments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getInstallmentsForLoan(loanId: Long): StateFlow<List<InstallmentEntity>> {
        return loanDao.getInstallmentsForLoan(loanId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addLoan(
        title: String,
        totalAmount: Long,
        installmentCount: Int,
        installmentAmount: Long,
        firstDueDateTimestamp: Long,
        sourceAccountId: Long? = null
    ) {
        viewModelScope.launch {
            val loan = LoanEntity(
                title = title,
                totalAmount = totalAmount,
                installmentCount = installmentCount,
                installmentAmount = installmentAmount,
                firstDueDateTimestamp = firstDueDateTimestamp,
                sourceAccountId = sourceAccountId
            )
            val loanId = loanDao.insertLoan(loan)
            
            val installments = mutableListOf<InstallmentEntity>()
            val monthMillis = 30L * 24 * 60 * 60 * 1000
            for (i in 1..installmentCount) {
                installments.add(
                    InstallmentEntity(
                        loanId = loanId,
                        installmentNumber = i,
                        dueDateTimestamp = firstDueDateTimestamp + ((i - 1) * monthMillis),
                        amount = installmentAmount
                    )
                )
            }
            loanDao.insertInstallments(installments)
        }
    }

    fun payInstallment(installmentId: Long) {
        viewModelScope.launch {
            val installment = loanDao.getInstallmentById(installmentId) ?: return@launch
            if (installment.isPaid) return@launch

            val loan = loanDao.getLoanById(installment.loanId)
            
            loanDao.updateInstallment(
                installment.copy(
                    isPaid = true,
                    paidTimestamp = System.currentTimeMillis()
                )
            )

            transactionDao.insert(
                Transaction(
                    amountToman = installment.amount,
                    type = TransactionType.EXPENSE,
                    categoryName = "اقساط وام",
                    merchantName = loan?.title ?: "پرداخت قسط",
                    dateMillis = System.currentTimeMillis(),
                    isParsedFromSms = false
                )
            )
        }
    }
}
