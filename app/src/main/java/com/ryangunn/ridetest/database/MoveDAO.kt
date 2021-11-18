package com.ryangunn.ridetest.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ryangunn.ridetest.database.model.Moves

@Dao
interface MoveDAO {
    @Insert
    suspend fun insertMove(move: Moves): Long

    @Query("SELECT * FROM moves")
    suspend fun getAll(): List<Moves>

    @Query("SELECT * FROM moves WHERE moveId == :moveId")
    suspend fun getMove(moveId: Long): Moves
}