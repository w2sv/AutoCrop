package com.autocrop.activities.examination.fragments.viewpager

import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.utils.BlankFun
import java.util.*

/**
 * Class accounting for automatic scrolling
 */
class AutoScroller: Timer() {
    fun run(viewPager2: ViewPager2, maxScrolls: Int, onFinishedListener: BlankFun) {
        schedule(
            object : TimerTask() {
                private var conductedScrolls: Int = 0

                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        with(viewPager2) {
                            setCurrentItem(currentItem + 1, true)
                        }

                        conductedScrolls++
                        if (conductedScrolls == maxScrolls){
                            onFinishedListener()
                            this@AutoScroller.cancel()

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