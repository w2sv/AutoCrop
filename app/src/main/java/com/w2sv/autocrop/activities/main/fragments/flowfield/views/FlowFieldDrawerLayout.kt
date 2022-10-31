package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.autocrop.utils.android.extensions.ifNotInEditMode

class FlowFieldDrawerLayout(context: Context, attributeSet: AttributeSet) : DrawerLayout(context, attributeSet) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            findFragment<FlowFieldFragment>()
                .binding
                .setAssociatedButtons()
        }
    }

    private fun FragmentFlowfieldBinding.setAssociatedButtons() {
        addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val fadeOutOnDrawerOpenAlpha = 1 - slideOffset

                navigationDrawerButtonBurger.alpha = fadeOutOnDrawerOpenAlpha
                navigationDrawerButtonArrow.alpha = slideOffset

                imageSelectionButton.alpha = fadeOutOnDrawerOpenAlpha
            }

            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        navigationDrawerButtonArrow.setOnClickListener {
            if (isOpen)
                closeDrawer(GravityCompat.START)
            else
                openDrawer(GravityCompat.START)
        }
    }
}