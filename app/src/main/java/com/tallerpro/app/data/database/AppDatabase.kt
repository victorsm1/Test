package com.tallerpro.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tallerpro.app.data.dao.ClientDao
import com.tallerpro.app.data.dao.InventoryDao
import com.tallerpro.app.data.dao.VehicleDao
import com.tallerpro.app.data.dao.WorkOrderDao
import com.tallerpro.app.data.model.Client
import com.tallerpro.app.data.model.InventoryItem
import com.tallerpro.app.data.model.Vehicle
import com.tallerpro.app.data.model.WorkOrder

@Database(
    entities = [Client::class, Vehicle::class, WorkOrder::class, InventoryItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tallerpro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
