package de.florianschwanz.bikepathquality.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.BuildConfig
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModel
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModelFactory
import de.florianschwanz.bikepathquality.services.TrackingForegroundService


class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    //
    // Lifecycle phases
    //

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

        private val userDataViewModel: UserDataViewModel by viewModels {
            UserDataViewModelFactory((requireActivity().application as BikePathQualityApplication).userDataRepository)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            userDataViewModel.singleUserData().observe(this, { userData ->
                findPreference<EditTextPreference>("user_id")?.summary = userData.uid
            })

            findPreference<EditTextPreference>("app_version")?.summary = BuildConfig.VERSION_NAME
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