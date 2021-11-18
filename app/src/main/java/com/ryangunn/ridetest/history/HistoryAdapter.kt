package com.ryangunn.ridetest.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryangunn.ridetest.database.model.Moves
import com.ryangunn.ridetest.databinding.ListItemMoveBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

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

    class HistoryViewHolder(val listItemMoveBinding: ListItemMoveBinding) :
        RecyclerView.ViewHolder(listItemMoveBinding.root) {
        fun bind(move: Moves) {
            listItemMoveBinding.apply {
                moveImageView.setImageBitmap(move.img)
                val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val moveDate = formatter.format(Date(move.timestamp))
                val dateString = "Date: $moveDate"
                val timeString = "Total Time: ${move.time}"
                val df = DecimalFormat("#.00")
                val mileString = df.format(move.distance)
                val totalDistanceString = "Total Distance: $mileString miles"
                dateTextView.text = dateString
                timeTextView.text = timeString
                distanceTextView.text = totalDistanceString
            }
        }
    }


}