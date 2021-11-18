package com.ryangunn.ridetest.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryangunn.ridetest.database.RouteDatabase
import com.ryangunn.ridetest.database.model.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _route: MutableStateFlow<List<Route>> = MutableStateFlow(listOf())
    val route: StateFlow<List<Route>> = _route
    fun getRoute(context: Context) {
        viewModelScope.launch {
            val routeList = RouteDatabase.getDatabase(context)?.routeDAO()?.getAll()
            routeList?.let {
                _route.value = routeList
            }
        }

    }
}