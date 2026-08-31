package com.example.data.dao

import androidx.room.*
import com.example.data.model.BankSmsSender
import kotlinx.coroutines.flow.Flow

@Dao
interface BankSmsSenderDao {
    @Query("SELECT * FROM bank_sms_senders ORDER BY id DESC")
    fun getAllBankSmsSenders(): Flow<List<BankSmsSender>>

    @Query("SELECT * FROM bank_sms_senders WHERE isActive = 1")
    suspend fun getActiveSenders(): List<BankSmsSender>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sender: BankSmsSender): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(senders: List<BankSmsSender>)

    @Update
    suspend fun update(sender: BankSmsSender)

    @Delete
    suspend fun delete(sender: BankSmsSender)
}
