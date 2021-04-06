package de.florianschwanz.bikepathquality.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.storage.LogEntry
import java.text.SimpleDateFormat
import java.util.*

class LogEntryListAdapter :
    ListAdapter<LogEntry, LogEntryListAdapter.LogEntryViewHolder>(LogEntryComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogEntryViewHolder {
        return LogEntryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: LogEntryViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class LogEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        var sdf: SimpleDateFormat = SimpleDateFormat("yyyy MMM dd HH:mm:ss", Locale.ENGLISH)

        fun bind(item: LogEntry) {
            tvTimestamp.text = sdf.format(item.timestamp.time)
            tvMessage.text = item.message
        }

        companion object {
            fun create(parent: ViewGroup): LogEntryViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.log_item, parent, false)
                return LogEntryViewHolder(view)
            }
        }
    }

    class LogEntryComparator : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem.message == newItem.message
        }
    }
}