package de.florianschwanz.bikepathquality.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.storage.log_entry.LogEntry
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class LogEntryListAdapter : RecyclerView.Adapter<LogEntryListAdapter.LogEntryViewHolder>() {

    var data = listOf<LogEntry>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogEntryViewHolder {
        return LogEntryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: LogEntryViewHolder, position: Int) {
        val current = data[position]
        holder.bind(current)
    }

    class LogEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        private var sdfShort: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        private var sdf: SimpleDateFormat = SimpleDateFormat("yyyy MMM dd HH:mm:ss", Locale.ENGLISH)

        fun bind(item: LogEntry) {
            tvTimestamp.text =
                if (item.timestamp.isToday()) sdfShort.format(Date.from(item.timestamp)) else sdf.format(
                    Date.from(item.timestamp)
                )
            tvMessage.text = item.message
        }

        private fun Instant.isToday() =
            this.truncatedTo(ChronoUnit.DAYS).equals(Instant.now().truncatedTo(ChronoUnit.DAYS))

        companion object {
            fun create(parent: ViewGroup): LogEntryViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.log_item, parent, false)
                return LogEntryViewHolder(view)
            }
        }
    }
}