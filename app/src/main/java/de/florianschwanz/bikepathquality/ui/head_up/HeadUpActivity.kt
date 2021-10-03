package de.florianschwanz.bikepathquality.ui.head_up

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.common.collect.EvictingQueue
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.tracking.Accelerometer
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import de.florianschwanz.bikepathquality.databinding.ActivityHeadUpBinding
import kotlin.math.round
import kotlin.math.sqrt
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.ActionBar


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class HeadUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeadUpBinding
    private lateinit var clContainer: ConstraintLayout
    private lateinit var tvAccelerometer: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvSamples: TextView
    private lateinit var btnLeave: Button
    private lateinit var fullscreenContentControls: LinearLayout
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
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((application as BikePathQualityApplication).bikeActivityRepository)
    }

    private lateinit var viewModel: HeadUpActivityViewModel

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
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
        tvSamples = binding.tvSamples
        btnLeave = binding.btnLeave

        tvAccelerometer.setOnClickListener { toggle() }

        fullscreenContentControls = binding.fullscreenContentControls

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.btnLeave.setOnClickListener {
            finish()
        }

        val accelerometerEvictingQueue: EvictingQueue<Float> = EvictingQueue.create(750)
        val speedEvictingQueue: EvictingQueue<Float> = EvictingQueue.create(750)

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
            tvSamples.setTextColor(textColor)
            fullscreenContentControls.setBackgroundColor(statusBarColor)
            btnLeave.setTextColor(textColor)
        })

        viewModel.locationLiveData.observe(this, { location ->

            speedEvictingQueue.add(location.speed)

            val value = accelerometerEvictingQueue.average().toFloat()

            tvSpeed.text = String.format(resources.getString(R.string.speed), value * 3.6)
        })

        viewModel.activeBikeActivityWithSamples.observe(this, { bikeActivityWithSamples ->
            bikeActivityWithSamples?.let {
                tvSamples.text = resources.getQuantityString(
                    R.plurals.samples,
                    it.bikeActivitySamples.filterValid().size,
                    it.bikeActivitySamples.filterValid().size
                )
            }
        })
    }

    fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (round(this * multiplier) / multiplier).toFloat()
    }

    private fun Float.square(): Float = this * this

    private fun Float.squareRoot(): Float = sqrt(this)

    fun Accelerometer.rootMeanSquare() = ((x.square() + y.square() + z.square()) / 3).squareRoot()

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
        fullscreenContentControls.visibility = View.VISIBLE
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
    // Helpers
    //

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