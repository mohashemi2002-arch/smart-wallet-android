import re

with open('app/src/main/java/com/example/data/dao/BankAccountDao.kt', 'r') as f:
    content = f.read()

content = content.replace('    fun getAllBankAccounts(): Flow<List<BankAccount>>', '    fun getAllBankAccounts(): Flow<List<BankAccount>>\n\n    @Query("SELECT * FROM bank_accounts ORDER BY id DESC")\n    fun getAllBankAccountsSync(): List<BankAccount>')
content = content.replace('    suspend fun insert(account: BankAccount): Long', '    suspend fun insert(account: BankAccount): Long\n\n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    fun insertSync(account: BankAccount): Long')

with open('app/src/main/java/com/example/data/dao/BankAccountDao.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/data/dao/CategoryDao.kt', 'r') as f:
    content = f.read()

if "fun insertSync(category: Category): Long" not in content:
    content = content.replace('    suspend fun insert(category: Category): Long', '    suspend fun insert(category: Category): Long\n\n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    fun insertSync(category: Category): Long')
    with open('app/src/main/java/com/example/data/dao/CategoryDao.kt', 'w') as f:
        f.write(content)

