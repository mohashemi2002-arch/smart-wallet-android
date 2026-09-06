with open('app/src/main/java/com/example/data/dao/TransactionDao.kt', 'r') as f:
    content = f.read()

replacement = """    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE status = 'PENDING_APPROVAL' ORDER BY dateMillis DESC")
    fun getPendingTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = 'CONFIRMED' ORDER BY dateMillis DESC")
    fun getConfirmedTransactionsFlow(): Flow<List<Transaction>>"""

content = content.replace('    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")\n    fun getAllTransactions(): Flow<List<Transaction>>', replacement)

with open('app/src/main/java/com/example/data/dao/TransactionDao.kt', 'w') as f:
    f.write(content)
