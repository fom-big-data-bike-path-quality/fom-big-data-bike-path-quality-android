package de.florianschwanz.bikepathquality.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.services.TrackingForegroundService
import de.florianschwanz.bikepathquality.ui.main.MainActivityViewModel

class SensorsFragment : Fragment() {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var toolbar: Toolbar

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create-view lifecycle phase
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sensors_fragment, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                ACTION_ENABLED_AUTOMATIC_TRACKING -> {
                    enableAutomaticTracking()
                }
                ACTION_DISABLED_AUTOMATIC_TRACKING -> {
                    disableAutomaticTracking()
                }
                else -> {
                }
            }

            false
        }

        return view
    }

    /**
     * Handles on-resume lifecycle phase
     */
    override fun onResume() {
        super.onResume()

        viewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.trackingServiceStatus.observe(viewLifecycleOwner, { trackingServiceStatus ->
            updateMenuItems(toolbar, trackingServiceStatus)
        })

        updateMenuItems(toolbar, TrackingForegroundService.status)
    }

    //
    // Helpers
    //

    /**
     * Updates menu items based on tracking service state
     */
    private fun updateMenuItems(toolbar: Toolbar, trackingServiceStatus: String) {
        toolbar.menu.removeItem(BikeActivitiesFragment.ACTION_ENABLED_AUTOMATIC_TRACKING)
        toolbar.menu.removeItem(BikeActivitiesFragment.ACTION_DISABLED_AUTOMATIC_TRACKING)

        when(trackingServiceStatus) {
            TrackingForegroundService.STATUS_STARTED -> {
                toolbar.menu.add(
                    Menu.NONE,
                    BikeActivitiesFragment.ACTION_DISABLED_AUTOMATIC_TRACKING,
                    Menu.NONE,
                    getString(R.string.action_disable_automatic_tracking)
                )
            }
            TrackingForegroundService.STATUS_STOPPED -> {
                toolbar.menu.add(
                    Menu.NONE,
                    BikeActivitiesFragment.ACTION_ENABLED_AUTOMATIC_TRACKING,
                    Menu.NONE,
                    getString(R.string.action_enable_automatic_tracking)
                )
            }
        }
    }

    /**
     * Enables automatic tracking
     */
    private fun enableAutomaticTracking() {
        val trackingForegroundServiceIntent =
            Intent(requireActivity(), TrackingForegroundService::class.java)
        trackingForegroundServiceIntent.action = TrackingForegroundService.ACTION_START
        ContextCompat.startForegroundService(requireActivity(), trackingForegroundServiceIntent)
    }

    /**
     * Disables automatic tracking
     */
    private fun disableAutomaticTracking() {
        val trackingForegroundServiceIntent =
            Intent(requireActivity(), TrackingForegroundService::class.java)
        trackingForegroundServiceIntent.action = TrackingForegroundService.ACTION_STOP
        ContextCompat.startForegroundService(
            requireActivity(),
            trackingForegroundServiceIntent
        )
    }

    companion object {
        const val ACTION_ENABLED_AUTOMATIC_TRACKING = 0
        const val ACTION_DISABLED_AUTOMATIC_TRACKING = 1
    }
}