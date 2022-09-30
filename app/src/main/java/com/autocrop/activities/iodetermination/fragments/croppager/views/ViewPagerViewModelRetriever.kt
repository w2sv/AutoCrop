package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.ViewPagerViewModel
import com.autocrop.retriever.viewmodel.ContextBasedViewModelRetriever

class ViewPagerViewModelRetriever(context: Context):
    ContextBasedViewModelRetriever<ViewPagerViewModel, IODeterminationActivity>(
        context,
        ViewPagerViewModel::class.java
    )