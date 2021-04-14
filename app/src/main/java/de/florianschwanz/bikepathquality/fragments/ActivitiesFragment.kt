package de.florianschwanz.bikepathquality.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.ui.BikeActivityListAdapter

class ActivitiesFragment : Fragment() {

    private val viewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((requireActivity().application as BikePathQualityApplication).bikeActivitiesRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.activities_fragment, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvActivities)
        val adapter = BikeActivityListAdapter()

        toolbar.inflateMenu(R.menu.menu_activities_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clear -> {
                    viewModel.deleteAll()
                }
                else -> {
                }
            }

            false
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allBikeActivitiesWithDetails.observe(viewLifecycleOwner, {
            adapter.data = it
            recyclerView.smoothScrollToPosition(adapter.data.size - 1)
        })

        return view
    }
}