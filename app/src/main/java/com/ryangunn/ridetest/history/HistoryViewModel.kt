package com.ryangunn.ridetest.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryangunn.ridetest.database.MoveDatabase
import com.ryangunn.ridetest.database.model.Moves
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _moves: MutableStateFlow<List<Moves>> = MutableStateFlow(listOf())
    val moves: StateFlow<List<Moves>> = _moves
    fun getMoves(context: Context) {
        viewModelScope.launch {
            val moveslist = MoveDatabase.getDatabase(context)?.moveDAO()?.getAll()
            moveslist?.let {
                _moves.value = moveslist
            }
        }

    }
}