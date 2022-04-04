package com.autocrop.activities.cropping.fragments

import android.content.Context
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.utils.android.AbstractViewModelRetriever

class CroppingActivityViewModelRetriever(context: Context):
    AbstractViewModelRetriever<CroppingActivityViewModel, CroppingActivity>(
        context,
        CroppingActivityViewModel::class.java
    )