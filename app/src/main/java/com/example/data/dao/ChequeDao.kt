package com.example.data.dao

import androidx.room.*
import com.example.data.model.ChequeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeDao {
    @Query("SELECT * FROM cheques ORDER BY dueDateTimestamp ASC")
    fun getAllCheques(): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE status = 'PENDING' AND dueDateTimestamp <= :endTimestamp")
    suspend fun getPendingChequesBefore(endTimestamp: Long): List<ChequeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: ChequeEntity): Long

    @Update
    suspend fun updateCheque(cheque: ChequeEntity)

    @Query("SELECT * FROM cheques WHERE id = :id LIMIT 1")
    suspend fun getChequeById(id: Long): ChequeEntity?
}
