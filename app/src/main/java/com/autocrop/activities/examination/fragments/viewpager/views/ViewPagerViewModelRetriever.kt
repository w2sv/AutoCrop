package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragmentViewModel
import com.autocrop.uielements.view.ViewModelRetrievingView

class ViewPagerViewModelRetriever(context: Context):
    ViewModelRetrievingView<ViewPagerFragmentViewModel, ViewPagerFragment>(
        context,
        ViewPagerFragmentViewModel::class.java
    )