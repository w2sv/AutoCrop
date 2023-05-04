package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.w2sv.androidutils.lifecycle.toggle
import com.w2sv.androidutils.ui.views.viewModel
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.ui.views.increaseTouchArea


class ForegroundElementsToggleButton(context: Context, attributeSet: AttributeSet) : AppCompatImageButton(
    context,
    attributeSet
) {

    private val viewModel by viewModel<FlowFieldFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        increaseTouchArea(40)

        setOnClickListener {
            viewModel.hideForegroundElementsLive.toggle()
        }
    }

    fun setForegroundElementsFadeAnimation(makeAnimation: () -> YoYoString) {
        fadeAnimation?.stop()
        fadeAnimation = makeAnimation()
    }

    private var fadeAnimation: YoYoString? = null
}