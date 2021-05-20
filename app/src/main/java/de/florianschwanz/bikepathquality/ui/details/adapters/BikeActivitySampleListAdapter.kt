package de.florianschwanz.bikepathquality.ui.details.adapters

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import java.text.SimpleDateFormat
import java.util.*

class BikeActivitySampleListAdapter(
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<BikeActivitySampleListAdapter.BikeActivitySampleViewHolder>() {

    var data = listOf<BikeActivitySampleWithMeasurements>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BikeActivitySampleViewHolder {
        return BikeActivitySampleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BikeActivitySampleViewHolder, position: Int) {
        val current = data[position]
        holder.bind(current, position, context, itemClickListener)
    }

    class BikeActivitySampleViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val clBikeActivitySample: ConstraintLayout =
            itemView.findViewById(R.id.clBikeActivitySample)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvMeasurements: TextView = itemView.findViewById(R.id.tvMeasurements)
        private val tvSpeed: TextView = itemView.findViewById(R.id.tvSpeed)
        private val tvAccelerometer: TextView = itemView.findViewById(R.id.tvAccelerometer)

        private var sdf: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        fun bind(
            item: BikeActivitySampleWithMeasurements,
            position: Int,
            context: Context,
            itemClickListener: OnItemClickListener
        ) {
            val resources = itemView.context.resources

            itemView.setOnClickListener {
                itemClickListener.onBikeActivitySampleItemClicked(item)
            }

            clBikeActivitySample.setBackgroundColor(
                if (position % 2 == 1) {
                    when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_NO -> ContextCompat.getColor(context, R.color.transparent_light)
                        Configuration.UI_MODE_NIGHT_YES -> ContextCompat.getColor(context, R.color.transparent_dark)
                        else -> ContextCompat.getColor(context, R.color.transparent)
                    }
                } else {
                    ContextCompat.getColor(context, R.color.transparent)
                }
            )

            tvStartTime.text = sdf.format(Date.from(item.bikeActivitySample.timestamp))
            tvMeasurements.text = resources.getQuantityString(
                R.plurals.measurements,
                item.bikeActivityMeasurements.size,
                item.bikeActivityMeasurements.size
            )
            tvSpeed.text = String.format(
                resources.getString(R.string.bike_activity_sample_speed),
                item.bikeActivitySample.speed.times(3.6)
            )
            tvAccelerometer.text = String.format(
                resources.getString(R.string.bike_activity_sample_accelerometer),
                item.bikeActivityMeasurements.map {
                    (it.accelerometerX.square() + it.accelerometerY.square() + it.accelerometerZ.square()).squareRoot()
                }.average()
            )
        }

        fun Float.square(): Float = this * this
        fun Float.squareRoot(): Float = Math.sqrt(this.toDouble()).toFloat()

        /**
         * Retrieves theme color
         */
        private fun getThemeColorInHex(context: Context, @AttrRes attribute: Int): String {
            val outValue = TypedValue()
            context.theme.resolveAttribute(attribute, outValue, true)

            return String.format("#%06X", 0xFFFFFF and outValue.data)
        }

        companion object {
            fun create(parent: ViewGroup): BikeActivitySampleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.bike_activity_sample_item, parent, false)
                return BikeActivitySampleViewHolder(view)
            }
        }
    }

    interface OnItemClickListener {
        fun onBikeActivitySampleItemClicked(bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements)
    }
}