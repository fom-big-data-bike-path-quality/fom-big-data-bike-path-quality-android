package de.florianschwanz.bikepathquality.ui.main.fragments.cards

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.ui.main.MainActivityViewModel

/**
 * Activity transition card fragment
 */
class ActivityTransitionCardFragment : Fragment() {

    private lateinit var viewModel: MainActivityViewModel

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
        val view = inflater.inflate(R.layout.activity_transition_card_fragment, container, false)

        ivStill = view.findViewById(R.id.ivStill)
        ivWalking = view.findViewById(R.id.ivWalking)
        ivRunning = view.findViewById(R.id.ivRunning)
        ivOnBicycle = view.findViewById(R.id.ivOnBicycle)
        ivInVehicle = view.findViewById(R.id.ivInVehicle)
        ivUnknown = view.findViewById(R.id.ivUnknown)

        viewModel =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.activityTransitionLiveData.observe(viewLifecycleOwner, {

            val activeColor =
                context?.getColorFromAttr(R.attr.colorPrimary) ?: R.color.green_700
            val inactiveColor =
                context?.getColorFromAttr(R.attr.colorOnSurface) ?: R.color.grey_500

            val color = when (it.transitionType) {
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
    private fun ImageView.tint(color: Int) = this.setColorFilter(
        ContextCompat.getColor(
            requireContext(),
            color
        ), android.graphics.PorterDuff.Mode.MULTIPLY
    )

    @ColorInt
    fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = false
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}