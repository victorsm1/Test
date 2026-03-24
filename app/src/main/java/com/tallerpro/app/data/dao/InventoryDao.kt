package com.tallerpro.app.data.dao

import androidx.room.*
import com.tallerpro.app.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory WHERE name LIKE '%' || :query || '%' OR partNumber LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory WHERE quantity <= minStock")
    fun getLowStock(): Flow<List<InventoryItem>>

    @Query("SELECT COUNT(*) FROM inventory WHERE quantity <= minStock")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory")
    fun getCount(): Flow<Int>

    @Insert
    suspend fun insert(item: InventoryItem): Long

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)
}
