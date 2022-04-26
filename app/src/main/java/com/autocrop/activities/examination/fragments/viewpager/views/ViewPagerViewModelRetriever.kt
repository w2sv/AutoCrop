package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.uielements.view.ViewModelRetrievingView

class ViewPagerViewModelRetriever(context: Context):
    ViewModelRetrievingView<ViewPagerViewModel, ViewPagerFragment>(
        context,
        ViewPagerViewModel::class.java
    )