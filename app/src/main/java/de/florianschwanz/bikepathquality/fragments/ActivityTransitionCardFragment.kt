package de.florianschwanz.bikepathquality.fragments

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import de.florianschwanz.bikepathquality.R

/**
 * Activity transition card fragment
 */
class ActivityTransitionCardFragment : Fragment() {

    companion object {
        fun newInstance() = ActivityTransitionCardFragment()
    }

    private lateinit var viewModel: ActivityTransitionViewModel

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

        val ivStill: ImageView = view.findViewById(R.id.ivStill)
        val ivWalking: ImageView = view.findViewById(R.id.ivWalking)
        val ivRunning: ImageView = view.findViewById(R.id.ivRunning)
        val ivOnBicycle: ImageView = view.findViewById(R.id.ivOnBicycle)
        val ivInVehicle: ImageView = view.findViewById(R.id.ivInVehicle)
        val ivUnknown: ImageView = view.findViewById(R.id.ivUnknown)

        viewModel =
            ViewModelProvider(requireActivity()).get(ActivityTransitionViewModel::class.java)
        viewModel.data.observe(viewLifecycleOwner, {

            val color = when (it.transitionType) {
//                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> getColorAttribute(R.attr.colorSecondary)
//                else -> getColorAttribute(R.attr.colorOnSurface)
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> R.color.teal_700
                else -> R.color.grey_500
            }

            when (it.activityType) {
                DetectedActivity.STILL -> ivStill.tint(color)
                DetectedActivity.WALKING -> ivWalking.tint(color)
                DetectedActivity.RUNNING -> ivRunning.tint(color)
                DetectedActivity.ON_BICYCLE -> ivOnBicycle.tint(color)
                DetectedActivity.IN_VEHICLE -> ivInVehicle.tint(color)
                DetectedActivity.UNKNOWN -> ivUnknown.tint(color)
            }
        })

        return view
    }

    //
    // Helpers
    //

    /**
     * Tints a image view in a given color
     *
     * @param color color resource
     */
    fun ImageView.tint(color: Int) = this.setColorFilter(
        ContextCompat.getColor(
            requireContext(),
            color
        ), android.graphics.PorterDuff.Mode.MULTIPLY
    );

    /**
     * Retrieves color attribute
     */
    fun getColorAttribute(attribute: Int, context: Context = requireContext()): Int {
        val typedValue = TypedValue()
        val typedArray = context.getTheme().obtainStyledAttributes(typedValue.data, intArrayOf(attribute))
        val color: Int = typedArray.getColor(0, 0)

        typedArray.recycle()

        return color
    }
}