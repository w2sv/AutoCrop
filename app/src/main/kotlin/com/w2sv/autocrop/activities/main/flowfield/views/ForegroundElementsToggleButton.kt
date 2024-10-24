package com.w2sv.autocrop.activities.main.flowfield.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.w2sv.androidutils.view.increaseTouchArea
import com.w2sv.androidutils.view.viewModel
import com.w2sv.autocrop.activities.main.flowfield.FlowFieldFragment


class ForegroundElementsToggleButton(context: Context, attributeSet: AttributeSet) : AppCompatImageButton(
    context,
    attributeSet
) {

    private val viewModel by viewModel<FlowFieldFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        increaseTouchArea(40)

        setOnClickListener {
            viewModel.toggleHideForegroundElements()
        }
    }

    fun setForegroundElementsFadeAnimation(makeAnimation: () -> YoYoString) {
        fadeAnimation?.stop()
        fadeAnimation = makeAnimation()
    }

    private var fadeAnimation: YoYoString? = null
}