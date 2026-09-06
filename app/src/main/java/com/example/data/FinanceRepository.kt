package com.example.data

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FinanceRepository private constructor(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    val transactionDao = db.transactionDao()
    val categoryDao = db.categoryDao()
    val assetDao = db.assetDao()
    val bankAccountDao = db.bankAccountDao()
    val bankSmsSenderDao = db.bankSmsSenderDao()

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allAssets: Flow<List<Asset>> = assetDao.getAllAssets()
    val allBankAccounts: Flow<List<BankAccount>> = bankAccountDao.getAllBankAccounts()
    val allBankSmsSenders: Flow<List<BankSmsSender>> = bankSmsSenderDao.getAllBankSmsSenders()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val categories = categoryDao.getAllCategories().first()
        if (categories.isEmpty()) {
            val defaultCategories = listOf(
                Category(name = "خوراکی و سوپرمارکت", budgetLimitToman = 12000000, colorHex = "#FF5722", iconName = "restaurant"),
                Category(name = "حمل و نقل", budgetLimitToman = 4000000, colorHex = "#0288D1", iconName = "directions_car"),
                Category(name = "مسکن و اجاره", budgetLimitToman = 25000000, colorHex = "#7B1FA2", iconName = "home"),
                Category(name = "قبوض و خدمات", budgetLimitToman = 3000000, colorHex = "#00897B", iconName = "receipt"),
                Category(name = "سلامت و درمان", budgetLimitToman = 5000000, colorHex = "#E91E63", iconName = "local_hospital"),
                Category(name = "پوشاک و خرید", budgetLimitToman = 8000000, colorHex = "#F57C00", iconName = "shopping_bag"),
                Category(name = "تفریح و سرگرمی", budgetLimitToman = 6000000, colorHex = "#3F51B5", iconName = "movie"),
                Category(name = "پس‌انداز و سرمایه", budgetLimitToman = 15000000, colorHex = "#388E3C", iconName = "savings"),
                Category(name = "متفرقه", budgetLimitToman = 5000000, colorHex = "#607D8B", iconName = "category")
            )
            categoryDao.insertAll(defaultCategories)
        }
    }

    suspend fun processIncomingSms(smsText: String, senderAddress: String? = null): ParsedSmsResult {
        val result = SmsAiParser.parseSms(smsText)
        val newTransaction = Transaction(
            amountToman = result.amountToman,
            type = result.type,
            categoryName = result.categoryName,
            merchantName = result.merchantName,
            cardNumber = result.cardNumber,
            dateMillis = System.currentTimeMillis(),
            rawSmsText = smsText,
            isParsedFromSms = true,
            status = com.example.data.model.TransactionStatus.PENDING_APPROVAL // Set as pending for user approval
        )
        transactionDao.insert(newTransaction)
        return result
    }

    suspend fun approvePendingTransaction(transaction: Transaction) {
        if (transaction.status == com.example.data.model.TransactionStatus.CONFIRMED) return
        
        val updatedTransaction = transaction.copy(status = com.example.data.model.TransactionStatus.CONFIRMED)
        transactionDao.update(updatedTransaction)
        
        updateBalanceForTransaction(updatedTransaction)
    }

    private fun String?.isNull_or_blank_helper(): Boolean = this == null || this.trim().isEmpty()

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
        if (transaction.status == com.example.data.model.TransactionStatus.CONFIRMED) {
            updateBalanceForTransaction(transaction)
        }
    }

    private suspend fun updateBalanceForTransaction(transaction: Transaction) {
        var matchedAccount: BankAccount? = null
        if (transaction.cardNumber != null) {
            val cleanDigits = transaction.cardNumber.replace("*", "").takeLast(4)
            if (cleanDigits.length >= 4) {
                matchedAccount = bankAccountDao.findByCardSuffix(cleanDigits)
            }
        }
        
        matchedAccount?.let { acc ->
            val newBalance = if (transaction.type == TransactionType.EXPENSE) {
                (acc.balanceToman - transaction.amountToman).coerceAtLeast(0)
            } else {
                acc.balanceToman + transaction.amountToman
            }
            bankAccountDao.update(acc.copy(balanceToman = newBalance))
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun addBankAccount(bankAccount: BankAccount) {
        bankAccountDao.insert(bankAccount)
    }

    suspend fun updateBankAccount(bankAccount: BankAccount) {
        bankAccountDao.update(bankAccount)
    }

    suspend fun deleteBankAccount(bankAccount: BankAccount) {
        bankAccountDao.delete(bankAccount)
    }

    suspend fun addBankSmsSender(sender: BankSmsSender) {
        bankSmsSenderDao.insert(sender)
    }

    suspend fun updateBankSmsSender(sender: BankSmsSender) {
        bankSmsSenderDao.update(sender)
    }

    suspend fun deleteBankSmsSender(sender: BankSmsSender) {
        bankSmsSenderDao.delete(sender)
    }

    suspend fun addAsset(asset: Asset) {
        assetDao.insert(asset)
    }

    suspend fun updateAsset(asset: Asset) {
        assetDao.update(asset)
    }

    suspend fun deleteAsset(asset: Asset) {
        assetDao.delete(asset)
    }




    suspend fun updateCategoryBudget(category: Category) {
        categoryDao.update(category)
    }

    companion object {
        @Volatile
        private var INSTANCE: FinanceRepository? = null

        fun getInstance(context: Context): FinanceRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FinanceRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
