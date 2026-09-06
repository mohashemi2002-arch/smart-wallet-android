package com.example.data.dao

import androidx.room.*
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE status = 'PENDING_APPROVAL' ORDER BY dateMillis DESC")
    fun getPendingTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = 'CONFIRMED' ORDER BY dateMillis DESC")
    fun getConfirmedTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE dateMillis >= :startTime ORDER BY dateMillis DESC")
    fun getTransactionsSince(startTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = 'CONFIRMED' AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getConfirmedTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactionsSync(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
