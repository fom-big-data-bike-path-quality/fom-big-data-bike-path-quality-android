package de.florianschwanz.bikepathquality.ui.smoothness_type.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.SmoothnessType

class SmoothnessTypeListAdapter(val activity: Activity) :
    RecyclerView.Adapter<SmoothnessTypeListAdapter.SmoothnessTypeViewHolder>() {

    var data = listOf<SmoothnessType>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmoothnessTypeViewHolder {
        return SmoothnessTypeViewHolder.create(activity, parent)
    }

    override fun onBindViewHolder(holder: SmoothnessTypeViewHolder, position: Int) {
        val current = data[position]
        holder.bind(current)
    }

    class SmoothnessTypeViewHolder(val activity: Activity, itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val cvSmoothnessType: CardView = itemView.findViewById(R.id.cvSmoothnessType)
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)

        fun bind(item: SmoothnessType) {

            item.photo?.let {
                ivPhoto.setImageDrawable(it)
            }

            tvValue.text = item.value
                .replace("_", " ")
                .replace(":", " ")
            tvComment.text = item.comment

            cvSmoothnessType.setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putExtra(
                    RESULT_SMOOTHNESS_TYPE,
                    tvValue.text
                )
                activity.setResult(Activity.RESULT_OK, resultIntent)
                activity.finish()
            }
        }

        companion object {
            const val RESULT_SMOOTHNESS_TYPE = "result.SMOOTHNESS_TYPE"

            fun create(activity: Activity, parent: ViewGroup): SmoothnessTypeViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.smoothness_type_item, parent, false)
                return SmoothnessTypeViewHolder(activity, view)
            }
        }
    }
}