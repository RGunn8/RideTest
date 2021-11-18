package com.ryangunn.ridetest.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryangunn.ridetest.R
import com.ryangunn.ridetest.database.model.Route
import com.ryangunn.ridetest.databinding.ListItemMoveBinding

class HistoryAdapter(val routes: List<Route>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            ListItemMoveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    class HistoryViewHolder(private val listItemMoveBinding: ListItemMoveBinding) :
        RecyclerView.ViewHolder(listItemMoveBinding.root) {
        fun bind(route: Route) {
            listItemMoveBinding.apply {
                moveImageView.setImageBitmap(route.img)
                listItemMoveBinding.root.context.also {
                    dateTextView.text = it.getString(R.string.move_date, route.getDate())
                    timeTextView.text = it.getString(R.string.move_total_time, route.time)
                    distanceTextView.text =
                        it.getString(R.string.move_total_distance, route.getDistanceInString())
                }
            }
        }
    }


}