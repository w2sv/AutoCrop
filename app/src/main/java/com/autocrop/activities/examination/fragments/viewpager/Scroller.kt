package com.autocrop.activities.examination.fragments.viewpager

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.utils.android.mutableLiveData
import java.util.*

/**
 * Class accounting for automatic scrolling at the start of crop examination
 */
class Scroller(private val scroll: LiveData<Boolean>):
    Timer() {

    fun run(viewPager2: ViewPager2, maxScrolls: Int) {
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
                            scroll.mutableLiveData.postValue(false)
                            return@post this@Scroller.cancel()
                        }
                    }
                }
            },
            1000L,
            1000L
        )
    }
}