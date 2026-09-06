import re

with open('app/src/main/java/com/example/data/dao/BankAccountDao.kt', 'r') as f:
    content = f.read()

queries = """    @Query("SELECT * FROM bank_accounts WHERE smsShortcode = :shortcode OR :shortcode LIKE '%' || smsShortcode || '%' LIMIT 1")
    suspend fun findBySmsShortcode(shortcode: String): BankAccount?

    @Query("UPDATE bank_accounts SET balanceToman = balanceToman - :amount WHERE id = :accountId")
    suspend fun deductBalance(accountId: Long, amount: Long)

    @Query("UPDATE bank_accounts SET balanceToman = balanceToman + :amount WHERE id = :accountId")
    suspend fun addBalance(accountId: Long, amount: Long)

    @Query("UPDATE bank_accounts SET balanceToman = :newBalance WHERE id = :accountId")
    suspend fun setDirectBalance(accountId: Long, newBalance: Long)
"""

content = content.replace('    @Query("SELECT * FROM bank_accounts WHERE smsShortcode = :shortcode OR :shortcode LIKE \'%\' || smsShortcode || \'%\' LIMIT 1")\n    suspend fun findBySmsShortcode(shortcode: String): BankAccount?', queries)

with open('app/src/main/java/com/example/data/dao/BankAccountDao.kt', 'w') as f:
    f.write(content)
