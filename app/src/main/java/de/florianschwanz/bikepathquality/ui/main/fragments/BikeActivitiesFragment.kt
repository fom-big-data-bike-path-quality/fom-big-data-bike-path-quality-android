package de.florianschwanz.bikepathquality.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.*
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModel
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModelFactory
import de.florianschwanz.bikepathquality.services.TrackingForegroundService
import de.florianschwanz.bikepathquality.ui.details.BikeActivityDetailsActivity
import de.florianschwanz.bikepathquality.ui.head_up.HeadUpActivity
import de.florianschwanz.bikepathquality.ui.main.MainActivity
import de.florianschwanz.bikepathquality.ui.main.MainActivityViewModel
import de.florianschwanz.bikepathquality.ui.main.adapters.BikeActivityListAdapter
import de.florianschwanz.bikepathquality.ui.settings.SettingsActivity
import java.time.Instant

class BikeActivitiesFragment : Fragment(), BikeActivityListAdapter.OnItemClickListener {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var toolbar: Toolbar

    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((requireActivity().application as BikePathQualityApplication).bikeActivityRepository)
    }
    private val userDataViewModel: UserDataViewModel by viewModels {
        UserDataViewModelFactory((requireActivity().application as BikePathQualityApplication).userDataRepository)
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

        val adapter = BikeActivityListAdapter(requireActivity(), this)

        toolbar = view.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_activities_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clear -> {
                    bikeActivityViewModel.deleteAll()
                }
                R.id.action_settings -> {
                    val intent = Intent(
                        requireActivity().applicationContext,
                        SettingsActivity::class.java
                    )

                    @Suppress("DEPRECATION")
                    requireActivity().startActivity(intent)
                }
                R.id.action_hud -> {
                    val intent = Intent(
                        requireActivity().applicationContext,
                        HeadUpActivity::class.java
                    )

                    @Suppress("DEPRECATION")
                    requireActivity().startActivity(intent)
                }
            }

            false
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        clStartStop.setOnClickListener {

            val bikeActivity = viewModel.activeBikeActivity.value
            val userData = viewModel.userData.value

            if (userData != null) {
                if (bikeActivity != null) {
                    disableAutomaticTracking()
                    bikeActivityViewModel.update(bikeActivity.copy(endTime = Instant.now()))
                } else {
                    enableManualTracking()
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().applicationContext)

                    bikeActivityViewModel.insert(
                        BikeActivity(
                            trackingType = BikeActivityTrackingType.AUTOMATIC,
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

        userDataViewModel.singleUserData().observe(viewLifecycleOwner, { userData ->
            viewModel.userData.value = userData
        })

        return view
    }

    /**
     * Handles click on bike activity item
     */
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
}