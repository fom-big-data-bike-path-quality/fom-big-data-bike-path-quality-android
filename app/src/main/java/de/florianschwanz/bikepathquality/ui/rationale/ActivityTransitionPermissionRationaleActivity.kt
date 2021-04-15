package de.florianschwanz.bikepathquality.ui.rationale

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
import de.florianschwanz.bikepathquality.R

/**
 * Displays rationale for allowing the activity recognition permission and allows user to accept
 * the permission
 */
class ActivityTransitionPermissionRationaleActivity : AppCompatActivity(),
    OnRequestPermissionsResultCallback {

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If permissions granted, we start the main activity (shut this activity down)
        if (isGranted(Manifest.permission.ACTIVITY_RECOGNITION)) {
            finish()
        }
        setContentView(R.layout.activity_transition_permission_rational)
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
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
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
            this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION
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
        private const val TAG = "PermissionRationalActivity"
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45
    }
}