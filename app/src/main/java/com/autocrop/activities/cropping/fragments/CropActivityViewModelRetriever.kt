package com.autocrop.activities.cropping.fragments

import android.content.Context
import com.autocrop.activities.cropping.CropActivity
import com.autocrop.activities.cropping.CropActivityViewModel
import com.autocrop.retriever.viewmodel.ContextBasedViewModelRetriever

class CropActivityViewModelRetriever(context: Context):
    ContextBasedViewModelRetriever<CropActivityViewModel, CropActivity>(
        context,
        CropActivityViewModel::class.java
    )