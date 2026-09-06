package com.example.data.dao

import androidx.room.*
import com.example.data.model.AssetTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetTransactionDao {
    @Query("SELECT * FROM asset_transactions WHERE assetId = :assetId ORDER BY dateTimestamp DESC")
    fun getTransactionsForAsset(assetId: Long): Flow<List<AssetTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: AssetTransactionEntity): Long
}
