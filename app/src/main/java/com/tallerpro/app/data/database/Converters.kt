package com.tallerpro.app.data.database

import androidx.room.TypeConverter
import com.tallerpro.app.data.model.WorkOrderStatus

class Converters {
    @TypeConverter
    fun fromWorkOrderStatus(status: WorkOrderStatus): String = status.name

    @TypeConverter
    fun toWorkOrderStatus(value: String): WorkOrderStatus = WorkOrderStatus.valueOf(value)
}
