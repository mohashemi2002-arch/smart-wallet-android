package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MarketRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketRateDao {
    @Query("SELECT * FROM market_rates")
    fun getAllRatesFlow(): Flow<List<MarketRateEntity>>

    @Query("SELECT * FROM market_rates WHERE category = :category")
    fun getRatesByCategory(category: String): Flow<List<MarketRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<MarketRateEntity>)
    
    @Query("SELECT * FROM market_rates")
    suspend fun getAllRatesSync(): List<MarketRateEntity>
}
