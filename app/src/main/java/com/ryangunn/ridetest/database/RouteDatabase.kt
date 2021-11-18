package com.ryangunn.ridetest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ryangunn.ridetest.database.model.Route
import com.ryangunn.ridetest.util.BitmapRoomConverter

@Database(entities = [Route::class], version = 1)
@TypeConverters(BitmapRoomConverter::class)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun routeDAO(): RouteDAO

    companion object {
        private var INSTANCE: RouteDatabase? = null
        fun getDatabase(context: Context): RouteDatabase? {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(
                            context.applicationContext,
                            RouteDatabase::class.java,
                            "route_database"
                        )
                            .build()
                }
            }
            return INSTANCE
        }
    }
}