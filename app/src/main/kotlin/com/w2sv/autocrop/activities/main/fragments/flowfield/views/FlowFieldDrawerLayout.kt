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

    private val binding: FlowfieldBinding by lazy { findFragment<FlowFieldFragment>().binding }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            addDrawerListener(
                object : SimpleDrawerListener() {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        binding.affectAssociatedViewsOnDrawerSlide(slideOffset)
                    }
                }
            )
            if (isOpen)
                binding.affectAssociatedViewsOnDrawerSlide(1f)
        }
    }

    fun FlowfieldBinding.affectAssociatedViewsOnDrawerSlide(slideOffset: Float) {
        navigationViewToggleButton.progress = slideOffset

        val associatedButtonAlpha = 1 - slideOffset
        imageSelectionButton.alpha = associatedButtonAlpha
        shareCropsButton.alpha = associatedButtonAlpha
        foregroundToggleButton.alpha = associatedButtonAlpha
    }

    fun openDrawer() {
        openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        closeDrawer(GravityCompat.START)
    }

    fun onToggleButtonClick() {
        if (isOpen)
            closeDrawer()
        else
            openDrawer()
    }
}