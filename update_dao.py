import re

with open('app/src/main/java/com/example/data/dao/TransactionDao.kt', 'r') as f:
    content = f.read()

target = '''    @Query("SELECT * FROM transactions WHERE dateMillis >= :startTime ORDER BY dateMillis DESC")
    fun getTransactionsSince(startTime: Long): Flow<List<Transaction>>'''

replacement = '''    @Query("SELECT * FROM transactions WHERE dateMillis >= :startTime ORDER BY dateMillis DESC")
    fun getTransactionsSince(startTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = 'CONFIRMED' AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getConfirmedTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/dao/TransactionDao.kt', 'w') as f:
    f.write(content)
