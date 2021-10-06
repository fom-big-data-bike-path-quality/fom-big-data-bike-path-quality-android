package de.florianschwanz.bikepathquality.ui.head_up

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.common.collect.EvictingQueue
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.tracking.Accelerometer
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityTrackingType
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModel
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModelFactory
import de.florianschwanz.bikepathquality.databinding.ActivityHeadUpBinding
import de.florianschwanz.bikepathquality.services.TrackingForegroundService
import java.time.Instant
import kotlin.math.round
import kotlin.math.sqrt

class HeadUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeadUpBinding
    private lateinit var clContainer: ConstraintLayout
    private lateinit var tvAccelerometer: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvSpeedMax: TextView
    private lateinit var clFooter: ConstraintLayout
    private lateinit var tvSamples: TextView


    private lateinit var clBack: ConstraintLayout
    private lateinit var ivBack: ImageView
    private lateinit var tvBack: TextView
    private lateinit var clStartStop: ConstraintLayout
    private lateinit var ivStartStop: ImageView
    private lateinit var tvStartStop: TextView

    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            tvAccelerometer.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            tvAccelerometer.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    private var speedMax = 0.0f

    private lateinit var viewModel: HeadUpActivityViewModel

    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((application as BikePathQualityApplication).bikeActivityRepository)
    }

    private val userDataViewModel: UserDataViewModel by viewModels {
        UserDataViewModelFactory((application as BikePathQualityApplication).userDataRepository)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHeadUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        clContainer = binding.flContainer
        tvAccelerometer = binding.tvAccelerometer
        tvSpeed = binding.tvSpeed
        tvSpeedMax = binding.tvSpeedMax
        tvSamples = binding.tvSamples
        clFooter = binding.clFooter
        clBack = binding.clBack
        ivBack = binding.ivBack
        tvBack = binding.tvBack
        clStartStop = binding.clStartStop
        ivStartStop = binding.ivStartStop
        tvStartStop = binding.tvStartStop

        tvAccelerometer.setOnClickListener { toggle() }

        binding.clBack.setOnClickListener {
            finish()
        }
        binding.clStartStop.setOnClickListener {
            val activeBikeActivityWithSamples = viewModel.activeBikeActivityWithSamples.value
            val userData = viewModel.userData.value

            if (userData != null) {
                if (activeBikeActivityWithSamples?.bikeActivity != null) {
                    disableAutomaticTracking()
                    bikeActivityViewModel.update(
                        activeBikeActivityWithSamples.bikeActivity.copy(
                            endTime = Instant.now()
                        )
                    )
                } else {
                    enableManualTracking()

                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

                    bikeActivityViewModel.insert(
                        BikeActivity(
                            trackingType = BikeActivityTrackingType.MANUAL,
                            phonePosition = sharedPreferences.getString(
                                resources.getString(R.string.setting_phone_position),
                                null
                            ),
                            bikeType = sharedPreferences.getString(
                                resources.getString(R.string.setting_bike_type),
                                null
                            )
                        )
                    )
                }
            }
        }

        val accelerometerEvictingQueue: EvictingQueue<Float> = EvictingQueue.create(750)

        bikeActivityViewModel.activeBikeActivityWithSamples.observe(
            this,
            { bikeActivityWithSamples ->
                viewModel.activeBikeActivityWithSamples.value = bikeActivityWithSamples
            })

        viewModel = ViewModelProvider(this).get(HeadUpActivityViewModel::class.java)
        viewModel.accelerometerLiveData.observe(this, { accelerometer ->

            accelerometerEvictingQueue.add(accelerometer.rootMeanSquare())

            val MAX_VALUE = 10.0f

            val statusBarColorGood = ContextCompat.getColor(this, R.color.green_800)
            val statusBarColorBad = ContextCompat.getColor(this, R.color.red_800)
            val backgroundColorGood = ContextCompat.getColor(this, R.color.green_600)
            val backgroundColorBad = ContextCompat.getColor(this, R.color.red_600)
            val textColorGood = ContextCompat.getColor(this, R.color.green_a200)
            val textColorBad = ContextCompat.getColor(this, R.color.red_a200)

            val value = accelerometerEvictingQueue.average().toFloat()
            val percentage = when {
                value < 0 -> 0.0f
                value > MAX_VALUE -> 1.0f
                else -> (1 / MAX_VALUE) * value
            }

            val statusBarColor =
                ArgbEvaluator().evaluate(
                    percentage, statusBarColorGood, statusBarColorBad
                ) as Int
            val backgroundColor =
                ArgbEvaluator().evaluate(
                    percentage, backgroundColorGood, backgroundColorBad
                ) as Int
            val textColor =
                ArgbEvaluator().evaluate(percentage, textColorGood, textColorBad) as Int

            window.statusBarColor = statusBarColor
            supportActionBar?.setBackgroundDrawable(ColorDrawable(statusBarColor))

            clContainer.setBackgroundColor(backgroundColor)
            tvAccelerometer.setTextColor(textColor)
            tvAccelerometer.text = value.round(1).toString()
            tvSpeed.setTextColor(textColor)
            tvSpeedMax.setTextColor(textColor)
            tvSamples.setTextColor(textColor)
            clFooter.setBackgroundColor(statusBarColor)
            ivBack.imageTintList = ColorStateList.valueOf(textColor)
            tvBack.setTextColor(textColor)
            ivStartStop.imageTintList = ColorStateList.valueOf(textColor)
            tvStartStop.setTextColor(textColor)
        })
        viewModel.locationLiveData.observe(this, { location ->
            if (location.speed > speedMax) {
                speedMax = location.speed
            }

            tvSpeed.text = String.format(resources.getString(R.string.speed), location.speed * 3.6)
            tvSpeedMax.text = String.format(resources.getString(R.string.speed_max), speedMax * 3.6)
        })
        viewModel.activeBikeActivityWithSamples.observe(this, { bikeActivityWithSamples ->
            bikeActivityWithSamples?.let {
                tvSamples.text = resources.getQuantityString(
                    R.plurals.samples,
                    it.bikeActivitySamples.filterValid().size,
                    it.bikeActivitySamples.filterValid().size
                )
                ivStartStop.setImageResource(R.drawable.ic_baseline_stop_48)
                tvStartStop.text = resources.getString(R.string.action_stop_activity)
            } ?: run {
                tvSamples.text = resources.getString(R.string.not_tracking)
                ivStartStop.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                tvStartStop.text = resources.getString(R.string.action_start_activity)
            }
        })
        userDataViewModel.singleUserData().observe(this, { userData ->
            viewModel.userData.value = userData
        })

        speedMax = 0.0f
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            tvAccelerometer.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            tvAccelerometer.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    //
    // Helpers (tracking)
    //

    /**
     * Enables manual tracking
     */
    private fun enableManualTracking() {
        val trackingForegroundServiceIntent =
            Intent(this, TrackingForegroundService::class.java)
        trackingForegroundServiceIntent.action = TrackingForegroundService.ACTION_START_MANUALLY
        ContextCompat.startForegroundService(this, trackingForegroundServiceIntent)
    }

    /**
     * Disables automatic tracking
     */
    private fun disableAutomaticTracking() {
        val trackingForegroundServiceIntent =
            Intent(this, TrackingForegroundService::class.java)
        trackingForegroundServiceIntent.action = TrackingForegroundService.ACTION_STOP
        ContextCompat.startForegroundService(
            this,
            trackingForegroundServiceIntent
        )
    }

    //
    // Helpers
    //

    fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (round(this * multiplier) / multiplier).toFloat()
    }

    private fun Float.square(): Float = this * this

    private fun Float.squareRoot(): Float = sqrt(this)

    fun Accelerometer.rootMeanSquare() = ((x.square() + y.square() + z.square()) / 3).squareRoot()

    private fun List<BikeActivitySample>.filterValid() =
        this.filter { it.lon != 0.0 || it.lat != 0.0 }

    companion object {

        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}