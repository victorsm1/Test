package com.tallerpro.app.data.dao

import androidx.room.*
import com.tallerpro.app.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY brand ASC, model ASC")
    fun getAll(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE clientId = :clientId")
    fun getByClient(clientId: Long): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: Long): Vehicle?

    @Query("SELECT * FROM vehicles WHERE plate LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR model LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<Vehicle>>

    @Query("SELECT COUNT(*) FROM vehicles")
    fun getCount(): Flow<Int>

    @Insert
    suspend fun insert(vehicle: Vehicle): Long

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)
}
