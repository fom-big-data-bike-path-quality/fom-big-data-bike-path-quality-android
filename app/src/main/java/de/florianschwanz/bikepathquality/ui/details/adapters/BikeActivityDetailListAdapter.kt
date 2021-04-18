package de.florianschwanz.bikepathquality.ui.details.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityDetail
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class BikeActivityDetailListAdapter() :
    RecyclerView.Adapter<BikeActivityDetailListAdapter.BikeActivityDetailViewHolder>() {

    var data = listOf<BikeActivityDetail>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BikeActivityDetailViewHolder {
        return BikeActivityDetailViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BikeActivityDetailViewHolder, position: Int) {
        val current = data[position]
        holder.bind(current)
    }

    class BikeActivityDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        private var sdfShort: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        private var sdf: SimpleDateFormat = SimpleDateFormat("MMM dd HH:mm:ss", Locale.ENGLISH)

        fun bind(item: BikeActivityDetail) {
            val resources = itemView.context.resources

            tvTimestamp.text =
                if (item.timestamp.isToday()) sdfShort.format(Date.from(item.timestamp)) else sdf.format(
                    Date.from(item.timestamp)
                )
            tvMessage.text = String.format(
                resources.getString(R.string.activity_detail),
                item.lon,
                item.lat,
                item.accelerometerX,
                item.accelerometerY,
                item.accelerometerZ
            )
        }

        private fun Instant.isToday() =
            this.truncatedTo(ChronoUnit.DAYS).equals(Instant.now().truncatedTo(ChronoUnit.DAYS))

        companion object {
            fun create(parent: ViewGroup): BikeActivityDetailViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.bike_activity_details_item, parent, false)
                return BikeActivityDetailViewHolder(view)
            }
        }
    }
}