package de.florianschwanz.bikepathquality.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.ui.head_up.HeadUpActivity
import de.florianschwanz.bikepathquality.ui.settings.SettingsActivity

class SensorsFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.sensors_fragment, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)

        toolbar.inflateMenu(R.menu.menu_sensors_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    val intent = Intent(
                        requireActivity().applicationContext,
                        SettingsActivity::class.java
                    )

                    @Suppress("DEPRECATION")
                    requireActivity().startActivity(intent)
                }
                else -> {
                }
            }

            false
        }

        return view
    }
}