package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.uielements.view.ContextBasedViewModelRetriever

class ComparisonViewModelRetriever(context: Context)
    : ContextBasedViewModelRetriever<ComparisonViewModel, ExaminationActivity>(context, ComparisonViewModel::class.java)