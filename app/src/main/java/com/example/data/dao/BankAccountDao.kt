package com.example.data.dao

import androidx.room.*
import com.example.data.model.BankAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts ORDER BY id DESC")
    fun getAllBankAccounts(): Flow<List<BankAccount>>

    @Query("SELECT * FROM bank_accounts ORDER BY id DESC")
    fun getAllBankAccountsSync(): List<BankAccount>

    @Query("SELECT * FROM bank_accounts WHERE cardNumber LIKE '%' || :cardSuffix || '%' LIMIT 1")
    suspend fun findByCardSuffix(cardSuffix: String): BankAccount?

    @Query("SELECT * FROM bank_accounts WHERE smsShortcode = :shortcode OR :shortcode LIKE '%' || smsShortcode || '%' LIMIT 1")
    suspend fun findBySmsShortcode(shortcode: String): BankAccount?

    @Query("UPDATE bank_accounts SET balanceToman = balanceToman - :amount WHERE id = :accountId")
    suspend fun deductBalance(accountId: Long, amount: Long)

    @Query("UPDATE bank_accounts SET balanceToman = balanceToman + :amount WHERE id = :accountId")
    suspend fun addBalance(accountId: Long, amount: Long)

    @Query("UPDATE bank_accounts SET balanceToman = :newBalance WHERE id = :accountId")
    suspend fun setDirectBalance(accountId: Long, newBalance: Long)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: BankAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(account: BankAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<BankAccount>)

    @Update
    suspend fun update(account: BankAccount)

    @Delete
    suspend fun delete(account: BankAccount)
}
