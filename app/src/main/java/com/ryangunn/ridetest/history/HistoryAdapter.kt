package com.ryangunn.ridetest.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryangunn.ridetest.R
import com.ryangunn.ridetest.database.model.Moves
import com.ryangunn.ridetest.databinding.ListItemMoveBinding

class HistoryAdapter(val moves: List<Moves>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            ListItemMoveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(moves[position])
    }

    override fun getItemCount(): Int {
        return moves.size
    }

    class HistoryViewHolder(private val listItemMoveBinding: ListItemMoveBinding) :
        RecyclerView.ViewHolder(listItemMoveBinding.root) {
        fun bind(move: Moves) {
            listItemMoveBinding.apply {
                moveImageView.setImageBitmap(move.img)
                listItemMoveBinding.root.context.also {
                    dateTextView.text = it.getString(R.string.move_date, move.getDate())
                    timeTextView.text = it.getString(R.string.move_total_time, move.time)
                    distanceTextView.text =
                        it.getString(R.string.move_total_distance, move.getDistanceInString())
                }
            }
        }
    }


}