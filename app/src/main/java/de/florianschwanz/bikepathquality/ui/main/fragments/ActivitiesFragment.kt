package de.florianschwanz.bikepathquality.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.ui.main.adapters.BikeActivityListAdapter
import java.time.Instant

class ActivitiesFragment : Fragment() {

    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((requireActivity().application as BikePathQualityApplication).logEntryRepository)
    }
    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((requireActivity().application as BikePathQualityApplication).bikeActivitiesRepository)
    }

    // Currently performed biking activity
    private var activeActivity: BikeActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.activities_fragment, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvActivities)
        val clStartStop = view.findViewById<ConstraintLayout>(R.id.clStartStop)
        val ivStartStop = view.findViewById<ImageView>(R.id.ivStartStop)
        val tvStartStop = view.findViewById<TextView>(R.id.tvStartStop)

        val adapter = BikeActivityListAdapter()

        toolbar.inflateMenu(R.menu.menu_activities_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clear -> {
                    bikeActivityViewModel.deleteAll()
                }
                else -> {
                }
            }

            false
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        clStartStop.setOnClickListener {
            if (activeActivity != null) {
                log("Stop manually")
                bikeActivityViewModel.update(activeActivity!!.copy(endTime = Instant.now()))
            } else {
                log("Start manually")
                bikeActivityViewModel.insert(BikeActivity())
            }
        }

        bikeActivityViewModel.allBikeActivitiesWithDetails.observe(viewLifecycleOwner, {
            adapter.data = it
            if (adapter.data.size > 0) {
                recyclerView.smoothScrollToPosition(adapter.data.size - 1)
            }
        })

        bikeActivityViewModel.activeBikeActivity.observe(viewLifecycleOwner, {
            activeActivity = it

            if (activeActivity != null) {
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
     * Logs message
     */
    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }
}