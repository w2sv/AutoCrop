package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.uielements.view.ContextBasedViewModelRetriever

class ViewPagerViewModelRetriever(context: Context):
    ContextBasedViewModelRetriever<ViewPagerViewModel, ViewPagerFragment>(
        context,
        ViewPagerViewModel::class.java
    )