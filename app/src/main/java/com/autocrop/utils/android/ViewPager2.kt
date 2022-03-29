package com.autocrop.utils.android

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.utils.toInt

/**
 * Reference: https://www.loginworks.com/blogs/how-to-make-awesome-transition-
 * effects-using-pagetransformer-in-android/
 */
class CubeOutPageTransformer: ViewPager2.PageTransformer{
    override fun transformPage(page: View, position: Float) {
        with(page) {
            pivotX = listOf(0f, width.toFloat())[(position < 0f).toInt()]
            pivotY = height * 0.5f
            rotationY = 90f * position
        }
    }
}