package de.florianschwanz.bikepathquality

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback

/**
 * Displays rationale for allowing the location permission and allows user to accept
 * the permission
 */
class LocationPermissionRationalActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If permissions granted, we start the main activity (shut this activity down)
        if (isGranted(Manifest.permission.ACCESS_FINE_LOCATION) && isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            finish()
        }
        setContentView(R.layout.location_permission_rational)
    }

    /**
     * Determines if a given permission is granted
     */
    private fun isGranted(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Callback received when a permissions request has been completed
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionResult =
            "Request code: ${requestCode}, Permissions: ${permissions.contentToString()}, Results: ${grantResults.contentToString()}"
        Log.d(TAG, "onRequestPermissionsResult(): $permissionResult")
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Close activity regardless of user's decision (decision picked up in main activity).
            finish()
        }
    }

    //
    // Actions
    //

    /**
     * Handles click on approval button
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun onClickApprovePermissionRequest(view: View?) {
        Log.d(TAG, "onClickApprovePermissionRequest()")
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_LOCATION
        )
    }

    /**
     * Handles click on denial button
     */
    fun onClickDenyPermissionRequest(view: View?) {
        Log.d(TAG, "onClickDenyPermissionRequest()")
        finish()
    }

    companion object {
        private const val TAG = "LocationPermissionRationalActivity"

        /* Id to identify Activity Recognition permission request */
        private const val PERMISSION_REQUEST_LOCATION = 46
    }
}