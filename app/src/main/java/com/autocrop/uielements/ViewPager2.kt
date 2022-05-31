package com.autocrop.uielements

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * Reference: https://www.loginworks.com/blogs/how-to-make-awesome-transition-
 * effects-using-pagetransformer-in-android/
 */
class CubeOutPageTransformer: ViewPager2.PageTransformer{
    override fun transformPage(page: View, position: Float) {
        with(page) {
            pivotX = (if (position < 0) width else 0).toFloat()
            pivotY = height * 0.5f
            rotationY = 90f * position
        }
    }
}

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView