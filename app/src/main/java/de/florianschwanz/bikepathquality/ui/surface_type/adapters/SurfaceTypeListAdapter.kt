package de.florianschwanz.bikepathquality.ui.surface_type.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.SurfaceType
import de.florianschwanz.bikepathquality.ui.details.adapters.BikeActivitySampleListAdapter

class SurfaceTypeListAdapter(
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<SurfaceTypeListAdapter.SurfaceTypeViewHolder>() {

    var data = listOf<SurfaceType>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurfaceTypeViewHolder {
        return SurfaceTypeViewHolder.create(context, parent)
    }

    override fun onBindViewHolder(holder: SurfaceTypeViewHolder, position: Int) {
        val current = data[position]
        holder.bind(current, context, itemClickListener)
    }

    class SurfaceTypeViewHolder(val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val cvSurfaceType: CardView = itemView.findViewById(R.id.cvSurfaceType)
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)

        fun bind(item: SurfaceType,
                 context: Context,
                 itemClickListener: OnItemClickListener
        ) {

            item.photo?.let {
                ivPhoto.setImageDrawable(it)
            }

            tvValue.text = item.value
                .replace("_", " ")
                .replace(":", " ")
            tvComment.text = item.comment

            cvSurfaceType.setOnClickListener {
                itemClickListener.onSurfaceTypeClicked(tvValue.text as String)
            }
        }

        companion object {
            const val RESULT_SURFACE_TYPE = "result.SURFACE_TYPE"

            fun create(context: Context, parent: ViewGroup): SurfaceTypeViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.surface_type_item, parent, false)
                return SurfaceTypeViewHolder(context, view)
            }
        }
    }

    interface OnItemClickListener {
        fun onSurfaceTypeClicked(surfaceType: String)
    }
}