package com.tallerpro.app.data.dao

import androidx.room.*
import com.tallerpro.app.data.model.WorkOrder
import com.tallerpro.app.data.model.WorkOrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders ORDER BY createdAt DESC")
    fun getAll(): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: WorkOrderStatus): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE status != 'DELIVERED' ORDER BY createdAt DESC")
    fun getActive(): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE id = :id")
    suspend fun getById(id: Long): WorkOrder?

    @Query("SELECT COUNT(*) FROM work_orders WHERE status != 'DELIVERED' AND status != 'COMPLETED'")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM work_orders")
    fun getCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalCost), 0) FROM work_orders WHERE status = 'DELIVERED'")
    fun getTotalRevenue(): Flow<Double>

    @Insert
    suspend fun insert(workOrder: WorkOrder): Long

    @Update
    suspend fun update(workOrder: WorkOrder)

    @Delete
    suspend fun delete(workOrder: WorkOrder)
}
