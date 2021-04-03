package de.florianschwanz.bikepathquality.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.florianschwanz.bikepathquality.R

/**
 * Accelerometer card fragment
 */
class AccelerometerCardFragment : Fragment() {

    private lateinit var viewModel: AccelerometerCardViewModel

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
        val view = inflater.inflate(R.layout.accelerometer_card_fragment, container, false)

        val tvAccelerometerX: TextView = view.findViewById(R.id.tvAccelerometerX)
        val tvAccelerometerY: TextView = view.findViewById(R.id.tvAccelerometerY)
        val tvAccelerometerZ: TextView = view.findViewById(R.id.tvAccelerometerZ)

        viewModel = ViewModelProvider(requireActivity()).get(AccelerometerCardViewModel::class.java)
        viewModel.data.observe(viewLifecycleOwner, {
            tvAccelerometerX.text = resources.getString(R.string.accelerometerX, it.x)
            tvAccelerometerY.text = resources.getString(R.string.accelerometerY, it.y)
            tvAccelerometerZ.text = resources.getString(R.string.accelerometerZ, it.z)
        })

        return view
    }
}