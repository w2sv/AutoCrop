package com.autocrop.activities.main

import androidx.lifecycle.ViewModel
import com.autocrop.activities.iodetermination.IODeterminationActivity

class MainActivityViewModel(val ioResults: IODeterminationActivity.Results?)
    : ViewModel() {
    var fadeInFlowFieldButtons = true
}