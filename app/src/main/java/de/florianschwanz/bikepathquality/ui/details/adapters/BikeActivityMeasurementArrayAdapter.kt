package de.florianschwanz.bikepathquality.ui.details.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurement
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class BikeActivityMeasurementArrayAdapter(
    context: Context,
    private val itemClickListener: OnItemClickListener,
    data: List<BikeActivityMeasurement>
) : ArrayAdapter<BikeActivityMeasurement>(context, R.layout.bike_activity_measurement_item, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)

        val itemView = convertView ?: run {
            LayoutInflater.from(context)
                .inflate(R.layout.bike_activity_measurement_item, parent, false)
        }

        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvLon: TextView = itemView.findViewById(R.id.tvLon)
        val tvLat: TextView = itemView.findViewById(R.id.tvLat)
        val tvSpeed: TextView = itemView.findViewById(R.id.tvSpeed)
        val tvAccelerometerX: TextView = itemView.findViewById(R.id.tvAccelerometerX)
        val tvAccelerometerY: TextView = itemView.findViewById(R.id.tvAccelerometerY)
        val tvAccelerometerZ: TextView = itemView.findViewById(R.id.tvAccelerometerZ)

        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH)

        val resources = context.resources

        item?.let {
            itemView.setOnClickListener { _ ->
                itemClickListener.onBikeActivityMeasurementItemClicked(it, position)
            }

            tvTimestamp.text = sdf.format(Date.from(it.timestamp))
            tvLon.text =
                String.format(
                    resources.getString(R.string.bike_activity_measurement_lon_lat), it.lon
                )
            tvLat.text =
                String.format(
                    resources.getString(R.string.bike_activity_measurement_lon_lat), it.lat
                )
            tvSpeed.text = String.format(
                resources.getString(R.string.bike_activity_measurement_speed),
                it.speed
            )
            tvAccelerometerX.text = String.format(
                resources.getString(R.string.bike_activity_measurement_accelerometer),
                it.accelerometerX
            )
            tvAccelerometerY.text = String.format(
                resources.getString(R.string.bike_activity_measurement_accelerometer),
                it.accelerometerY
            )
            tvAccelerometerZ.text = String.format(
                resources.getString(R.string.bike_activity_measurement_accelerometer),
                it.accelerometerZ
            )
        }

        return itemView
    }

    private fun Instant.isToday() =
        this.truncatedTo(ChronoUnit.DAYS).equals(Instant.now().truncatedTo(ChronoUnit.DAYS))

    interface OnItemClickListener {
        fun onBikeActivityMeasurementItemClicked(
            bikeActivityMeasurement: BikeActivityMeasurement,
            position: Int
        )
    }
}