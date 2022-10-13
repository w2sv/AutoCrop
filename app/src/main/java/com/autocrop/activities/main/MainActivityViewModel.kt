package com.autocrop.activities.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.activities.iodetermination.IOSynopsis

class MainActivityViewModel(val ioSynopsis: IOSynopsis?,
                            val savedCropUris: ArrayList<Uri>?)
    : ViewModel() {
    var fadeInFlowFieldButtons = true
}