package de.florianschwanz.bikepathquality.ui.smoothness_type.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.tracking.SmoothnessType

class SmoothnessTypeListAdapter(
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<SmoothnessTypeListAdapter.SmoothnessTypeViewHolder>() {

    var data = listOf<SmoothnessType>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmoothnessTypeViewHolder {
        return SmoothnessTypeViewHolder.create(context, parent)
    }

    override fun onBindViewHolder(holder: SmoothnessTypeViewHolder, position: Int) {
        val current = data[position]
        holder.bind(current, context, itemClickListener)
    }

    class SmoothnessTypeViewHolder(val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val cvSmoothnessType: CardView = itemView.findViewById(R.id.cvSmoothnessType)
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)

        fun bind(item: SmoothnessType,
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

            cvSmoothnessType.setOnClickListener {
                itemClickListener.onSmoothnessTypeClicked(tvValue.text as String)
            }
        }

        companion object {
            const val RESULT_SMOOTHNESS_TYPE = "result.SMOOTHNESS_TYPE"

            fun create(context: Context, parent: ViewGroup): SmoothnessTypeViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.smoothness_type_item, parent, false)
                return SmoothnessTypeViewHolder(context, view)
            }
        }
    }

    interface OnItemClickListener {
        fun onSmoothnessTypeClicked(smoothnessType: String)
    }
}