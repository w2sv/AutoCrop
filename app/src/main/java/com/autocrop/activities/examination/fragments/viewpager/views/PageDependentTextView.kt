package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.ExtendedAppCompatTextView

abstract class PageDependentTextView(context: Context, attr: AttributeSet):
    ExtendedAppCompatTextView(context, attr),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context) {
    abstract fun update(position: Int)
}