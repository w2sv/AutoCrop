package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.utils.android.extensions.postValue
import com.w2sv.autocrop.utils.android.extensions.viewModelImmediate

class CancelAutoScrollButton(context: Context, attributeSet: AttributeSet) :
    AppCompatButton(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            viewModelImmediate<CropPagerViewModel>().liveAutoScroll.postValue(false)
        }
    }
}