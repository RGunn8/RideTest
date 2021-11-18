package com.ryangunn.ridetest.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ryangunn.ridetest.database.model.Route

@Dao
interface RouteDAO {
    @Insert
    suspend fun insertRoute(move: Route): Long

    @Query("SELECT * FROM route ORDER BY routeId asc")
    suspend fun getAll(): List<Route>

    @Query("SELECT * FROM route WHERE routeId == :moveId")
    suspend fun getRoute(moveId: Long): Route
}