package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Asset
import com.example.data.model.AssetTransactionEntity
import com.example.data.model.AssetTransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AssetMetric(
    val asset: Asset,
    val totalInvestedToman: Long,
    val currentValueToman: Long,
    val pnlToman: Long,
    val pnlPercentage: Double
)

class PortfolioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val assetDao = db.assetDao()
    private val assetTxDao = db.assetTransactionDao()

    val assets: StateFlow<List<Asset>> = assetDao.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPortfolioValue: StateFlow<Long> = assets.map { list ->
        list.sumOf { it.totalValueToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // A flow of lists of metrics
    val assetMetrics: StateFlow<List<AssetMetric>> = assets.map { assetList ->
        assetList.map { asset ->
            val transactions = assetTxDao.getTransactionsForAsset(asset.id).firstOrNull() ?: emptyList()
            var invested = 0L
            var quantityHeld = 0.0
            
            // Simple weighted calculation for invested amount
            for (tx in transactions) {
                if (tx.type == AssetTransactionType.BUY) {
                    invested += (tx.quantity * tx.unitPriceToman).toLong()
                    quantityHeld += tx.quantity
                } else {
                    if (quantityHeld > 0) {
                        val proportion = tx.quantity / quantityHeld
                        invested -= (invested * proportion).toLong()
                    }
                    quantityHeld -= tx.quantity
                }
            }

            if (transactions.isEmpty() && asset.purchaseUnitPriceToman > 0) {
                invested = asset.totalInvestedToman
            }
            
            val currentVal = asset.totalValueToman
            val pnl = currentVal - invested
            val pnlPct = if (invested > 0) (pnl.toDouble() / invested.toDouble()) * 100.0 else 0.0

            AssetMetric(asset, invested, currentVal, pnl, pnlPct)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun addAsset(
        name: String,
        type: com.example.data.model.AssetType,
        quantity: Double,
        priceToman: Long,
        unit: String,
        purchasePriceToman: Long = priceToman
    ) {
        viewModelScope.launch {
            val asset = Asset(
                name = name,
                type = type,
                totalQuantity = quantity,
                currentUnitPriceToman = priceToman,
                purchaseUnitPriceToman = purchasePriceToman,
                unit = unit
            )
            val assetId = assetDao.insert(asset)
            if (quantity > 0) {
                assetTxDao.insertTransaction(
                    AssetTransactionEntity(
                        assetId = assetId,
                        type = AssetTransactionType.BUY,
                        quantity = quantity,
                        unitPriceToman = purchasePriceToman,
                        dateTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun updateAssetPrice(asset: Asset, newPriceToman: Long) {
        viewModelScope.launch {
            assetDao.update(
                asset.copy(
                    currentUnitPriceToman = newPriceToman,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun addAssetTransaction(assetId: Long, type: AssetTransactionType, quantity: Double, unitPriceToman: Long) {
        viewModelScope.launch {
            val tx = AssetTransactionEntity(
                assetId = assetId,
                type = type,
                quantity = quantity,
                unitPriceToman = unitPriceToman,
                dateTimestamp = System.currentTimeMillis()
            )
            assetTxDao.insertTransaction(tx)
            
            // Update total quantity
            val asset = assetDao.getAssetById(assetId)
            if (asset != null) {
                val newQuantity = if (type == AssetTransactionType.BUY) {
                    asset.totalQuantity + quantity
                } else {
                    maxOf(0.0, asset.totalQuantity - quantity)
                }
                assetDao.update(
                    asset.copy(
                        totalQuantity = newQuantity,
                        currentUnitPriceToman = unitPriceToman,
                        lastUpdatedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
