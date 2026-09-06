import re

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

# We need to implement confirmAndSyncBalance
# And remove the old applyBalanceAndProvision from approvePendingTransaction.

new_methods = """
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
"""

# Now let's remove the old applyBalanceAndProvision and replace approvePendingTransaction / addTransaction / deleteTransaction

old_approve = r'    suspend fun approvePendingTransaction\(transaction: Transaction\) \{[\s\S]*?suspend fun deleteTransaction\(transaction: Transaction\) \{\n        transactionDao\.delete\(transaction\)\n    \}'

content = re.sub(old_approve, new_methods.strip(), content)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)

