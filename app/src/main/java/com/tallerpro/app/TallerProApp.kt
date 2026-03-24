package com.tallerpro.app

import android.app.Application
import com.tallerpro.app.data.database.AppDatabase

class TallerProApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
