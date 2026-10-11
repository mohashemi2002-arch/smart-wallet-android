package com.example.data.dao

import androidx.room.*
import com.example.data.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY id DESC")
    suspend fun getAllBudgetsSync(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryName = :categoryName LIMIT 1")
    fun getBudgetByCategoryName(categoryName: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE categoryName = :categoryName LIMIT 1")
    suspend fun getBudgetByCategoryNameSync(categoryName: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE jalaliYear = :year AND (jalaliMonth = :month OR jalaliMonth = 0) AND isActive = 1")
    fun getBudgetsForMonth(year: Int, month: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>): List<Long>

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM budgets")
    suspend fun clearAll()
}
