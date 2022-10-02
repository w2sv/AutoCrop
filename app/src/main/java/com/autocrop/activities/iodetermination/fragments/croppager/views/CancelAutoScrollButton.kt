package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.ui.elements.view.activityViewModel
import com.autocrop.utils.android.livedata.asMutable

class CancelAutoScrollButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet){

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            activityViewModel<CropPagerViewModel>().autoScroll.asMutable.postValue(false)
        }
    }
}