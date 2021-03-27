package de.florianschwanz.bikepathquality.logger

import android.R
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment

/**
 * LogView outputs log data to the screen.
 */
class LogFragment : Fragment() {

    var logView: LogView? = null
        private set

    private var scrollView: ScrollView? = null

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val result = inflateViews()
        logView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                scrollView!!.fullScroll(ScrollView.FOCUS_DOWN)
            }
        })
        return result
    }

    //
    // Helpers
    //

    /**
     * Inflates views
     */
    fun inflateViews(): View {
        scrollView = ScrollView(activity)

        val scrollParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        scrollView!!.layoutParams = scrollParams
        logView = LogView(activity)

        val logParams = ViewGroup.LayoutParams(scrollParams)
        logParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        logView!!.layoutParams = logParams
        logView!!.isClickable = true
        logView!!.isFocusable = true
        logView!!.setTypeface(Typeface.MONOSPACE)

        // Want to set padding as 16 dips, setPadding takes pixels.  Hooray math!
        val paddingDips = 16
        val scale = resources.displayMetrics.density.toDouble()
        val paddingPixels = (paddingDips * scale + .5).toInt()
        logView!!.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels)
        logView!!.compoundDrawablePadding = paddingPixels
        logView!!.gravity = Gravity.BOTTOM
        logView!!.setTextAppearance(R.style.TextAppearance_DeviceDefault_Medium)
        scrollView!!.addView(logView)
        return scrollView as ScrollView
    }
}