package de.florianschwanz.bikepathquality.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.storage.LogEntryViewModel
import de.florianschwanz.bikepathquality.storage.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.ui.LogEntryListAdapter


class LogFragment : Fragment() {

    private val viewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((requireActivity().application as BikePathQualityApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true);

        val view = inflater.inflate(R.layout.log_fragment, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = LogEntryListAdapter()

        toolbar.inflateMenu(R.menu.menu_log_fragment)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_clear -> {
                    println("foo")
                }
                else -> {}
            }

            false
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allLogEntries.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.getItemId()
        return if (id == R.id.action_clear) {
            true
        } else super.onOptionsItemSelected(item)
    }
}