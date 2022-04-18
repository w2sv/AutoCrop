package com.autocrop.activities.cropping.fragments

import android.content.Context
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.uielements.view.AbstractContextBasedViewModelRetriever

class CroppingActivityViewModelRetriever(context: Context):
    AbstractContextBasedViewModelRetriever<CroppingActivityViewModel, CroppingActivity>(
        context,
        CroppingActivityViewModel::class.java
    )