package de.florianschwanz.bikepathquality.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.common.collect.EvictingQueue
import de.florianschwanz.bikepathquality.ui.main.MainActivityViewModel
import de.florianschwanz.bikepathquality.R
import java.lang.Math.abs

/**
 * Accelerometer card fragment
 */
class AccelerometerCardFragment : Fragment() {

    private lateinit var viewModel: MainActivityViewModel

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

        val tvMinusX: TextView = view.findViewById(R.id.tvMinusX)
        val tvMinusY: TextView = view.findViewById(R.id.tvMinusY)
        val tvMinusZ: TextView = view.findViewById(R.id.tvMinusZ)
        val tvAccelerometerX: TextView = view.findViewById(R.id.tvAccelerometerX)
        val tvAccelerometerY: TextView = view.findViewById(R.id.tvAccelerometerY)
        val tvAccelerometerZ: TextView = view.findViewById(R.id.tvAccelerometerZ)

        val accelerometerEvictingQueueX: EvictingQueue<Float> = EvictingQueue.create(100)
        val accelerometerEvictingQueueY: EvictingQueue<Float> = EvictingQueue.create(100)
        val accelerometerEvictingQueueZ: EvictingQueue<Float> = EvictingQueue.create(100)

        viewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        viewModel.accelerometerLiveData.observe(viewLifecycleOwner, {

            accelerometerEvictingQueueX.add(it.x)
            accelerometerEvictingQueueY.add(it.y)
            accelerometerEvictingQueueZ.add(it.z)

            val x = accelerometerEvictingQueueX.average()
            val y = accelerometerEvictingQueueY.average()
            val z = accelerometerEvictingQueueZ.average()

            tvMinusX.text = x.sign()
            tvMinusY.text = y.sign()
            tvMinusZ.text = z.sign()
            tvAccelerometerX.text = resources.getString(R.string.accelerometer_x, abs(x))
            tvAccelerometerY.text = resources.getString(R.string.accelerometer_y, abs(y))
            tvAccelerometerZ.text = resources.getString(R.string.accelerometer_z, abs(z))
        })

        return view
    }

    private fun Double.sign(): String = if (this < 0) "-" else ""
}