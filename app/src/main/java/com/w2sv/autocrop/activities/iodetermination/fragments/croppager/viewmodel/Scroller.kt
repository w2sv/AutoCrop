package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2
import com.w2sv.autocrop.utils.UnitFun
import java.util.Timer
import java.util.TimerTask

/**
 * Class accounting for automatic scrolling
 */
class Scroller : Timer() {
    fun run(viewPager2: ViewPager2, maxScrolls: Int, onFinishedListener: UnitFun) {
        schedule(
            object : TimerTask() {
                private var conductedScrolls: Int = 0

                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        with(viewPager2) {
                            setCurrentItem(currentItem + 1, true)
                        }

                        conductedScrolls++
                        if (conductedScrolls == maxScrolls) {
                            onFinishedListener()
                            this@Scroller.cancel()

                            return@post
                        }
                    }
                }
            },
            1000L,
            1000L
        )
    }
}