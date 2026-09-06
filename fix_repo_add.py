with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

old = """    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
        if (transaction.status == com.example.data.model.TransactionStatus.CONFIRMED) {
            approvePendingTransaction(transaction)
        }
    }"""

new = """    suspend fun addTransaction(transaction: Transaction) {
        val newId = transactionDao.insert(transaction)
        val inserted = transaction.copy(id = newId)
        if (inserted.status == com.example.data.model.TransactionStatus.CONFIRMED) {
            approvePendingTransaction(inserted)
        }
    }"""

content = content.replace(old, new)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
