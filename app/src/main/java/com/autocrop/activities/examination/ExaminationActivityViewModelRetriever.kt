package com.autocrop.activities.examination

import android.content.Context
import com.autocrop.retriever.viewmodel.ContextBasedViewModelRetriever

class ExaminationActivityViewModelRetriever(context: Context)
    : ContextBasedViewModelRetriever<ExaminationActivityViewModel, ExaminationActivity>(
        context,
        ExaminationActivityViewModel::class.java
    )