package com.example.data.dao

import androidx.room.*
import com.example.data.model.AssetTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetTransactionDao {
    @Query("SELECT * FROM asset_transactions WHERE assetId = :assetId ORDER BY dateTimestamp DESC")
    fun getTransactionsForAsset(assetId: Long): Flow<List<AssetTransactionEntity>>

    @Query("SELECT * FROM asset_transactions ORDER BY dateTimestamp DESC")
    fun getAllAssetTransactions(): Flow<List<AssetTransactionEntity>>

    @Query("SELECT * FROM asset_transactions ORDER BY dateTimestamp DESC")
    suspend fun getAllAssetTransactionsSync(): List<AssetTransactionEntity>

    @Query("SELECT * FROM asset_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): AssetTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: AssetTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<AssetTransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: AssetTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: AssetTransactionEntity)

    @Query("DELETE FROM asset_transactions WHERE assetId = :assetId")
    suspend fun deleteTransactionsByAssetId(assetId: Long)

    @Query("DELETE FROM asset_transactions")
    suspend fun clearAll()
}
