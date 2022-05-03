package com.autocrop.activities.main

import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    var fadeInFlowFieldButtons = true

    var stoppedAboutFragment = false
    var reinitializeRootFragment = false

    fun resetValues(){
        stoppedAboutFragment = false
        reinitializeRootFragment = false
    }
}