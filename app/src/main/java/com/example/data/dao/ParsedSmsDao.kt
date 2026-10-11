package com.example.data.dao

import androidx.room.*
import com.example.data.model.ParsedSmsEntity
import com.example.data.model.SmsProcessingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ParsedSmsDao {
    @Query("SELECT * FROM parsed_sms_records ORDER BY dateMillis DESC")
    fun getAllParsedSms(): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms_records ORDER BY dateMillis DESC")
    suspend fun getAllParsedSmsSync(): List<ParsedSmsEntity>

    @Query("SELECT * FROM parsed_sms_records WHERE status = :status ORDER BY dateMillis DESC")
    fun getParsedSmsByStatus(status: SmsProcessingStatus): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms_records WHERE status = 'PENDING' ORDER BY dateMillis DESC")
    fun getPendingParsedSms(): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms_records WHERE bankName = :bankName ORDER BY dateMillis DESC")
    fun getParsedSmsByBank(bankName: String): Flow<List<ParsedSmsEntity>>

    @Query("SELECT * FROM parsed_sms_records WHERE id = :id LIMIT 1")
    suspend fun getParsedSmsById(id: Long): ParsedSmsEntity?

    @Query("SELECT * FROM parsed_sms_records WHERE rawSmsText = :rawText LIMIT 1")
    suspend fun findByRawText(rawText: String): ParsedSmsEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM parsed_sms_records WHERE rawSmsText = :rawText)")
    suspend fun existsByRawText(rawText: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ParsedSmsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ParsedSmsEntity>): List<Long>

    @Update
    suspend fun update(entity: ParsedSmsEntity)

    @Query("UPDATE parsed_sms_records SET status = :status, linkedTransactionId = :linkedTxId WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SmsProcessingStatus, linkedTxId: Long?)

    @Delete
    suspend fun delete(entity: ParsedSmsEntity)

    @Query("DELETE FROM parsed_sms_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM parsed_sms_records")
    suspend fun clearAll()
}
