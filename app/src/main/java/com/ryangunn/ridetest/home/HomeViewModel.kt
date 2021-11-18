package com.ryangunn.ridetest.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryangunn.ridetest.database.RouteDatabase
import com.ryangunn.ridetest.database.model.Route
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class HomeViewModel() : ViewModel() {
    val newRouteIdFlow: MutableSharedFlow<Long> = MutableSharedFlow()

    fun insertRoute(route: Route, context: Context) {
        viewModelScope.launch {
            val newRouteID = RouteDatabase.getDatabase(context)?.routeDAO()?.insertRoute(route)
            newRouteID?.let {
                newRouteIdFlow.emit(newRouteID)
            }
        }
    }
}