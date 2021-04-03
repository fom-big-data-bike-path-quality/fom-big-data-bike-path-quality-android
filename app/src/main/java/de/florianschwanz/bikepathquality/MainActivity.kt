package de.florianschwanz.bikepathquality

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import de.florianschwanz.bikepathquality.fragments.ActivityTransitionViewModel
import de.florianschwanz.bikepathquality.logger.LogFragment
import de.florianschwanz.bikepathquality.model.ActivityTransitionModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private var mLogFragment: LogFragment? = null

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        mLogFragment = supportFragmentManager.findFragmentById(R.id.log_fragment) as LogFragment?

        printToScreen("App initialized.")
    }

    //
    // Actions
    //

    /**
     * Handles click on activity recognition button
     */
    private fun requestPermissionActivityRecognition() {

        // Enable/Disable activity tracking and ask for permissions if needed
        if (!activityRecognitionPermissionApproved()) {
            // Request permission and start activity for result. If the permission is approved, we
            // want to make sure we start activity recognition tracking
            val startIntent = Intent(this, PermissionRationalActivity::class.java)

            @Suppress("DEPRECATION")
            startActivityForResult(startIntent, 0)
        }
    }

    //
    // Helpers
    //

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private fun activityRecognitionPermissionApproved(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
    }

    /**
     * Prints message to screen
     */
    private fun printToScreen(message: String) {
        mLogFragment?.logView?.println(message)
        Log.d(TAG, message)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
