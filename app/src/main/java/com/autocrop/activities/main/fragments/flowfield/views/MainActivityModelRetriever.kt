package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.uielements.view.ViewModelHoldingView

class MainActivityModelRetriever(context: Context):
    ViewModelHoldingView<MainActivityViewModel, MainActivity>(
        context,
        MainActivityViewModel::class.java
    )