package de.florianschwanz.bikepathquality.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.florianschwanz.bikepathquality.LocationPermissionRationalActivity
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.utils.GpsUtils

/**
 * Location card fragment
 */
class LocationCardFragment : Fragment() {

    private lateinit var viewModel: LocationCardViewModel

    private lateinit var tvLon: TextView
    private lateinit var tvLat: TextView

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

        tvLon = view.findViewById(R.id.tvLon)
        tvLat = view.findViewById(R.id.tvLat)

        return view
    }

    /**
     * Handles activity-result lifecycle phase
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                isGPSEnabled = true
                invokeLocationAction()
            }
        }
    }

    /**
     * Handles attach lifecycle phase
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        GpsUtils(context).turnGPSOn(object : GpsUtils.OnGpsListener {
            override fun gpsStatus(isGPSEnabled: Boolean) {
                this@LocationCardFragment.isGPSEnabled = isGPSEnabled
            }
        })
    }

    /**
     * Handles start lifecycle phase
     */
    override fun onStart() {
        super.onStart()
        invokeLocationAction()
    }

    //
    // Helpers
    //

    /**
     * Invokes location action
     */
    private fun invokeLocationAction() {
        when {
            isPermissionsGranted() -> {
                viewModel =
                    ViewModelProvider(requireActivity()).get(LocationCardViewModel::class.java)
                viewModel.data.observe(viewLifecycleOwner, {
                    tvLon.text = resources.getString(R.string.lon, it.lon)
                    tvLat.text = resources.getString(R.string.lat, it.lat)
                })
            }

            else -> activity?.let {
                requestPermission()
            }
        }
    }

    /**
     * Checks if permissions are granted
     */
    private fun isPermissionsGranted() =
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    /**
     * Determines if a given permission is granted
     */
    private fun isGranted(permission: String) =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Requests permission for location
     */
    private fun requestPermission() {

        // Enable/Disable location tracking and ask for permissions if needed
        if (!isPermissionsGranted()) {
            // Request permission and start activity for result
            val startIntent =
                Intent(requireActivity(), LocationPermissionRationalActivity::class.java)

            @Suppress("DEPRECATION")
            startActivityForResult(startIntent, 0)
        }
    }

    companion object {

        /** ID to identify location requests */
        val LOCATION_REQUEST = 100

        /** ID to identify GPS requests */
        val GPS_REQUEST = 101
    }
}