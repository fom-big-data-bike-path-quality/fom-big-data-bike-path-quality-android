package de.florianschwanz.bikepathquality.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.activities.ActivityTransitionPermissionRationalActivity
import de.florianschwanz.bikepathquality.storage.LogEntryViewModel
import de.florianschwanz.bikepathquality.storage.LogEntryViewModelFactory

/**
 * Activity transition card fragment
 */
class ActivityTransitionCardFragment : Fragment() {

    private lateinit var viewModel: ActivityTransitionViewModel
    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((requireActivity().application as BikePathQualityApplication).logEntryRepository)
    }

    private lateinit var ivStill: ImageView
    private lateinit var ivWalking: ImageView
    private lateinit var ivRunning: ImageView
    private lateinit var ivOnBicycle: ImageView
    private lateinit var ivInVehicle: ImageView
    private lateinit var ivUnknown: ImageView

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
        val view = inflater.inflate(R.layout.activity_transition_fragment, container, false)

        ivStill = view.findViewById(R.id.ivStill)
        ivWalking = view.findViewById(R.id.ivWalking)
        ivRunning = view.findViewById(R.id.ivRunning)
        ivOnBicycle = view.findViewById(R.id.ivOnBicycle)
        ivInVehicle = view.findViewById(R.id.ivInVehicle)
        ivUnknown = view.findViewById(R.id.ivUnknown)

        return view
    }

    /**
     * Handles on-start lifecycle phase
     */
    override fun onStart() {
        super.onStart()
        invokeActivityTransitionAction()
    }

    //
    // Helpers
    //

    /**
     * Invokes activity transaction action
     */
    private fun invokeActivityTransitionAction() {
        when {
            isPermissionsGranted() -> {
                viewModel =
                    ViewModelProvider(requireActivity()).get(ActivityTransitionViewModel::class.java)
                viewModel.data.observe(viewLifecycleOwner, {

                    val activeColor = R.color.teal_700
                    val inactiveColor = R.color.grey_500

                    val color = when (it.transitionType) {
//                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> getColorAttribute(R.attr.colorSecondary)
//                else -> getColorAttribute(R.attr.colorOnSurface)
                        ActivityTransition.ACTIVITY_TRANSITION_ENTER -> activeColor
                        else -> inactiveColor
                    }

                    ivStill.tint(inactiveColor)
                    ivWalking.tint(inactiveColor)
                    ivRunning.tint(inactiveColor)
                    ivOnBicycle.tint(inactiveColor)
                    ivInVehicle.tint(inactiveColor)
                    ivUnknown.tint(inactiveColor)

                    when (it.activityType) {
                        DetectedActivity.STILL -> ivStill.tint(color)
                        DetectedActivity.WALKING -> ivWalking.tint(color)
                        DetectedActivity.RUNNING -> ivRunning.tint(color)
                        DetectedActivity.ON_BICYCLE -> ivOnBicycle.tint(color)
                        DetectedActivity.IN_VEHICLE -> ivInVehicle.tint(color)
                        DetectedActivity.UNKNOWN -> ivUnknown.tint(color)
                    }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isGranted(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            true
        }

    /**
     * Determines if a given permission is granted
     */
    private fun isGranted(permission: String) =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Requests permission for activity recognition
     */
    private fun requestPermission() {

        // Enable/Disable activity tracking and ask for permissions if needed
        if (!isPermissionsGranted()) {
            // Request permission and start activity for result
            val startIntent =
                Intent(requireActivity(), ActivityTransitionPermissionRationalActivity::class.java)

            @Suppress("DEPRECATION")
            startActivityForResult(startIntent, 0)
        }
    }

    /**
     * Tints a image view in a given color
     *
     * @param color color resource
     */
    private fun ImageView.tint(color: Int) = this.setColorFilter(
        ContextCompat.getColor(
            requireContext(),
            color
        ), android.graphics.PorterDuff.Mode.MULTIPLY
    )

//    /**
//     * Retrieves color attribute
//     */
//    private fun getColorAttribute(attribute: Int, context: Context = requireContext()): Int {
//        val typedValue = TypedValue()
//        val typedArray = context.theme.obtainStyledAttributes(typedValue.data, intArrayOf(attribute))
//        val color: Int = typedArray.getColor(0, 0)
//
//        typedArray.recycle()
//
//        return color
//    }
}