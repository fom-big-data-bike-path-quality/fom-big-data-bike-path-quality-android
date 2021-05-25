package de.florianschwanz.bikepathquality.ui.surface_type

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.SurfaceType
import de.florianschwanz.bikepathquality.ui.surface_type.adapters.SurfaceTypeListAdapter

class SurfaceTypeActivity : AppCompatActivity() {

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_surface_type)
        setTitle(R.string.empty)
        setSupportActionBar(findViewById(R.id.toolbar))

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val recyclerView = findViewById<RecyclerView>(R.id.rvSurfaceTypes)
        val adapter = SurfaceTypeListAdapter(this)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_reset -> {
                    val resultIntent = Intent()
                    val empty: String? = null
                    resultIntent.putExtra(
                        SurfaceTypeListAdapter.SurfaceTypeViewHolder.RESULT_SURFACE_TYPE,
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

        val surfaceType = intent.getStringExtra(EXTRA_SURFACE_TYPE)

        val surfaceTypeValues = resources.getStringArray(R.array.surface_type_values)
        val surfaceTypeComments = resources.getStringArray(R.array.surface_type_comments)
        val surfaceTypePhotoResources =
            resources.obtainTypedArray(R.array.surface_type_photo_resources)

        adapter.data = surfaceTypeValues.zip(surfaceTypeComments).mapIndexed { index, pair ->
            SurfaceType(
                value = pair.first,
                comment = pair.second,
                surfaceTypePhotoResources.getDrawable(index)
            )
        }

        surfaceTypePhotoResources.recycle()

        surfaceType?.let {
            recyclerView.scrollToPosition(surfaceTypeValues.indexOf(it))
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
        const val EXTRA_SURFACE_TYPE = "extra.SURFACE_TYPE"
    }
}
