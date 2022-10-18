package com.autocrop.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.autocrop.utils.android.extensions.ifNotInEditMode

abstract class ExtendedAppCompatImageButton(context: Context, attributeSet: AttributeSet)
    : AppCompatImageButton(context, attributeSet){

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            setOnClickListener{
                onClickListener()
            }
        }
    }

    protected abstract fun onClickListener()
}