package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.databinding.FlowfieldBinding

class FlowFieldDrawerLayout(context: Context, attributeSet: AttributeSet) : DrawerLayout(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            val binding: FlowfieldBinding by lazy { findFragment<FlowFieldFragment>().binding }

            addDrawerListener(
                object : SimpleDrawerListener() {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        binding.affectAssociatedViewsOnDrawerSlide(slideOffset)
                    }
                }
            )
            if (isOpen) {
                binding.affectAssociatedViewsOnDrawerSlide(1f)
            }
        }
    }

    fun openDrawer() {
        openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        closeDrawer(GravityCompat.START)
    }

    fun toggleDrawer() {
        if (isOpen)
            closeDrawer()
        else
            openDrawer()
    }
}

private fun FlowfieldBinding.affectAssociatedViewsOnDrawerSlide(slideOffset: Float) {
    navigationViewToggleButton.progress = slideOffset

    val associatedButtonAlpha = 1 - slideOffset
    imageSelectionButton.alpha = associatedButtonAlpha
    shareCropsButton.alpha = associatedButtonAlpha
    foregroundElementsToggleButton.alpha = associatedButtonAlpha
}