package de.florianschwanz.bikepathquality.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.storage.BikeActivity
import java.text.SimpleDateFormat
import java.util.*

class BikeActivityListAdapter(val context: Context?):
    ListAdapter<BikeActivity, BikeActivityListAdapter.BikeActivityViewHolder>(BikeActivityComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeActivityViewHolder {
        return BikeActivityViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BikeActivityViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class BikeActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

        var sdf: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        fun bind(item: BikeActivity) {

            item.endTime?.run {
                val diff = item.endTime.toEpochMilli() - item.startTime.toEpochMilli()
                val duration = diff / 1000 / 60
                tvDuration.text = duration.toString()
            }

            tvStartTime.text = sdf.format(Date.from(item.startTime))
        }

        companion object {
            fun create(parent: ViewGroup): BikeActivityViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.activities_item, parent, false)
                return BikeActivityViewHolder(view)
            }
        }
    }

    class BikeActivityComparator : DiffUtil.ItemCallback<BikeActivity>() {
        override fun areItemsTheSame(oldItem: BikeActivity, newItem: BikeActivity): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: BikeActivity, newItem: BikeActivity): Boolean {
            return oldItem.startTime == newItem.startTime
        }
    }
}