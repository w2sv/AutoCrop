package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.utilsandroid.livedata.asMutable

class CancelAutoScrollButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context){

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            sharedViewModel.autoScroll.asMutable.postValue(false)
        }
    }
}