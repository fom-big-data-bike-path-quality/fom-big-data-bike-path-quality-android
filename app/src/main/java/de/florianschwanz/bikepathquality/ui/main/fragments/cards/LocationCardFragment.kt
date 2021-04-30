package de.florianschwanz.bikepathquality.ui.main.fragments.cards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.ui.main.MainActivityViewModel
import de.florianschwanz.bikepathquality.utils.GpsUtils

/**
 * Location card fragment
 */
class LocationCardFragment : Fragment() {

    private lateinit var viewModel: MainActivityViewModel

    /** Whether GPS is enabled or not */
    private var isGPSEnabled = false

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
        val view = inflater.inflate(R.layout.location_card_fragment, container, false)

        val tvLon: TextView = view.findViewById(R.id.tvLon)
        val tvLat: TextView = view.findViewById(R.id.tvLat)

        viewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.locationLiveData.observe(viewLifecycleOwner, {
            tvLon.text = resources.getString(R.string.lon, it.lon)
            tvLat.text = resources.getString(R.string.lat, it.lat)
        })

        return view
    }

    /**
     * Handles attach lifecycle phase
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        GpsUtils(context).turnGPSOn(object : GpsUtils.OnGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                this@LocationCardFragment.isGPSEnabled = isGPSEnable
            }
        })
    }

    //
    // Helpers
    //

    companion object {
        const val GPS_REQUEST = 101
    }
}