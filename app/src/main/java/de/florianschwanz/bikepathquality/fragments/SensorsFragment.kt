package de.florianschwanz.bikepathquality.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.florianschwanz.bikepathquality.R

class SensorsFragment : Fragment() {

    private lateinit var viewModel: SensorsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sensors_fragment, container, false)
    }
}