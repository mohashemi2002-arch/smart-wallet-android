package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AssetTransactionType { BUY, SELL }

@Entity(
    tableName = "asset_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Asset::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assetId"])]
)
data class AssetTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val type: AssetTransactionType,
    val quantity: Double,
    val unitPriceToman: Long,
    val dateTimestamp: Long,
    val notes: String? = null
)
