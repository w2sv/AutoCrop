package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding

class FlowFieldDrawerLayout(context: Context, attributeSet: AttributeSet) : DrawerLayout(context, attributeSet) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            addDrawerListener(
                object : SimpleDrawerListener() {

                    private val binding: FragmentFlowfieldBinding = findFragment<FlowFieldFragment>().binding

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        binding.navigationViewToggleButton.progress = slideOffset

                        val alphaOverlaidButtons = 1 - slideOffset
                        binding.imageSelectionButton.alpha = alphaOverlaidButtons
                        binding.shareCropsButton.alpha = alphaOverlaidButtons
                        binding.foregroundToggleButton.alpha = alphaOverlaidButtons
                    }
                }
            )
        }
    }

    fun openDrawer() {
        openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        closeDrawer(GravityCompat.START)
    }
}