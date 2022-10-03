package com.autocrop.activities.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.dataclasses.IOSynopsis

class MainActivityViewModel(val ioSynopsis: IOSynopsis?,
                            val savedCropUris: ArrayList<Uri>?)
    : ViewModel() {
    var fadeInFlowFieldButtons = true
}