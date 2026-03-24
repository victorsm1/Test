package com.tallerpro.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class WorkOrderStatus {
    PENDING,
    IN_PROGRESS,
    WAITING_PARTS,
    COMPLETED,
    DELIVERED
}

@Entity(
    tableName = "work_orders",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class WorkOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val description: String,
    val diagnosis: String = "",
    val status: WorkOrderStatus = WorkOrderStatus.PENDING,
    val laborCost: Double = 0.0,
    val partsCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val notes: String = ""
)
