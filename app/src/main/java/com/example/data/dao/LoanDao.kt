package com.example.data.dao

import androidx.room.*
import com.example.data.model.InstallmentEntity
import com.example.data.model.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY id DESC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<InstallmentEntity>)

    @Query("SELECT * FROM installments WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getInstallmentsForLoan(loanId: Long): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE isPaid = 0 ORDER BY dueDateTimestamp ASC")
    fun getAllPendingInstallments(): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE isPaid = 0 AND dueDateTimestamp <= :endTimestamp")
    suspend fun getPendingInstallmentsBefore(endTimestamp: Long): List<InstallmentEntity>

    @Update
    suspend fun updateInstallment(installment: InstallmentEntity)
    
    @Query("SELECT * FROM installments WHERE id = :id LIMIT 1")
    suspend fun getInstallmentById(id: Long): InstallmentEntity?
    
    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: Long): LoanEntity?
}
