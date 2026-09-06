package com.example.data

import android.content.Context
import androidx.room.withTransaction
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
    val confirmedTransactions: Flow<List<Transaction>> = transactionDao.getConfirmedTransactionsFlow()

    fun getConfirmedTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> {
        return transactionDao.getConfirmedTransactionsBetween(startMillis, endMillis)
    }
    val pendingTransactions: Flow<List<Transaction>> = transactionDao.getPendingTransactionsFlow()
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

    suspend fun importPoolakiExcel(rows: List<com.example.data.PoolakiExcelRow>): Pair<Int, Int> {
        var txCount = 0
        var accCount = 0
        
        db.withTransaction {
            val existingAccounts = bankAccountDao.getAllBankAccountsSync().associateBy { it.accountName }
            val existingCategories = categoryDao.getAllCategoriesSync().associateBy { it.name }
            
            val accountNameMap = mutableMapOf<String, Long>()
            existingAccounts.forEach { (name, acc) -> accountNameMap[name] = acc.id }
            
            val categoryNameMap = mutableMapOf<String, Long>()
            existingCategories.forEach { (name, cat) -> categoryNameMap[name] = cat.id }
            
            val accountBalanceMap = mutableMapOf<String, Long>()

            for (row in rows) {
                // 1. Resolve Account
                val accName = row.source.trim().ifEmpty { "نامشخص" }
                val accountId = accountNameMap.getOrPut(accName) {
                    val newAcc = BankAccount(
                        bankName = accName,
                        accountName = accName,
                        cardNumber = accName,
                        balanceToman = 0L,
                        cardColorHex = "#95A5A6",
                        accountType = "OTHER_SOURCE"
                    )
                    accCount++
                    bankAccountDao.insertSync(newAcc)
                }
                
                // 2. Resolve Category
                val catName = row.category.trim().ifEmpty { "متفرقه" }
                val categoryId = categoryNameMap.getOrPut(catName) {
                    categoryDao.insertSync(Category(name = catName, colorHex = "#00897B"))
                }
                
                // 3. Process Date
                val dateParts = row.dateJalali.split("/")
                val dateMillis = if (dateParts.size >= 3) {
                    val y = dateParts[0].toIntOrNull() ?: 1400
                    val m = dateParts[1].toIntOrNull() ?: 1
                    val d = dateParts[2].take(2).toIntOrNull() ?: 1 // Handle potential time part
                    com.example.util.JalaliCalendarHelper.getMillisFromJalali(y, m, d)
                } else {
                    System.currentTimeMillis()
                }
                
                // 4. Process Amount and Type
                val rawAmount = row.amountRial.replace(Regex("[^0-9-]"), "").toLongOrNull() ?: 0L
                val amountToman = Math.abs(rawAmount) / 10L
                
                val txType = when (row.type.trim()) {
                    "دخل" -> TransactionType.INCOME
                    "جابجایی" -> TransactionType.EXPENSE
                    else -> TransactionType.EXPENSE
                }
                
                // Track balance per account
                val currentBal = accountBalanceMap.getOrDefault(accName, 0L)
                if (txType == TransactionType.INCOME) {
                    accountBalanceMap[accName] = currentBal + amountToman
                } else {
                    accountBalanceMap[accName] = currentBal - amountToman
                }
                
                // 5. Insert Transaction
                val tx = Transaction(
                    amountToman = amountToman,
                    type = txType,
                    categoryName = catName,
                    merchantName = row.description.trim().ifEmpty { "وارد شده از اکسل" },
                    cardNumber = accName,
                    dateMillis = dateMillis,
                    status = com.example.data.model.TransactionStatus.CONFIRMED,
                    notes = row.description.trim()
                )
                transactionDao.insertSync(tx)
                txCount++
            }
            
            // 6. Recalculate balances and update BankAccounts
            for ((accName, netBalance) in accountBalanceMap) {
                val accId = accountNameMap[accName]
                if (accId != null) {
                    bankAccountDao.addBalance(accId, netBalance)
                }
            }
        }
        
        return Pair(txCount, accCount)
    }

    suspend fun processIncomingSms(smsText: String, senderAddress: String? = null): ParsedSmsResult {
        val result = SmsAiParser.parseSms(smsText, senderAddress = senderAddress)
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

suspend fun confirmAndSyncBalance(
        transactionId: Long,
        accountId: Long,
        categoryName: String,
        amount: Long,
        type: TransactionType,
        cardNumber: String?
    ) {
        db.withTransaction {
            // 1. Update the transaction row to CONFIRMED
            val tx = transactionDao.getAllTransactionsSync().find { it.id == transactionId }
            if (tx != null) {
                val updatedTx = tx.copy(
                    status = com.example.data.model.TransactionStatus.CONFIRMED,
                    categoryName = categoryName,
                    cardNumber = cardNumber
                )
                transactionDao.update(updatedTx)
            }

            // Ensure category exists
            val categories = categoryDao.getAllCategoriesSync()
            if (categories.none { it.name == categoryName }) {
                categoryDao.insert(Category(name = categoryName, colorHex = "#95A5A6"))
            }

            // 2. Adjust the corresponding bank account balance
            when (type) {
                TransactionType.EXPENSE -> {
                    bankAccountDao.deductBalance(accountId, amount)
                }
                TransactionType.INCOME -> {
                    bankAccountDao.addBalance(accountId, amount)
                }
            }
        }
    }

    suspend fun reverseTransactionBalance(transaction: Transaction) {
        if (transaction.status != com.example.data.model.TransactionStatus.CONFIRMED) return
        
        db.withTransaction {
            val account = if (!transaction.cardNumber.isNullOrBlank()) {
                val cleanDigits = transaction.cardNumber.replace("*", "").replace("-", "").takeLast(4)
                if (cleanDigits.isNotEmpty()) bankAccountDao.findByCardSuffix(cleanDigits) else null
            } else null

            account?.let {
                when (transaction.type) {
                    TransactionType.EXPENSE -> {
                        bankAccountDao.addBalance(it.id, transaction.amountToman)
                    }
                    TransactionType.INCOME -> {
                        bankAccountDao.deductBalance(it.id, transaction.amountToman)
                    }
                }
            }
        }
    }

    suspend fun approvePendingTransaction(transaction: Transaction) {
        if (transaction.status == com.example.data.model.TransactionStatus.CONFIRMED) return
        
        // This is a single approval from UI that might not have resolved the accountId yet.
        // But if UI passes everything via PendingSmsViewModel, we can do it directly.
        // Wait, old UI might just call this. We will redirect it to a resolve-then-call logic.
        
        var finalCardNumber = transaction.cardNumber
        var matchedAccount: BankAccount? = null
        if (!finalCardNumber.isNullOrBlank()) {
            val cleanDigits = finalCardNumber.replace("*", "").replace("-", "").takeLast(4)
            if (cleanDigits.isNotEmpty()) {
                matchedAccount = bankAccountDao.findByCardSuffix(cleanDigits)
                if (matchedAccount == null) {
                    val newAccount = BankAccount(
                        bankName = "حساب پیامکی",
                        accountName = "حساب جدید",
                        cardNumber = finalCardNumber,
                        accountNumber = finalCardNumber,
                        balanceToman = 0L,
                        cardColorHex = "#1E88E5",
                        accountType = "BANK_CARD"
                    )
                    val insertedId = bankAccountDao.insert(newAccount)
                    // We must fetch it or create dummy one with ID
                    matchedAccount = newAccount.copy(id = insertedId)
                }
            }
        }
        
        val accountId = matchedAccount?.id ?: 0L // If 0, deductBalance might do nothing (which is fine if no account linked)
        
        var finalCategoryName = transaction.categoryName
        if (finalCategoryName.isBlank()) {
            finalCategoryName = "متفرقه"
        }

        confirmAndSyncBalance(
            transactionId = transaction.id,
            accountId = accountId,
            categoryName = finalCategoryName,
            amount = transaction.amountToman,
            type = transaction.type,
            cardNumber = finalCardNumber
        )
    }

    suspend fun addTransaction(transaction: Transaction) {
        val newId = transactionDao.insert(transaction)
        val inserted = transaction.copy(id = newId)
        if (inserted.status == com.example.data.model.TransactionStatus.CONFIRMED) {
            approvePendingTransaction(inserted)
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        reverseTransactionBalance(transaction)
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
