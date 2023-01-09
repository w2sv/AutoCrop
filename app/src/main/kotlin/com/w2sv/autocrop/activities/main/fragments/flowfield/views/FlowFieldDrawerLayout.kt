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
            findFragment<FlowFieldFragment>()
                .binding
                .setAssociatedButtons()
        }
    }

    private fun FragmentFlowfieldBinding.setAssociatedButtons() {
        addDrawerListener(
            object : SimpleDrawerListener() {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    navigationViewToggleButton.progress = slideOffset

                    val alphaOverlaidButtons = 1 - slideOffset
                    imageSelectionButton.alpha = alphaOverlaidButtons
                    shareCropsButton.alpha = alphaOverlaidButtons
                    foregroundToggleButton.alpha = alphaOverlaidButtons
                }
            }
        )

        navigationViewToggleButton.setOnClickListener {
            if (isOpen)
                closeDrawer(GravityCompat.START)
            else
                openDrawer(GravityCompat.START)
        }
    }
}