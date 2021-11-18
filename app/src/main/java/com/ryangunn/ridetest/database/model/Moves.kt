package com.ryangunn.ridetest.database.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Moves(
    val time: String,
    val distance: Double,
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
) {
    @PrimaryKey(autoGenerate = true)
    var moveId: Long = 0
}