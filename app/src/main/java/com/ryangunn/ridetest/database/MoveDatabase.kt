package com.ryangunn.ridetest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ryangunn.ridetest.database.model.Moves
import com.ryangunn.ridetest.util.BitmapRoomConverter

@Database(entities = [Moves::class], version = 1)
@TypeConverters(BitmapRoomConverter::class)
abstract class MoveDatabase : RoomDatabase() {
    abstract fun moveDAO(): MoveDAO

    companion object {
        private var INSTANCE: MoveDatabase? = null
        fun getDatabase(context: Context): MoveDatabase? {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(
                            context.applicationContext,
                            MoveDatabase::class.java,
                            "move_database"
                        )
                            .build()
                }
            }
            return INSTANCE
        }
    }
}