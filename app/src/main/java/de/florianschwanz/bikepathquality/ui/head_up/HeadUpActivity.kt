package de.florianschwanz.bikepathquality.ui.head_up

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.common.collect.EvictingQueue
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.databinding.ActivityHeadUpBinding
import kotlin.math.round
import kotlin.math.sqrt

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class HeadUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeadUpBinding
    private lateinit var flContainer: FrameLayout
    private lateinit var tvAccelerometer: TextView
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

        flContainer = binding.flContainer

        // Set up the user interaction to manually show or hide the system UI.
        tvAccelerometer = binding.tvAccelerometer
        tvAccelerometer.setOnClickListener { toggle() }

        fullscreenContentControls = binding.fullscreenContentControls

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.btnLeave.setOnTouchListener(delayHideTouchListener)

        val accelerometerEvictingQueueX: EvictingQueue<Float> = EvictingQueue.create(500)
        val accelerometerEvictingQueueY: EvictingQueue<Float> = EvictingQueue.create(500)
        val accelerometerEvictingQueueZ: EvictingQueue<Float> = EvictingQueue.create(500)

        viewModel = ViewModelProvider(this).get(HeadUpActivityViewModel::class.java)
        viewModel.accelerometerLiveData.observe(this, {

            accelerometerEvictingQueueX.add(it.x)
            accelerometerEvictingQueueY.add(it.y)
            accelerometerEvictingQueueZ.add(it.z)

            val x = accelerometerEvictingQueueX.average()
            val y = accelerometerEvictingQueueY.average()
            val z = accelerometerEvictingQueueZ.average()
            val rootMeanSquare = ((x.square() + y.square() + z.square()) / 3).squareRoot()

            val MAX_VALUE = 15.0f

            val backgroundColorGood = ContextCompat.getColor(this, R.color.green_600)
            val backgroundColorBad = ContextCompat.getColor(this, R.color.red_600)
            val textColorGood = ContextCompat.getColor(this, R.color.green_a200)
            val textColorBad = ContextCompat.getColor(this, R.color.red_a200)

            val percentage = when {
                rootMeanSquare < 0 -> 0.0f
                rootMeanSquare > MAX_VALUE -> 1.0f
                else -> (1 / MAX_VALUE) * rootMeanSquare
            }

            val backgroundColor =
                ArgbEvaluator().evaluate(
                    percentage.toFloat(),
                    backgroundColorGood,
                    backgroundColorBad
                ) as Int
            val textColor =
                ArgbEvaluator().evaluate(percentage.toFloat(), textColorGood, textColorBad) as Int

            flContainer.setBackgroundColor(backgroundColor)
            tvAccelerometer.setTextColor(textColor)
            tvAccelerometer.text = rootMeanSquare.round(2).toString()
        })
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    private fun Double.square(): Double = this * this

    private fun Double.squareRoot(): Double = sqrt(this)

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
        fullscreenContentControls.visibility = View.GONE
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