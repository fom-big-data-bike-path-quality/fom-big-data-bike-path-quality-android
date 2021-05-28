package de.florianschwanz.bikepathquality.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.services.TrackingForegroundService

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        when (key) {
            "tracking_automatic" -> {
                if (sharedPreferences?.getBoolean(key, false) == true) {
                    enableAutomaticTracking()
                } else {
                    disableAutomaticTracking()
                }
            }
        }
    }

    //
    // Helpers
    //

    /**
     * Enables automatic tracking
     */
    private fun enableAutomaticTracking() {
        val trackingForegroundServiceIntent = Intent(this, TrackingForegroundService::class.java)
        trackingForegroundServiceIntent.action = TrackingForegroundService.ACTION_START
        ContextCompat.startForegroundService(this, trackingForegroundServiceIntent)
    }

    /**
     * Disables automatic tracking
     */
    private fun disableAutomaticTracking() {
        val trackingForegroundServiceIntent = Intent(this, TrackingForegroundService::class.java)
        trackingForegroundServiceIntent.action = TrackingForegroundService.ACTION_STOP
        ContextCompat.startForegroundService(this, trackingForegroundServiceIntent)
    }
}