package de.florianschwanz.bikepathquality.data.livedata

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import de.florianschwanz.bikepathquality.data.model.LocationModel

/**
 * Location live data
 */
class LocationLiveData(context: Context) : LiveData<LocationModel>() {

    /** Fused location client */
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    //
    // Lifecycle phases
    //

    /**
     * Handles inactivity
     */
    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Handles activity
     */
    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.also {
                    setLocationData(it)
                }
            }
        startLocationUpdates()
    }

    /**
     * Starts location updates
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    /** Location callback */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                setLocationData(location)
            }
        }
    }

    /**
     * Sets location data
     */
    private fun setLocationData(location: Location) {
        value = LocationModel(
            lon = location.longitude,
            lat = location.latitude
        )
    }

    /**
     * Companion object
     */
    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 1_000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}