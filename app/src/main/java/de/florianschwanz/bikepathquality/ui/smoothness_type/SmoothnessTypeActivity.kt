package de.florianschwanz.bikepathquality.ui.smoothness_type

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.SmoothnessType
import de.florianschwanz.bikepathquality.ui.smoothness_type.adapters.SmoothnessTypeListAdapter
import de.florianschwanz.bikepathquality.ui.surface_type.adapters.SurfaceTypeListAdapter

class SmoothnessTypeActivity  : AppCompatActivity() {

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_smoothness_type)
        setTitle(R.string.empty)
        setSupportActionBar(findViewById(R.id.toolbar))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val recyclerView = findViewById<RecyclerView>(R.id.rvSmoothnessTypes)
        val adapter = SmoothnessTypeListAdapter(this)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_reset -> {
                    val resultIntent = Intent()
                    val empty: String? = null
                    resultIntent.putExtra(
                        SmoothnessTypeListAdapter.SmoothnessTypeViewHolder.RESULT_SMOOTHNESS_TYPE,
                        empty
                    )
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                else -> {
                }
            }

            false
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val smoothnessType = intent.getStringExtra(EXTRA_SMOOTHNESS_TYPE)

        val smoothnessTypeValues = resources.getStringArray(R.array.smoothness_type_values)
        val smoothnessTypeComments = resources.getStringArray(R.array.smoothness_type_comments)
        val smoothnessTypePhotoResources =
            resources.obtainTypedArray(R.array.smoothness_type_photo_resources)

        adapter.data = smoothnessTypeValues.zip(smoothnessTypeComments).mapIndexed { index, pair ->
            SmoothnessType(
                value = pair.first,
                comment = pair.second,
                smoothnessTypePhotoResources.getDrawable(index)
            )
        }

        smoothnessTypePhotoResources.recycle()

        smoothnessType?.let {
            recyclerView.scrollToPosition(smoothnessTypeValues.indexOf(it))
        }
    }

    /**
     * Handles option menu creation
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_surface_type_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        const val EXTRA_SMOOTHNESS_TYPE = "extra.SMOOTHNESS_TYPE"
    }
}
