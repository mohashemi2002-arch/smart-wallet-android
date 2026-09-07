package com.example.data

import com.example.data.dao.AssetDao
import com.example.data.dao.MarketRateDao
import com.example.data.database.AppDatabase
import com.example.data.model.MarketRateEntity
import com.example.network.NavasanScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MarketRepository private constructor(
    private val marketRateDao: MarketRateDao,
    private val assetDao: AssetDao
) {
    private val scraper = NavasanScraper()

    fun getCachedRates(): Flow<List<MarketRateEntity>> = marketRateDao.getAllRatesFlow()

    suspend fun refreshRates(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val newRates = scraper.scrapeRates()
            if (newRates.isNotEmpty()) {
                marketRateDao.insertAll(newRates)
                syncAssetValuations(newRates)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to extract rates or empty result"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAssetValuations(rates: List<MarketRateEntity> = emptyList()) {
        val currentRates = if (rates.isEmpty()) marketRateDao.getAllRatesSync() else rates
        val assets = assetDao.getAllAssetsSync()
        
        val updatedAssets = assets.map { asset ->
            val rate = currentRates.find { it.symbol == asset.name || it.name == asset.name }
            if (rate != null && rate.priceToman > 0) {
                asset.copy(currentUnitPriceToman = rate.priceToman)
            } else {
                asset
            }
        }
        assetDao.insertAll(updatedAssets)
    }

    companion object {
        @Volatile
        private var instance: MarketRepository? = null

        fun getInstance(marketRateDao: MarketRateDao, assetDao: AssetDao): MarketRepository {
            return instance ?: synchronized(this) {
                instance ?: MarketRepository(marketRateDao, assetDao).also { instance = it }
            }
        }
    }
}
