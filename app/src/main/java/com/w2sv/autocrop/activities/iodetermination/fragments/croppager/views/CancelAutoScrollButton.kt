package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.utils.android.extensions.postValue
import com.w2sv.autocrop.utils.android.extensions.viewModel

class CancelAutoScrollButton(context: Context, attributeSet: AttributeSet) :
    AppCompatButton(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            viewModel<CropPagerViewModel>().autoScroll.postValue(false)
        }
    }
}