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
    val debtLoanDao = db.debtLoanDao()
    val bankAccountDao = db.bankAccountDao()
    val bankSmsSenderDao = db.bankSmsSenderDao()

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allAssets: Flow<List<Asset>> = assetDao.getAllAssets()
    val allDebtLoans: Flow<List<DebtLoan>> = debtLoanDao.getAllDebtLoans()
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

        val transactions = transactionDao.getAllTransactions().first()
        if (transactions.isEmpty()) {
            val now = System.currentTimeMillis()
            val day = 86400000L
            val sampleTransactions = listOf(
                Transaction(amountToman = 450000, type = TransactionType.EXPENSE, categoryName = "خوراکی و سوپرمارکت", merchantName = "هایپرمارکت کوروش", cardNumber = "6037****8910", dateMillis = now - (day * 0.2).toLong(), isParsedFromSms = true, rawSmsText = "برداشت مبلغ 4,500,000 ریال از حساب 6037****8910 - بانک ملی"),
                Transaction(amountToman = 85000, type = TransactionType.EXPENSE, categoryName = "حمل و نقل", merchantName = "اسنپ", cardNumber = "6104****4321", dateMillis = now - (day * 0.8).toLong(), isParsedFromSms = true, rawSmsText = "خرید اسنپ مبلغ 850,000 ریال - بانک ملت"),
                Transaction(amountToman = 12000000, type = TransactionType.INCOME, categoryName = "پس‌انداز و سرمایه", merchantName = "واریز حقوق شرکت", cardNumber = "6221****1111", dateMillis = now - (day * 2).toLong(), isParsedFromSms = true, rawSmsText = "واریز مبلغ 120,000,000 ریال به حساب - بانک پارسیان"),
                Transaction(amountToman = 1250000, type = TransactionType.EXPENSE, categoryName = "سلامت و درمان", merchantName = "داروخانه دکتر رضایی", cardNumber = "6037****8910", dateMillis = now - (day * 3).toLong(), isParsedFromSms = true),
                Transaction(amountToman = 3200000, type = TransactionType.EXPENSE, categoryName = "پوشاک و خرید", merchantName = "دیجی‌کالا", cardNumber = "6037****8910", dateMillis = now - (day * 5).toLong(), isParsedFromSms = true)
            )
            for (t in sampleTransactions) {
                transactionDao.insert(t)
            }
        }

        val assets = assetDao.getAllAssets().first()
        if (assets.isEmpty()) {
            val sampleAssets = listOf(
                Asset(name = "طلا ۱۸ عیار", type = AssetType.GOLD, quantity = 25.5, currentPriceToman = 4650000, unit = "گرم"),
                Asset(name = "سکه طرح جدید (امامی)", type = AssetType.GOLD_COIN, quantity = 2.0, currentPriceToman = 52000000, unit = "عدد"),
                Asset(name = "دلار آمریکا", type = AssetType.CURRENCY, quantity = 1500.0, currentPriceToman = 68500, unit = "دلار"),
                Asset(name = "تتر (USDT)", type = AssetType.CRYPTO, quantity = 800.0, currentPriceToman = 68700, unit = "USDT"),
                Asset(name = "بیت‌کوین (BTC)", type = AssetType.CRYPTO, quantity = 0.045, currentPriceToman = 6450000000, unit = "BTC")
            )
            assetDao.insertAll(sampleAssets)
        }

        val debtLoans = debtLoanDao.getAllDebtLoans().first()
        if (debtLoans.isEmpty()) {
            val now = System.currentTimeMillis()
            val day = 86400000L
            val sampleDebts = listOf(
                DebtLoan(title = "قسط وام مسکن - بانک مسکن", type = DebtType.LOAN_INSTALLMENT, amountToman = 4200000, dueDateMillis = now + (day * 5), isPaid = false, contactName = "شعبه مرکزی"),
                DebtLoan(title = "چک صیادی شماره ۹۸۴۱۲ (خرید لپ‌تاپ)", type = DebtType.SAYAD_CHECK, amountToman = 18500000, dueDateMillis = now + (day * 12), isPaid = false, contactName = "فروشگاه آریا"),
                DebtLoan(title = "طلب از آقای احمدی", type = DebtType.RECEIVABLE_DUE, amountToman = 5000000, dueDateMillis = now + (day * 20), isPaid = false, contactName = "احمدی"),
                DebtLoan(title = "قسط وام خودرو", type = DebtType.LOAN_INSTALLMENT, amountToman = 3800000, dueDateMillis = now - (day * 2), isPaid = true, contactName = "بانک ملت")
            )
            for (d in sampleDebts) {
                debtLoanDao.insert(d)
            }
        }

        val accounts = bankAccountDao.getAllBankAccounts().first()
        if (accounts.isEmpty()) {
            val sampleAccounts = listOf(
                BankAccount(bankName = "بانک ملی ایران", accountName = "حساب جاری اصلی", cardNumber = "6037-9978-1234-8910", accountNumber = "0109823412", balanceToman = 24500000, cardColorHex = "#1E88E5"),
                BankAccount(bankName = "بانک ملت", accountName = "کارت خریدهای روزمره", cardNumber = "6104-3378-4321-4321", accountNumber = "0214872931", balanceToman = 8200000, cardColorHex = "#D32F2F"),
                BankAccount(bankName = "بلوبانک (سامان)", accountName = "حساب پس‌انداز بلو", cardNumber = "6219-8610-5544-5544", accountNumber = "0301129841", balanceToman = 15000000, cardColorHex = "#0288D1")
            )
            bankAccountDao.insertAll(sampleAccounts)
        }

        val senders = bankSmsSenderDao.getAllBankSmsSenders().first()
        if (senders.isEmpty()) {
            val defaultSenders = listOf(
                BankSmsSender(bankName = "بانک ملی ایران", senderNumber = "2000400", isActive = true),
                BankSmsSender(bankName = "بانک ملت", senderNumber = "30009411", isActive = true),
                BankSmsSender(bankName = "بانک سامان", senderNumber = "20008511", isActive = true),
                BankSmsSender(bankName = "بلوبانک", senderNumber = "989000", isActive = true),
                BankSmsSender(bankName = "بانک پاسارگاد", senderNumber = "200082", isActive = true),
                BankSmsSender(bankName = "بانک سپه", senderNumber = "300025", isActive = true),
                BankSmsSender(bankName = "بانک تجارت", senderNumber = "200070", isActive = true)
            )
            bankSmsSenderDao.insertAll(defaultSenders)
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
            isParsedFromSms = true
        )
        transactionDao.insert(newTransaction)
        
        // Match bank account by sender shortcode or card digits
        var matchedAccount: BankAccount? = null
        if (!senderAddress.isNull_or_blank_helper()) {
            val cleanSender = senderAddress!!.replace("+98", "0").trim()
            matchedAccount = bankAccountDao.findBySmsShortcode(cleanSender)
        }

        if (matchedAccount == null && result.cardNumber != null) {
            val cleanDigits = result.cardNumber.replace("*", "").takeLast(4)
            if (cleanDigits.length >= 4) {
                matchedAccount = bankAccountDao.findByCardSuffix(cleanDigits)
            }
        }

        matchedAccount?.let { acc ->
            val newBalance = if (result.type == TransactionType.EXPENSE) {
                (acc.balanceToman - result.amountToman).coerceAtLeast(0)
            } else {
                acc.balanceToman + result.amountToman
            }
            bankAccountDao.update(acc.copy(balanceToman = newBalance))
        }

        return result
    }

    private fun String?.isNull_or_blank_helper(): Boolean = this == null || this.trim().isEmpty()

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
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

    suspend fun addDebtLoan(debtLoan: DebtLoan) {
        debtLoanDao.insert(debtLoan)
    }

    suspend fun updateDebtLoan(debtLoan: DebtLoan) {
        debtLoanDao.update(debtLoan)
    }

    suspend fun deleteDebtLoan(debtLoan: DebtLoan) {
        debtLoanDao.delete(debtLoan)
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
