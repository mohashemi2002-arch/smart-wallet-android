with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

import re

# We will replace approvePendingTransaction
# And updateBalanceForTransaction

old_func = """    suspend fun approvePendingTransaction(transaction: Transaction) {
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
    }"""

new_func = """    suspend fun approvePendingTransaction(transaction: Transaction) {
        androidx.room.withTransaction(db) {
            var finalCategoryName = transaction.categoryName
            if (finalCategoryName.isBlank()) {
                finalCategoryName = "متفرقه"
            }
            val categories = categoryDao.getAllCategoriesSync()
            if (categories.none { it.name == finalCategoryName }) {
                categoryDao.insert(Category(name = finalCategoryName, colorHex = "#95A5A6"))
            }

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
                        bankAccountDao.insert(newAccount)
                        matchedAccount = bankAccountDao.findByCardSuffix(cleanDigits)
                    } else {
                        finalCardNumber = matchedAccount.cardNumber
                    }
                }
            }

            val updatedTransaction = transaction.copy(
                status = com.example.data.model.TransactionStatus.CONFIRMED,
                categoryName = finalCategoryName,
                cardNumber = finalCardNumber
            )
            transactionDao.update(updatedTransaction)

            matchedAccount?.let { acc ->
                val newBalance = if (updatedTransaction.type == TransactionType.EXPENSE) {
                    (acc.balanceToman - updatedTransaction.amountToman).coerceAtLeast(0)
                } else {
                    acc.balanceToman + updatedTransaction.amountToman
                }
                bankAccountDao.update(acc.copy(balanceToman = newBalance))
            }
        }
    }

    private fun String?.isNull_or_blank_helper(): Boolean = this == null || this.trim().isEmpty()

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
        if (transaction.status == com.example.data.model.TransactionStatus.CONFIRMED) {
            approvePendingTransaction(transaction)
        }
    }"""

if old_func in content:
    content = content.replace(old_func, new_func)
else:
    print("Could not find old_func")

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
