package com.autocrop.activities.examination.fragments.viewpager

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.uielements.view.show
import java.util.*

/**
 * Class accounting for automatic scrolling at the start of crop examination
 */
class Scroller(private val onCancelListener: (onScreenTouch: Boolean) -> Unit) {
    private lateinit var timer: Timer

    fun run(viewPager2: ViewPager2, maxScrolls: Int, autoScrollingTv: TextView) {
        (viewPager2.context as Activity).runOnUiThread { autoScrollingTv.show() }

        timer = Timer().apply {
            schedule(
                object : TimerTask() {
                    private var conductedScrolls: Int = 0

                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            with(viewPager2) {
                                setCurrentItem(currentItem + 1, true)
                            }
                            conductedScrolls++

                            if (conductedScrolls == maxScrolls)
                                return@post cancel(false)
                        }
                    }
                },
                1000L,
                1000L
            )
        }
    }

    /**
     * • Cancel [timer] and call [onCancelListener]
     */
    fun cancel(onScreenTouch: Boolean) {
        timer.cancel()
        onCancelListener(onScreenTouch)
    }
}