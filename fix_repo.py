import re

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'r') as f:
    content = f.read()

target = '''    suspend fun processIncomingSms(smsText: String, senderAddress: String? = null): ParsedSmsResult {'''

replacement = '''    suspend fun importPoolakiExcel(rows: List<com.example.data.PoolakiExcelRow>): Pair<Int, Int> {
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

    suspend fun processIncomingSms(smsText: String, senderAddress: String? = null): ParsedSmsResult {'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/FinanceRepository.kt', 'w') as f:
    f.write(content)
