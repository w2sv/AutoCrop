package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.CropPagerFragment

class CancelAutoScrollButton(context: Context, attributeSet: AttributeSet) :
    AppCompatButton(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            viewModel<CropPagerFragment.ViewModel>().value.liveAutoScroll.postValue(false)
        }
    }
}