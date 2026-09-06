#!/bin/bash
awk '
/suspend fun insert/ {
    print "    @Query(\"SELECT * FROM transactions ORDER BY dateMillis DESC\")"
    print "    fun getAllTransactionsSync(): List<Transaction>"
    print ""
    print "    @Insert(onConflict = OnConflictStrategy.REPLACE)"
    print "    fun insertSync(transaction: Transaction): Long"
    print ""
}
{ print }
' app/src/main/java/com/example/data/dao/TransactionDao.kt > temp_dao.kt && mv temp_dao.kt app/src/main/java/com/example/data/dao/TransactionDao.kt

awk '
/suspend fun insert/ {
    print "    @Query(\"SELECT * FROM categories\")"
    print "    fun getAllCategoriesSync(): List<Category>"
    print ""
}
{ print }
' app/src/main/java/com/example/data/dao/CategoryDao.kt > temp_cat.kt && mv temp_cat.kt app/src/main/java/com/example/data/dao/CategoryDao.kt
