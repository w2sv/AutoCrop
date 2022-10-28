package com.w2sv.autocrop.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.w2sv.autocrop.utils.android.extensions.ifNotInEditMode

abstract class ExtendedAppCompatImageButton(
    context: Context,
    attributeSet: AttributeSet
) : AppCompatImageButton(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            setOnClickListener {
                onClickListener()
            }
        }
    }

    protected abstract fun onClickListener()
}