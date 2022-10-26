package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.utils.android.extensions.activityViewModel
import com.autocrop.utils.android.extensions.postValue

class CancelAutoScrollButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet){

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            activityViewModel<CropPagerViewModel>().autoScroll.postValue(false)
        }
    }
}