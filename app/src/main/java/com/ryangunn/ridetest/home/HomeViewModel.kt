package com.ryangunn.ridetest.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryangunn.ridetest.database.MoveDatabase
import com.ryangunn.ridetest.database.model.Moves
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class HomeViewModel() : ViewModel() {
    val newMoveIdFlow: MutableSharedFlow<Long> = MutableSharedFlow()

    fun insertMove(moves: Moves, context: Context) {
        viewModelScope.launch {
            val newMoveID = MoveDatabase.getDatabase(context)?.moveDAO()?.insertMove(moves)
            newMoveID?.let {
                newMoveIdFlow.emit(newMoveID)
            }
        }
    }
}