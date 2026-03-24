package com.tallerpro.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val partNumber: String = "",
    val quantity: Int = 0,
    val minStock: Int = 0,
    val unitPrice: Double = 0.0,
    val supplier: String = "",
    val category: String = ""
)
