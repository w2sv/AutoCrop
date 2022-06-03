package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.retriever.viewmodel.ViewModelRetriever

class CancelAutoScrollButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context){

    init {
        setOnClickListener {
            sharedViewModel.autoScroll.postValue(false)
        }
    }
}