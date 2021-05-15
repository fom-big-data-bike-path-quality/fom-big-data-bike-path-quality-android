package de.florianschwanz.bikepathquality.ui.details.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import java.text.SimpleDateFormat
import java.util.*

class BikeActivitySampleListAdapter(private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<BikeActivitySampleListAdapter.BikeActivitySampleViewHolder>() {

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
        holder.bind(current, itemClickListener)
    }

    class BikeActivitySampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvMeasurements: TextView = itemView.findViewById(R.id.tvMeasurements)
        private val tvSpeed: TextView = itemView.findViewById(R.id.tvSpeed)
        private val tvAccelerometer: TextView = itemView.findViewById(R.id.tvAccelerometer)

        private var sdf: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        fun bind(item: BikeActivitySampleWithMeasurements, itemClickListener: OnItemClickListener) {
            itemView.setOnClickListener {
                itemClickListener.onBikeActivitySampleItemClicked(item)
            }

            val resources = itemView.context.resources

            tvStartTime.text = sdf.format(Date.from(item.bikeActivitySample.timestamp))
            tvMeasurements.text = resources.getQuantityString(
                R.plurals.measurements,
                item.bikeActivityMeasurements.size,
                item.bikeActivityMeasurements.size
            )
            tvSpeed.text = String.format(resources.getString(R.string.bike_activity_sample_speed), item.bikeActivitySample.speed.times(3.6))
            tvAccelerometer.text = String.format(resources.getString(R.string.bike_activity_sample_accelerometer), item.bikeActivityMeasurements.map {
                (it.accelerometerX.square() + it.accelerometerY.square() + it.accelerometerZ.square()).squareRoot()
            }.average())
        }

        fun Float.square(): Float = this * this
        fun Float.squareRoot(): Float = Math.sqrt(this.toDouble()).toFloat()

        companion object {
            fun create(parent: ViewGroup): BikeActivitySampleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.bike_activity_sample_item, parent, false)
                return BikeActivitySampleViewHolder(view)
            }
        }
    }

    interface OnItemClickListener{
        fun onBikeActivitySampleItemClicked(bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements)
    }
}