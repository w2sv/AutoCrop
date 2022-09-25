package com.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.croppager.viewmodel.ViewPagerViewModel
import com.autocrop.retriever.viewmodel.ContextBasedViewModelRetriever

class ViewPagerViewModelRetriever(context: Context):
    ContextBasedViewModelRetriever<ViewPagerViewModel, ExaminationActivity>(
        context,
        ViewPagerViewModel::class.java
    )