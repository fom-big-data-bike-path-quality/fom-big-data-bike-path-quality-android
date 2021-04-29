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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.services.TrackingForegroundService
import de.florianschwanz.bikepathquality.ui.main.MainActivityViewModel
import de.florianschwanz.bikepathquality.ui.main.adapters.LogEntryListAdapter

class LogFragment : Fragment() {

    private lateinit var viewModel: MainActivityViewModel

    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((requireActivity().application as BikePathQualityApplication).logEntryRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.log_fragment, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvLog)
        val adapter = LogEntryListAdapter()

        toolbar.inflateMenu(R.menu.menu_log_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clear -> {
                    logEntryViewModel.deleteAll()
                }
                ACTION_ENABLED_AUTOMATIC_TRACKING -> {
                    val trackingForegroundServiceIntent =
                        Intent(requireActivity(), TrackingForegroundService::class.java)
                    trackingForegroundServiceIntent.setAction(TrackingForegroundService.ACTION_START)
                    ContextCompat.startForegroundService(
                        requireActivity(),
                        trackingForegroundServiceIntent
                    )
                }
                ACTION_DISABLED_AUTOMATIC_TRACKING -> {
                    val trackingForegroundServiceIntent =
                        Intent(requireActivity(), TrackingForegroundService::class.java)
                    trackingForegroundServiceIntent.setAction(TrackingForegroundService.ACTION_STOP)
                    ContextCompat.startForegroundService(
                        requireActivity(),
                        trackingForegroundServiceIntent
                    )
                }
                else -> {
                }
            }

            false
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        logEntryViewModel.allLogEntries.observe(viewLifecycleOwner, {
            adapter.data = it
            if (adapter.data.isNotEmpty()) {
                recyclerView.smoothScrollToPosition(adapter.data.size - 1)
            }
        })

        viewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.trackingServiceEnabled.observe(viewLifecycleOwner, { trackingServiceEnabled ->
            if (trackingServiceEnabled) {
                toolbar.menu.removeItem(BikeActivitiesFragment.ACTION_ENABLED_AUTOMATIC_TRACKING)
                toolbar.menu.add(
                    Menu.NONE,
                    BikeActivitiesFragment.ACTION_DISABLED_AUTOMATIC_TRACKING,
                    Menu.NONE,
                    getString(R.string.action_disable_automatic_tracking)
                )
            } else {
                toolbar.menu.removeItem(BikeActivitiesFragment.ACTION_DISABLED_AUTOMATIC_TRACKING)
                toolbar.menu.add(
                    Menu.NONE,
                    BikeActivitiesFragment.ACTION_ENABLED_AUTOMATIC_TRACKING,
                    Menu.NONE,
                    getString(R.string.action_enable_automatic_tracking)
                )
            }
        })

        return view
    }

    companion object {
        const val ACTION_ENABLED_AUTOMATIC_TRACKING = 0
        const val ACTION_DISABLED_AUTOMATIC_TRACKING = 1
    }
}