package com.example.data.dao

import androidx.room.*
import com.example.data.model.DebtLoan
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtLoanDao {
    @Query("SELECT * FROM debt_loans ORDER BY dueDateMillis ASC")
    fun getAllDebtLoans(): Flow<List<DebtLoan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debtLoan: DebtLoan): Long

    @Update
    suspend fun update(debtLoan: DebtLoan)

    @Delete
    suspend fun delete(debtLoan: DebtLoan)
}
