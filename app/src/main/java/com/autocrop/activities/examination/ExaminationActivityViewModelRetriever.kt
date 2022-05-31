package com.autocrop.activities.examination

import android.content.Context
import com.autocrop.uielements.view.ContextBasedViewModelRetriever

class ExaminationActivityViewModelRetriever(context: Context)
    : ContextBasedViewModelRetriever<ExaminationActivityViewModel, ExaminationActivity>(
        context,
        ExaminationActivityViewModel::class.java
    )