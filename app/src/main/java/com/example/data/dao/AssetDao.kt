package com.example.data.dao

import androidx.room.*
import com.example.data.model.Asset
import com.example.data.model.AssetType
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY id DESC")
    fun getAllAssets(): Flow<List<Asset>>

    @Query("SELECT * FROM assets")
    suspend fun getAllAssetsSync(): List<Asset>

    @Query("SELECT * FROM assets WHERE type = :type ORDER BY id DESC")
    fun getAssetsByType(type: AssetType): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getAssetById(id: Long): Asset?

    @Query("SELECT * FROM assets WHERE name = :name LIMIT 1")
    suspend fun getAssetByName(name: String): Asset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: Asset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<Asset>)

    @Update
    suspend fun update(asset: Asset)

    @Delete
    suspend fun delete(asset: Asset)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM assets")
    suspend fun clearAll()
}
