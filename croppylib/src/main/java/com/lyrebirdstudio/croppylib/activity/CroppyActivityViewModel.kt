package com.lyrebirdstudio.croppylib.activity

import android.content.Context
import androidx.lifecycle.ViewModel

class CroppyActivityViewModel : ViewModel() {
    var exitActivityAnimation: ((Context) -> Unit)? = null
}