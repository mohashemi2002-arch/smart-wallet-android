import re

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

target = '''    val confirmedTransactions: Flow<List<Transaction>> = transactionDao.getConfirmedTransactionsFlow()'''

replacement = '''    val confirmedTransactions: Flow<List<Transaction>> = transactionDao.getConfirmedTransactionsFlow()

    fun getConfirmedTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> {
        return transactionDao.getConfirmedTransactionsBetween(startMillis, endMillis)
    }'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
