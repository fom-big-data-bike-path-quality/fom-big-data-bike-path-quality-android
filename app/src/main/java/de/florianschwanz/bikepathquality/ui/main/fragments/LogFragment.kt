package de.florianschwanz.bikepathquality.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.ui.main.adapters.LogEntryListAdapter

class LogFragment : Fragment() {

    private val viewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((requireActivity().application as BikePathQualityApplication).logEntryRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.log_fragment, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvLog)
        val adapter = LogEntryListAdapter()

        toolbar.inflateMenu(R.menu.menu_log_fragment)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clear -> {
                    viewModel.deleteAll()
                }
                else -> {
                }
            }

            false
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allLogEntries.observe(viewLifecycleOwner, {
            adapter.data = it
            if (adapter.data.isNotEmpty()) {
                recyclerView.smoothScrollToPosition(adapter.data.size - 1)
            }
        })

        return view
    }
}