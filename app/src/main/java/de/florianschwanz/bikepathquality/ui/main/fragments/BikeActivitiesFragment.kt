package de.florianschwanz.bikepathquality.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.*
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.services.TrackingForegroundService
import de.florianschwanz.bikepathquality.ui.details.BikeActivityDetailsActivity
import de.florianschwanz.bikepathquality.ui.main.MainActivity
import de.florianschwanz.bikepathquality.ui.main.MainActivityViewModel
import de.florianschwanz.bikepathquality.ui.main.adapters.BikeActivityListAdapter
import java.time.Instant

class BikeActivitiesFragment : Fragment(), BikeActivityListAdapter.OnItemClickListener {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var toolbar: Toolbar

    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((requireActivity().application as BikePathQualityApplication).logEntryRepository)
    }
    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((requireActivity().application as BikePathQualityApplication).bikeActivityRepository)
    }

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
        viewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.bike_activities_fragment, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvActivities)
        val clStartStop = view.findViewById<ConstraintLayout>(R.id.clStartStop)
        val ivStartStop = view.findViewById<ImageView>(R.id.ivStartStop)
        val tvStartStop = view.findViewById<TextView>(R.id.tvStartStop)

        val adapter = BikeActivityListAdapter(this)

        toolbar = view.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_activities_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clear -> {
                    bikeActivityViewModel.deleteAll()
                }
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
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        clStartStop.setOnClickListener {

            val bikeActivity = viewModel.activeBikeActivity.value

            if (bikeActivity != null) {
                log("Stop manually")
                disableAutomaticTracking()
                bikeActivityViewModel.update(bikeActivity.copy(endTime = Instant.now()))
            } else {
                log("Start manually")
                enableManualTracking()
                bikeActivityViewModel.insert(BikeActivity(trackingType = BikeActivityTrackingType.MANUAL))
            }
        }

        bikeActivityViewModel.allBikeActivitiesWithSamples.observe(viewLifecycleOwner, {
            adapter.data = it
            if (adapter.data.isNotEmpty()) {
                recyclerView.smoothScrollToPosition(adapter.data.size - 1)
            }
        })

        bikeActivityViewModel.activeBikeActivity.observe(viewLifecycleOwner, { bikeActivity ->

            viewModel.activeBikeActivity.value = bikeActivity

            if (bikeActivity != null) {
                ivStartStop.setImageResource(R.drawable.ic_baseline_stop_48)
                tvStartStop.text = resources.getString(R.string.action_stop_activity)
            } else {
                ivStartStop.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                tvStartStop.text = resources.getString(R.string.action_start_activity)
            }
        })

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

    override fun onBikeActivityItemClicked(bikeActivityWithSamples: BikeActivityWithSamples) {
        val intent = Intent(
            requireActivity().applicationContext,
            BikeActivityDetailsActivity::class.java
        ).apply {
            putExtra(
                BikeActivityDetailsActivity.EXTRA_BIKE_ACTIVITY_UID,
                bikeActivityWithSamples.bikeActivity.uid.toString()
            )
            putExtra(
                BikeActivityDetailsActivity.EXTRA_TRACKING_SERVICE_ENABLED,
                bikeActivityWithSamples.bikeActivity.uid.toString()
            )
        }

        @Suppress("DEPRECATION")
        requireActivity().startActivityForResult(intent, MainActivity.REQUEST_BIKE_ACTIVITY_DETAILS)
    }

    //
    // Helpers
    //

    /**
     * Updates menu items based on tracking service state
     */
    private fun updateMenuItems(toolbar: Toolbar, trackingServiceStatus: String) {
        toolbar.menu.removeItem(ACTION_ENABLED_AUTOMATIC_TRACKING)
        toolbar.menu.removeItem(ACTION_DISABLED_AUTOMATIC_TRACKING)

        when (trackingServiceStatus) {
            TrackingForegroundService.STATUS_STARTED -> {
                toolbar.menu.add(
                    Menu.NONE,
                    ACTION_DISABLED_AUTOMATIC_TRACKING,
                    Menu.NONE,
                    getString(R.string.action_disable_automatic_tracking)
                )
            }
            TrackingForegroundService.STATUS_STOPPED -> {
                toolbar.menu.add(
                    Menu.NONE,
                    ACTION_ENABLED_AUTOMATIC_TRACKING,
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
     * Enables manual tracking
     */
    private fun enableManualTracking() {
        val trackingForegroundServiceIntent =
            Intent(requireActivity(), TrackingForegroundService::class.java)
        trackingForegroundServiceIntent.action = TrackingForegroundService.ACTION_START_MANUALLY
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

    /**
     * Logs message
     */
    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }

    companion object {
        const val ACTION_ENABLED_AUTOMATIC_TRACKING = 0
        const val ACTION_DISABLED_AUTOMATIC_TRACKING = 1
    }
}