package com.ryangunn.ridetest.database.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class Moves(
    val time: String,
    val distance: Double,
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
) {
    @PrimaryKey(autoGenerate = true)
    var moveId: Long = 0

    fun getDate(): String {
        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    fun getDistanceInString(): String {
        val df = DecimalFormat("#.00")
        return df.format(distance)
    }
}