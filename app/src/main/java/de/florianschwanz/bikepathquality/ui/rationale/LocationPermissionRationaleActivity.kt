package de.florianschwanz.bikepathquality.ui.rationale

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import de.florianschwanz.bikepathquality.R

/**
 * Displays rationale for allowing the location permission and allows user to accept
 * the permission
 */
class LocationPermissionRationaleActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {

    private lateinit var btnApprove: AppCompatButton
    private lateinit var btnDeny: AppCompatButton

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isGranted(Manifest.permission.ACCESS_FINE_LOCATION) && isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            setResult(Activity.RESULT_OK)
            finish()
        }
        setContentView(R.layout.activity_location_permission_rationale)

        btnApprove = findViewById(R.id.approve_permission_request)
        btnDeny = findViewById(R.id.deny_permission_request)

        btnApprove.setOnClickListener {
            setResult(Activity.RESULT_OK)
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_LOCATION
            )
        }
        btnDeny.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
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

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            setResult(Activity.RESULT_OK)
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
        setResult(Activity.RESULT_OK)
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
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    companion object {
        private const val TAG = "LocationPermissionRationalActivity"
        private const val PERMISSION_REQUEST_LOCATION = 46
    }
}