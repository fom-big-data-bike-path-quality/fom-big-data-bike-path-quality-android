package de.florianschwanz.bikepathquality.ui.details.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurement
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityStatus
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class BikeActivitySampleListAdapter(
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<BikeActivitySampleListAdapter.BikeActivitySampleViewHolder>(),
    BikeActivityMeasurementArrayAdapter.OnItemClickListener {

    var data = listOf<BikeActivitySampleWithMeasurements>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var focus: BikeActivitySampleWithMeasurements? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var bikeActivity: BikeActivity? = null
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
        holder.bind(current, position, focus, bikeActivity, context, itemClickListener, this)
    }

    override fun onBikeActivityMeasurementItemClicked(
        bikeActivityMeasurement: BikeActivityMeasurement,
        position: Int
    ) {
    }

    class BikeActivitySampleViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val clBikeActivitySample: ConstraintLayout =
            itemView.findViewById(R.id.clBikeActivitySample)
        private val ivBike: ImageView = itemView.findViewById(R.id.ivBike)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMeasurements: TextView = itemView.findViewById(R.id.tvMeasurements)
        private val tvSpeed: TextView = itemView.findViewById(R.id.tvSpeed)
        private val btnSurfaceType: MaterialButton = itemView.findViewById(R.id.btnSurfaceType)
        private val tvAccelerometer: TextView = itemView.findViewById(R.id.tvAccelerometer)
        private val clBikeActivityMeasurements: ConstraintLayout =
            itemView.findViewById(R.id.clBikeActivityMeasurements)
        private val lvBikeActivityMeasurements: ListView =
            itemView.findViewById(R.id.lvBikeActivityMeasurements)

        private var sdf: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        fun bind(
            item: BikeActivitySampleWithMeasurements,
            position: Int,
            focus: BikeActivitySampleWithMeasurements?,
            bikeActivity: BikeActivity?,
            context: Context,
            itemClickListener: OnItemClickListener,
            bikeActivityMeasurementItemClickListener: BikeActivityMeasurementArrayAdapter.OnItemClickListener
        ) {
            val resources = context.resources
            val isValid = isValid(bikeActivity, item)

            clBikeActivitySample.setOnClickListener {
                itemClickListener.onBikeActivitySampleItemClicked(item, position)
            }

            if (isValid) {
                clBikeActivitySample.setBackgroundColor(buildBackgroundColor(context, item, focus))
            } else {
                clBikeActivitySample.setBackgroundResource(
                    buildBackgroundResourceInvalid(context, item, focus)
                )
            }

            ivBike.imageTintList = ColorStateList.valueOf(buildTextColor(context, item, focus))
            tvStartTime.setTextColor(buildTextColor(context, item, focus))
            tvTitle.setTextColor(buildTextColor(context, item, focus))
            tvTitle.text = when {
                isValid -> resources.getString(R.string.bike_activity_sample)
                !item.isLabeled() -> resources.getString(R.string.bike_activity_sample_unlabled)
                !item.isAboveSpeedLimit() -> resources.getString(R.string.bike_activity_sample_too_slow)
                else -> resources.getString(R.string.bike_activity_sample_invalid)
            }

            tvMeasurements.setTextColor(buildTextColor(context, item, focus))
            tvSpeed.setTextColor(buildTextColor(context, item, focus))
            btnSurfaceType.setTextColor(buildTextColor(context, item, focus))
            btnSurfaceType.iconTint = ColorStateList.valueOf(buildTextColor(context, item, focus))
            tvAccelerometer.setTextColor(buildTextColor(context, item, focus))

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
            btnSurfaceType.isEnabled = bikeActivity?.uploadStatus != BikeActivityStatus.UPLOADED
            btnSurfaceType.setOnClickListener {
                itemClickListener.onBikeActivitySampleSurfaceTypeClicked(item, position)
            }
            btnSurfaceType.visibility =
                if (item.bikeActivitySample.surfaceType != null || item.bikeActivitySample.uid == focus?.bikeActivitySample?.uid) View.VISIBLE else View.INVISIBLE
            btnSurfaceType.text =
                item.bikeActivitySample.surfaceType
                    ?: resources.getString(
                        R.string.empty
                    )
            tvAccelerometer.text = String.format(
                resources.getString(R.string.bike_activity_sample_accelerometer),
                item.bikeActivityMeasurements.map { it.rootMeanSquare() }.average()
            )

            clBikeActivityMeasurements.maxHeight = 0
//            clBikeActivityMeasurements.maxHeight =
//                if (item.bikeActivitySample.uid == focus?.bikeActivitySample?.uid) 5000 else 0
            clBikeActivityMeasurements.setBackgroundColor(
                when {
                    position % 2 == 1 -> {
                        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_NO -> ContextCompat.getColor(
                                context,
                                R.color.transparent_light
                            )
                            Configuration.UI_MODE_NIGHT_YES -> ContextCompat.getColor(
                                context,
                                R.color.transparent_dark
                            )
                            else -> ContextCompat.getColor(context, R.color.transparent)
                        }
                    }
                    else -> {
                        ContextCompat.getColor(context, R.color.transparent)
                    }
                }
            )
            lvBikeActivityMeasurements.adapter = BikeActivityMeasurementArrayAdapter(
                context,
                bikeActivityMeasurementItemClickListener,
                item.bikeActivityMeasurements
            )
        }

        //
        // Helpers
        //

        private fun BikeActivityMeasurement.rootMeanSquare() = ((accelerometerX.square() + accelerometerY.square() + accelerometerZ.square()) / 3).squareRoot()

        private fun Float.square(): Float = this * this

        private fun Float.squareRoot(): Float = sqrt(this)

        private fun isValid(
            bikeActivity: BikeActivity?,
            bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements
        ) =
            (bikeActivity?.isLabeled() ?: false || bikeActivitySampleWithMeasurements.isLabeled()) && bikeActivitySampleWithMeasurements.isAboveSpeedLimit()

        private fun BikeActivity.isLabeled() =
            this.surfaceType != "mixed"

        private fun BikeActivitySampleWithMeasurements.isLabeled() =
            this.bikeActivitySample.surfaceType != "mixed"

        private fun BikeActivitySampleWithMeasurements.isAboveSpeedLimit() =
            this.bikeActivityMeasurements.all {
                it.speed.times(3.6) > 5
            }

        /**
         * Builds background color based on whether the item is focussed or not
         */
        private fun buildBackgroundColor(
            context: Context,
            item: BikeActivitySampleWithMeasurements,
            focus: BikeActivitySampleWithMeasurements?
        ) = when {
            item.bikeActivitySample.uid == focus?.bikeActivitySample?.uid -> {
                Color.parseColor(getThemeColorInHex(context, R.attr.colorSecondary))
            }
            layoutPosition % 2 == 1 -> {
                when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> ContextCompat.getColor(
                        context,
                        R.color.transparent_light
                    )
                    Configuration.UI_MODE_NIGHT_YES -> ContextCompat.getColor(
                        context,
                        R.color.transparent_dark
                    )
                    else -> ContextCompat.getColor(context, R.color.transparent)
                }
            }
            else -> {
                ContextCompat.getColor(context, R.color.transparent)
            }
        }

        /**
         * Builds background resource for invalid entries
         */
        private fun buildBackgroundResourceInvalid(
            context: Context,
            item: BikeActivitySampleWithMeasurements,
            focus: BikeActivitySampleWithMeasurements?
        ) = when (item.bikeActivitySample.uid) {
            focus?.bikeActivitySample?.uid -> {
                R.drawable.bike_activity_sample_focus
            }
            else -> {
                when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> R.drawable.bike_activity_sample_invalid
                    Configuration.UI_MODE_NIGHT_YES -> R.drawable.bike_activity_sample_invalid_night
                    else -> ContextCompat.getColor(context, R.color.transparent)
                }
            }
        }


        /**
         * Builds text color based on whether the item is focussed or not
         */
        private fun buildTextColor(
            context: Context,
            item: BikeActivitySampleWithMeasurements,
            focus: BikeActivitySampleWithMeasurements?
        ) = when (item.bikeActivitySample.uid) {
            focus?.bikeActivitySample?.uid -> {
                Color.parseColor(getThemeColorInHex(context, R.attr.colorOnSecondary))
            }
            else -> {
                Color.parseColor(getThemeColorInHex(context, R.attr.colorOnSurface))
            }
        }

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
        fun onBikeActivitySampleItemClicked(
            bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements,
            position: Int
        )

        fun onBikeActivitySampleSurfaceTypeClicked(
            bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements,
            position: Int
        )
    }
}